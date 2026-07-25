package main.concurrent;

import main.exceptions.StudentNotFoundException;
import main.export.FileExporter;
import main.export.ReportGenerator;
import main.logging.Logger;
import main.manager.StudentManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Generates and exports grade reports for many students at once, one task per student on a
 * fixed-size {@link ExecutorService} ({@link Executors#newFixedThreadPool}, US-4/PBI-4) - rather
 * than one {@code exportSummary}/{@code exportDetailed} call after another as
 * {@link main.console.ExportGradeReportAction} does for a single student.
 *
 * <p>{@link ReportGenerator} and {@link FileExporter} are safe to share across the pool's threads:
 * neither holds per-call mutable state, and report generation only reads student/grade data - it
 * never writes to the repositories, so concurrent reads of the same {@code Map}-backed
 * repositories (PBI-1) are safe without extra synchronization, the same guarantee the JDK gives any
 * unsynchronized {@code Map} under read-only concurrent access.
 */
public class BatchReportService {

    public static final int MIN_THREADS = 2;
    public static final int MAX_THREADS = 8;

    private static final long SHUTDOWN_TIMEOUT_SECONDS = 30;

    public enum ReportKind {
        SUMMARY, DETAILED
    }

    private final StudentManager studentManager;
    private final ReportGenerator reportGenerator;
    private final FileExporter fileExporter;
    private final int threadCount;
    private final Supplier<ExecutorService> executorFactory;

    public BatchReportService(StudentManager studentManager, ReportGenerator reportGenerator,
                               FileExporter fileExporter, int threadCount) {
        this(studentManager, reportGenerator, fileExporter, threadCount,
                () -> Executors.newFixedThreadPool(threadCount));
    }

    /**
     * PBI-10: lets a test substitute a mocked {@link ExecutorService} for the real thread pool -
     * e.g. to verify shutdown always happens even when a submitted task fails, deterministically,
     * without depending on real thread timing. Ordinary callers should use the four-argument
     * constructor above; this one exists for tests.
     */
    public BatchReportService(StudentManager studentManager, ReportGenerator reportGenerator,
                               FileExporter fileExporter, int threadCount, Supplier<ExecutorService> executorFactory) {
        if (threadCount < MIN_THREADS || threadCount > MAX_THREADS) {
            throw new IllegalArgumentException(
                    "threadCount must be between " + MIN_THREADS + " and " + MAX_THREADS + ", got: " + threadCount);
        }
        this.studentManager = studentManager;
        this.reportGenerator = reportGenerator;
        this.fileExporter = fileExporter;
        this.threadCount = threadCount;
        this.executorFactory = executorFactory;
    }

    /**
     * Submits one report-generation task per student ID to a fixed thread pool and blocks until
     * every task completes. A single student's failure (unknown ID, disk error, ...) is caught and
     * recorded in that student's {@link StudentReportOutcome} rather than aborting the whole batch -
     * the same "skip and continue" philosophy {@code CSVParser}/{@code BulkImportService} already
     * use for per-row failures.
     */
    public BatchResult generateBatch(List<String> studentIds, ReportKind kind, String filenamePrefix) {
        long start = System.nanoTime();
        ExecutorService executor = executorFactory.get();

        try {
            List<Future<StudentReportOutcome>> futures = new ArrayList<>(studentIds.size());
            for (String studentId : studentIds) {
                futures.add(executor.submit(() -> generateOne(studentId, kind, filenamePrefix)));
            }

            List<StudentReportOutcome> outcomes = new ArrayList<>(futures.size());
            for (Future<StudentReportOutcome> future : futures) {
                outcomes.add(awaitOutcome(future));
            }

            long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
            return BatchResult.from(outcomes, elapsedMillis, threadCount);
        } finally {
            shutdown(executor);
        }
    }

    private StudentReportOutcome generateOne(String studentId, ReportKind kind, String filenamePrefix) {
        try {
            // ReportGenerator itself doesn't validate the student exists - it prints "Unknown"/0%
            // for an unrecognized ID instead of throwing (see ReportGeneratorTest) - so a batch
            // export checks explicitly first, matching what ExportGradeReportAction already does
            // for a single student, rather than silently writing a junk report to disk.
            if (studentManager.findStudent(studentId) == null) {
                throw new StudentNotFoundException("Student with ID '" + studentId + "' not found.", studentId, List.of());
            }
            String content = kind == ReportKind.SUMMARY
                    ? reportGenerator.exportSummary(studentId)
                    : reportGenerator.exportDetailed(studentId);
            String suffix = kind == ReportKind.SUMMARY ? "_summary" : "_detailed";
            FileExporter.FileExportResult result =
                    fileExporter.exportToFile(filenamePrefix + studentId + suffix + ".txt", content);
            return StudentReportOutcome.success(studentId, result.getFilePath());
        } catch (RuntimeException e) {
            Logger.error("Batch report generation failed for student " + studentId, e);
            return StudentReportOutcome.failure(studentId, e.getMessage());
        }
    }

    private StudentReportOutcome awaitOutcome(Future<StudentReportOutcome> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Batch report generation was interrupted", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Unexpected failure during batch report generation", e.getCause());
        }
    }

    private void shutdown(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /** One student's outcome: either the exported file's path, or the reason it failed - never both. */
    public record StudentReportOutcome(String studentId, boolean success, String filePathOrError) {
        static StudentReportOutcome success(String studentId, String filePath) {
            return new StudentReportOutcome(studentId, true, filePath);
        }

        static StudentReportOutcome failure(String studentId, String error) {
            return new StudentReportOutcome(studentId, false, error);
        }
    }

    /** Aggregate result of one {@link #generateBatch} call, including the elapsed wall-clock time for measuring speedup. */
    public record BatchResult(int totalRequested, int succeeded, int failed, long elapsedMillis,
                               int threadPoolSize, List<StudentReportOutcome> outcomes) {
        static BatchResult from(List<StudentReportOutcome> outcomes, long elapsedMillis, int threadPoolSize) {
            int succeeded = (int) outcomes.stream().filter(StudentReportOutcome::success).count();
            return new BatchResult(outcomes.size(), succeeded, outcomes.size() - succeeded,
                    elapsedMillis, threadPoolSize, outcomes);
        }
    }
}
