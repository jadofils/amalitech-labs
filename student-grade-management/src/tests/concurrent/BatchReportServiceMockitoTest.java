package tests.concurrent;

import main.concurrent.BatchReportService;
import main.concurrent.BatchReportService.BatchResult;
import main.concurrent.BatchReportService.ReportKind;
import main.export.FileExporter;
import main.export.ReportGenerator;
import main.manager.StudentManager;
import main.model.student.RegularStudent;
import main.model.student.Student;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class BatchReportServiceMockitoTest {

    private static final int SIMULATED_WORK_MILLIS = 25;

    @Test
    @DisplayName("A student whose report generation throws is recorded as a failed outcome, and the rest of the batch still completes")
    void oneFailureDoesNotAbortTheBatchTest() {
        StudentManager studentManager = mock(StudentManager.class);
        ReportGenerator reportGenerator = mock(ReportGenerator.class);
        FileExporter fileExporter = mock(FileExporter.class);

        Student ok1 = new RegularStudent("Student One", 16, "one@school.edu", "1234567890");
        Student ok2 = new RegularStudent("Student Two", 16, "two@school.edu", "1234567890");
        when(studentManager.findStudent(ok1.getStudentId())).thenReturn(ok1);
        when(studentManager.findStudent(ok2.getStudentId())).thenReturn(ok2);
        when(studentManager.findStudent("BOOM")).thenReturn(new RegularStudent("Boom", 16, "boom@school.edu", "1234567890"));

        when(reportGenerator.exportSummary(ok1.getStudentId())).thenReturn("report one");
        when(reportGenerator.exportSummary(ok2.getStudentId())).thenReturn("report two");
        when(reportGenerator.exportSummary("BOOM")).thenThrow(new RuntimeException("disk exploded"));
        when(fileExporter.exportToFile(anyString(), anyString()))
                .thenReturn(new FileExporter.FileExportResult("reports/x.txt", 10));

        BatchReportService service = new BatchReportService(studentManager, reportGenerator, fileExporter, BatchReportService.MIN_THREADS);
        BatchResult result = service.generateBatch(List.of(ok1.getStudentId(), "BOOM", ok2.getStudentId()), ReportKind.SUMMARY, "batch_");

        assertEquals(3, result.totalRequested());
        assertEquals(2, result.succeeded());
        assertEquals(1, result.failed());
        assertTrue(result.outcomes().stream().anyMatch(o -> o.studentId().equals("BOOM") && !o.success()));
    }

    @Test
    @DisplayName("Concurrent generation over a fixed thread pool is measurably faster than sequential, for simulated per-student work")
    void concurrentGenerationIsFasterThanSequentialTest() {
        int studentCount = 16;
        int threadCount = 8;

        StudentManager studentManager = mock(StudentManager.class);
        ReportGenerator reportGenerator = mock(ReportGenerator.class);
        FileExporter fileExporter = mock(FileExporter.class);

        List<String> studentIds = new ArrayList<>();
        for (int i = 0; i < studentCount; i++) {
            Student student = new RegularStudent("Student " + i, 16, "s" + i + "@school.edu", "1234567890");
            studentIds.add(student.getStudentId());
            when(studentManager.findStudent(student.getStudentId())).thenReturn(student);
            when(reportGenerator.exportDetailed(student.getStudentId())).thenAnswer(invocation -> {
                Thread.sleep(SIMULATED_WORK_MILLIS);
                return "report for " + student.getStudentId();
            });
        }
        when(fileExporter.exportToFile(anyString(), anyString()))
                .thenReturn(new FileExporter.FileExportResult("reports/x.txt", 10));

        long sequentialStart = System.nanoTime();
        for (String studentId : studentIds) {
            reportGenerator.exportDetailed(studentId);
            fileExporter.exportToFile("seq_" + studentId + ".txt", "content");
        }
        long sequentialMillis = (System.nanoTime() - sequentialStart) / 1_000_000;

        BatchReportService service = new BatchReportService(studentManager, reportGenerator, fileExporter, threadCount);
        BatchResult result = service.generateBatch(studentIds, ReportKind.DETAILED, "batch_");

        assertEquals(studentCount, result.succeeded());
        double speedup = (double) sequentialMillis / result.elapsedMillis();
        System.out.printf("BatchReportService measured speedup: %.2fx (sequential=%dms, parallel=%dms, threads=%d)%n",
                speedup, sequentialMillis, result.elapsedMillis(), threadCount);

        // Not asserting an exact ratio (real wall-clock timing is inherently noisy, especially in
        // CI) - just that the parallel run is meaningfully faster, with enough margin to not be
        // flaky. The actual measured number is printed above for the record, per this story's own
        // "record actual measured speedup instead" guidance.
        assertTrue(result.elapsedMillis() < sequentialMillis,
                "parallel (" + result.elapsedMillis() + "ms) should be faster than sequential (" + sequentialMillis + "ms)");
    }

    @Test
    @DisplayName("generateBatch() still shuts the executor down even when task submission itself fails (PBI-10: mocked ExecutorService, no real threads)")
    void shutsDownExecutorEvenWhenSubmissionFailsTest() {
        ExecutorService mockExecutor = mock(ExecutorService.class);
        when(mockExecutor.submit(any(Callable.class))).thenThrow(new RejectedExecutionException("pool exhausted"));

        StudentManager studentManager = mock(StudentManager.class);
        ReportGenerator reportGenerator = mock(ReportGenerator.class);
        FileExporter fileExporter = mock(FileExporter.class);
        BatchReportService service = new BatchReportService(studentManager, reportGenerator, fileExporter,
                BatchReportService.MIN_THREADS, () -> mockExecutor);

        List<String> studentIds = List.of("STU001");
        assertThrows(RejectedExecutionException.class,
                () -> service.generateBatch(studentIds, ReportKind.SUMMARY, "batch_"));

        verify(mockExecutor, times(1)).shutdown();
    }

    @Test
    @DisplayName("A student whose Future.get() is interrupted stops the batch with IllegalStateException and restores the interrupt flag")
    void awaitOutcomeHandlesInterruptedExceptionTest() {
        StudentManager studentManager = mock(StudentManager.class);
        ReportGenerator reportGenerator = mock(ReportGenerator.class);
        FileExporter fileExporter = mock(FileExporter.class);
        Student student = new RegularStudent("Test Student", 16, "t@school.edu", "1234567890");
        when(studentManager.findStudent(student.getStudentId())).thenReturn(student);
        when(reportGenerator.exportSummary(student.getStudentId())).thenReturn("content");

        BatchReportService service = new BatchReportService(studentManager, reportGenerator, fileExporter, BatchReportService.MIN_THREADS);

        // Future.get() (like ExecutorService.awaitTermination()) throws InterruptedException
        // immediately if the calling thread is already interrupted when it's called - no need to
        // race a real interrupt against real work.
        List<String> studentIds = List.of(student.getStudentId());
        Thread.currentThread().interrupt();
        try {
            assertThrows(IllegalStateException.class,
                    () -> service.generateBatch(studentIds, ReportKind.SUMMARY, "batch_"));
            assertTrue(Thread.interrupted(), "interrupt flag should be restored after awaitOutcome() catches InterruptedException");
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    @DisplayName("A Future.get() ExecutionException is wrapped as IllegalStateException, unwrapping the real cause")
    void awaitOutcomeHandlesExecutionExceptionTest() throws Exception {
        ExecutorService mockExecutor = mock(ExecutorService.class);
        Future<Object> mockFuture = mock(Future.class);
        when(mockFuture.get()).thenThrow(new ExecutionException("boom", new RuntimeException("root cause")));
        doReturn(mockFuture).when(mockExecutor).submit(any(Callable.class));

        StudentManager studentManager = mock(StudentManager.class);
        ReportGenerator reportGenerator = mock(ReportGenerator.class);
        FileExporter fileExporter = mock(FileExporter.class);
        BatchReportService service = new BatchReportService(studentManager, reportGenerator, fileExporter,
                BatchReportService.MIN_THREADS, () -> mockExecutor);

        List<String> studentIds = List.of("STU001");
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.generateBatch(studentIds, ReportKind.SUMMARY, "batch_"));
        assertEquals("root cause", ex.getCause().getMessage());
    }

    @Test
    @DisplayName("shutdown() handles being interrupted while awaiting termination, restoring the interrupt flag (mocked ExecutorService)")
    void shutdownHandlesInterruptedAwaitTest() throws Exception {
        ExecutorService mockExecutor = mock(ExecutorService.class);
        when(mockExecutor.submit(any(Callable.class))).thenAnswer(invocation -> {
            Callable<?> callable = invocation.getArgument(0);
            return CompletableFuture.completedFuture(callable.call());
        });
        when(mockExecutor.awaitTermination(anyLong(), any(TimeUnit.class))).thenThrow(new InterruptedException());

        StudentManager studentManager = mock(StudentManager.class);
        Student student = new RegularStudent("Test Student", 16, "t@school.edu", "1234567890");
        when(studentManager.findStudent(student.getStudentId())).thenReturn(student);
        ReportGenerator reportGenerator = mock(ReportGenerator.class);
        when(reportGenerator.exportSummary(student.getStudentId())).thenReturn("content");
        FileExporter fileExporter = mock(FileExporter.class);
        when(fileExporter.exportToFile(anyString(), anyString()))
                .thenReturn(new FileExporter.FileExportResult("reports/x.txt", 10));

        BatchReportService service = new BatchReportService(studentManager, reportGenerator, fileExporter,
                BatchReportService.MIN_THREADS, () -> mockExecutor);

        try {
            service.generateBatch(List.of(student.getStudentId()), ReportKind.SUMMARY, "batch_");
            verify(mockExecutor).shutdownNow();
            assertTrue(Thread.interrupted(), "interrupt flag should be restored after shutdown() catches InterruptedException");
        } finally {
            Thread.interrupted();
        }
    }
}
