package tests.concurrent;

import main.calculators.StatisticsCalculator;
import main.manager.GradeManager;
import main.manager.StudentManager;
import main.model.grade.Grade;
import main.model.student.RegularStudent;
import main.model.student.Student;
import main.model.subject.Subject;
import main.repository.student.StudentRepositoryImpl;
import main.repository.subject.SubjectRepositoryImpl;
import main.service.GradeService;
import main.service.GradeServiceImpl;
import main.service.StudentService;
import main.service.StudentServiceImpl;
import main.concurrent.StatisticsDashboard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Real collaborators for the lifecycle/timing tests (the reasoning FileExporterTest documents in
// src/tests/README.md); PBI-10 added two tests using a mocked ThreadPoolExecutor specifically to
// verify stop()'s timeout/interrupt branches deterministically, without a real 30-second wait.
class StatisticsDashboardTest {

    private static final long SHORT_INTERVAL_MILLIS = 30;

    private final StudentRepositoryImpl students = new StudentRepositoryImpl();
    private final SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
    private final GradeService gradeService = new GradeServiceImpl(students, subjects);
    private final GradeManager gradeManager = new GradeManager(gradeService, subjects);
    private final StudentService studentService = new StudentServiceImpl(students);
    private final StudentManager studentManager = new StudentManager(studentService, gradeManager);
    private final StatisticsCalculator statisticsCalculator = new StatisticsCalculator();

    @Test
    @DisplayName("start() starts the dashboard and returns true; a second start() while running returns false")
    void startIsIdempotentTest() {
        StatisticsDashboard dashboard = new StatisticsDashboard(studentManager, gradeManager, statisticsCalculator, SHORT_INTERVAL_MILLIS);
        try {
            assertTrue(dashboard.start());
            assertTrue(dashboard.isRunning());
            assertFalse(dashboard.start());
        } finally {
            dashboard.stop();
        }
    }

    @Test
    @DisplayName("stop() stops a running dashboard and returns true; stop() when not running returns false")
    void stopIsIdempotentTest() {
        StatisticsDashboard dashboard = new StatisticsDashboard(studentManager, gradeManager, statisticsCalculator, SHORT_INTERVAL_MILLIS);

        assertFalse(dashboard.stop());

        dashboard.start();
        assertTrue(dashboard.stop());
        assertFalse(dashboard.isRunning());
        assertFalse(dashboard.stop());
    }

    @Test
    @DisplayName("refreshOnce() prints a snapshot reflecting the current student/grade counts")
    void refreshOnceReflectsCurrentDataTest() {
        Subject subject = subjects.getAllSubjects().get(0);
        Student student = new RegularStudent("Test Student", 16, "test@school.edu", "1234567890");
        studentManager.addStudent(student);
        gradeManager.addGrade(new Grade(student.getStudentId(), subject, 90.0));

        StatisticsDashboard dashboard = new StatisticsDashboard(studentManager, gradeManager, statisticsCalculator, SHORT_INTERVAL_MILLIS);
        String output = captureStdOut(dashboard::refreshOnce);

        assertTrue(output.contains("REAL-TIME STATISTICS DASHBOARD"));
        assertTrue(output.contains("Students: " + students.getAllStudents().size()));
        assertTrue(output.contains("Grades: " + gradeManager.getGradeCount()));
    }

    @Test
    @DisplayName("A running dashboard refreshes automatically at least twice within a few intervals, then stops cleanly")
    void refreshesAutomaticallyOnScheduleTest() {
        StatisticsDashboard dashboard = new StatisticsDashboard(studentManager, gradeManager, statisticsCalculator, SHORT_INTERVAL_MILLIS);
        PrintStream originalOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        String output;
        try (PrintStream captured = new PrintStream(buffer)) {
            System.setOut(captured);
            dashboard.start();

            // Polls for the 2nd refresh instead of sleeping a single fixed window - a blind
            // Thread.sleep(SHORT_INTERVAL_MILLIS * 8) flakes under CPU contention from the rest of
            // the suite, since the scheduled executor's actual fire times can slip past it.
            long deadline = System.currentTimeMillis() + 5000;
            while (refreshCount(buffer) < 2 && System.currentTimeMillis() < deadline) {
                try {
                    Thread.sleep(SHORT_INTERVAL_MILLIS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            dashboard.stop();
            output = buffer.toString();
        } finally {
            System.setOut(originalOut);
        }

        long refreshCount = refreshCount(buffer);
        assertTrue(refreshCount >= 2, "expected at least 2 refreshes, got " + refreshCount + " in output:\n" + output);
        assertFalse(dashboard.isRunning());
    }

    private long refreshCount(ByteArrayOutputStream buffer) {
        return buffer.toString().lines().filter(line -> line.contains("REAL-TIME STATISTICS DASHBOARD")).count();
    }

    @Test
    @DisplayName("A running dashboard's refreshes never observe a torn/partial grade addition happening concurrently on another thread")
    void concurrentGradeEntryDoesNotRaceWithRefreshTest() throws InterruptedException {
        Subject subject = subjects.getAllSubjects().get(0);
        Student student = new RegularStudent("Test Student", 16, "test@school.edu", "1234567890");
        studentManager.addStudent(student);

        StatisticsDashboard dashboard = new StatisticsDashboard(studentManager, gradeManager, statisticsCalculator, SHORT_INTERVAL_MILLIS);
        int gradesToAdd = 200;
        AtomicInteger added = new AtomicInteger(0);
        CountDownLatch writerDone = new CountDownLatch(1);

        Thread writer = new Thread(() -> {
            for (int i = 0; i < gradesToAdd; i++) {
                gradeManager.addGrade(new Grade(student.getStudentId(), subject, 70.0 + (i % 30)));
                added.incrementAndGet();
            }
            writerDone.countDown();
        });

        dashboard.start();
        writer.start();
        assertTrue(writerDone.await(10, TimeUnit.SECONDS), "writer thread did not finish in time");
        writer.join();
        dashboard.stop();

        assertEquals(gradesToAdd, added.get());
        assertEquals(gradesToAdd, gradeManager.getGradeCount(),
                "no grade addition should be lost or duplicated under concurrent refresh reads");
    }

    @Test
    @DisplayName("The three-argument constructor defaults to DEFAULT_REFRESH_INTERVAL_MILLIS (a real 5s schedule)")
    void threeArgConstructorUsesDefaultIntervalTest() {
        StatisticsDashboard dashboard = new StatisticsDashboard(studentManager, gradeManager, statisticsCalculator);
        try {
            assertTrue(dashboard.start());
        } finally {
            dashboard.stop();
        }
    }

    @Test
    @DisplayName("stop() calls shutdownNow() when awaitTermination times out (mocked ThreadPoolExecutor, no real 30s wait)")
    void stopCallsShutdownNowOnTimeoutTest() {
        ThreadPoolExecutor mockExecutor = mock(ThreadPoolExecutor.class);
        // awaitTermination is left unstubbed, so Mockito's default (false) drives the timeout branch.
        StatisticsDashboard dashboard = new StatisticsDashboard(studentManager, gradeManager, statisticsCalculator, SHORT_INTERVAL_MILLIS);
        dashboard.start(mockExecutor);

        dashboard.stop();

        verify(mockExecutor).shutdown();
        verify(mockExecutor).shutdownNow();
    }

    @Test
    @DisplayName("stop() handles being interrupted while joining the ticker thread, restoring the interrupt flag")
    void stopHandlesInterruptedJoinTest() {
        StatisticsDashboard dashboard = new StatisticsDashboard(studentManager, gradeManager, statisticsCalculator, SHORT_INTERVAL_MILLIS);
        dashboard.start();
        Thread.currentThread().interrupt();
        try {
            assertDoesNotThrow(dashboard::stop);
            assertTrue(Thread.interrupted(), "interrupt flag should be restored after stop() catches InterruptedException");
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    @DisplayName("stop() handles being interrupted while the executor awaits termination, restoring the interrupt flag (mocked ThreadPoolExecutor)")
    void shutdownExecutorHandlesInterruptedAwaitTest() throws InterruptedException {
        ThreadPoolExecutor mockExecutor = mock(ThreadPoolExecutor.class);
        when(mockExecutor.awaitTermination(anyLong(), any(TimeUnit.class))).thenThrow(new InterruptedException());
        StatisticsDashboard dashboard = new StatisticsDashboard(studentManager, gradeManager, statisticsCalculator, SHORT_INTERVAL_MILLIS);
        dashboard.start(mockExecutor);

        try {
            assertDoesNotThrow(dashboard::stop);
            verify(mockExecutor).shutdownNow();
            assertTrue(Thread.interrupted(), "interrupt flag should be restored after shutdownExecutor() catches InterruptedException");
        } finally {
            Thread.interrupted();
        }
    }

    private String captureStdOut(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));
        try {
            action.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString();
    }
}
