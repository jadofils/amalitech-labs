package tests.concurrent;

import main.calculators.GPACalculator;
import main.concurrent.ScheduledGpaRecalculationJob;
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.NavigableMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

// Real collaborators throughout - same reasoning as StatisticsDashboardTest: this class's
// interesting behavior is scheduling/lifecycle/timing, not branching logic worth mocking.
class ScheduledGpaRecalculationJobTest {

    private static final long SHORT_PERIOD_MILLIS = 30;

    private final StudentRepositoryImpl students = new StudentRepositoryImpl();
    private final SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
    private final GradeService gradeService = new GradeServiceImpl(students, subjects);
    private final GradeManager gradeManager = new GradeManager(gradeService, subjects);
    private final StudentService studentService = new StudentServiceImpl(students);
    private final StudentManager studentManager = new StudentManager(studentService, gradeManager);
    private final GPACalculator gpaCalculator = new GPACalculator(gradeManager, studentManager);

    @Test
    @DisplayName("start() starts the job and returns true; a second start() while running returns false")
    void startIsIdempotentTest() {
        ScheduledGpaRecalculationJob job = new ScheduledGpaRecalculationJob(gradeManager, gpaCalculator, SHORT_PERIOD_MILLIS);
        try {
            assertTrue(job.start());
            assertTrue(job.isRunning());
            assertFalse(job.start());
        } finally {
            job.stop();
        }
    }

    @Test
    @DisplayName("stop() stops a running job and returns true; stop() when not running returns false")
    void stopIsIdempotentTest() {
        ScheduledGpaRecalculationJob job = new ScheduledGpaRecalculationJob(gradeManager, gpaCalculator, SHORT_PERIOD_MILLIS);

        assertFalse(job.stop());

        job.start();
        assertTrue(job.stop());
        assertFalse(job.isRunning());
        assertFalse(job.stop());
    }

    @Test
    @DisplayName("runOnce() populates lastRunAt/lastResult matching a direct classRankings() call")
    void runOnceMatchesDirectCalculationTest() {
        Subject subject = subjects.getAllSubjects().get(0);
        Student student = new RegularStudent("Test Student", 16, "test@school.edu", "1234567890");
        studentManager.addStudent(student);
        gradeManager.addGrade(new Grade(student.getStudentId(), subject, 90.0));

        ScheduledGpaRecalculationJob job = new ScheduledGpaRecalculationJob(gradeManager, gpaCalculator, SHORT_PERIOD_MILLIS);
        assertTrue(job.getLastRunAt().isEmpty());
        assertTrue(job.getLastResult().isEmpty());

        job.runOnce();

        assertTrue(job.getLastRunAt().isPresent());
        NavigableMap<Double, List<Student>> expected = gpaCalculator.classRankings();
        assertEquals(expected, job.getLastResult().orElseThrow());
    }

    @Test
    @DisplayName("A running job re-runs automatically on schedule, and stops producing new runs once stopped")
    void runsAutomaticallyOnScheduleThenStopsTest() throws InterruptedException {
        ScheduledGpaRecalculationJob job = new ScheduledGpaRecalculationJob(gradeManager, gpaCalculator, SHORT_PERIOD_MILLIS);

        job.start();
        Instant firstObserved = awaitFirstRun(job);
        Thread.sleep(SHORT_PERIOD_MILLIS * 5);
        Instant laterObserved = job.getLastRunAt().orElseThrow();
        assertTrue(laterObserved.isAfter(firstObserved), "expected at least one more scheduled run after the first");

        job.stop();
        assertTrue(job.getNextRunAt().isEmpty());
        Instant atStop = job.getLastRunAt().orElseThrow();
        Thread.sleep(SHORT_PERIOD_MILLIS * 5);
        assertEquals(atStop, job.getLastRunAt().orElseThrow(), "no further runs should happen after stop()");
    }

    @Test
    @DisplayName("Concurrent grade entry while the job is running never corrupts a recalculation")
    void concurrentGradeEntryDoesNotRaceWithRecalculationTest() throws InterruptedException {
        Subject subject = subjects.getAllSubjects().get(0);
        Student student = new RegularStudent("Test Student", 16, "test@school.edu", "1234567890");
        studentManager.addStudent(student);

        ScheduledGpaRecalculationJob job = new ScheduledGpaRecalculationJob(gradeManager, gpaCalculator, SHORT_PERIOD_MILLIS);
        int gradesToAdd = 150;
        AtomicInteger added = new AtomicInteger(0);
        CountDownLatch writerDone = new CountDownLatch(1);

        Thread writer = new Thread(() -> {
            for (int i = 0; i < gradesToAdd; i++) {
                gradeManager.addGrade(new Grade(student.getStudentId(), subject, 70.0 + (i % 30)));
                added.incrementAndGet();
            }
            writerDone.countDown();
        });

        job.start();
        writer.start();
        assertTrue(writerDone.await(10, TimeUnit.SECONDS), "writer thread did not finish in time");
        writer.join();
        job.stop();

        assertEquals(gradesToAdd, added.get());
        assertEquals(gradesToAdd, gradeManager.getGradeCount());
        int totalRankedStudents = job.getLastResult().orElseThrow().values().stream().mapToInt(List::size).sum();
        assertEquals(studentManager.getAllStudents().size(), totalRankedStudents,
                "every student must appear in exactly one GPA bucket, with none lost or duplicated");
    }

    @Test
    @DisplayName("The no-argument constructor defaults to DEFAULT_PERIOD_MILLIS (a real daily schedule)")
    void noArgConstructorUsesDefaultPeriodTest() {
        ScheduledGpaRecalculationJob job = new ScheduledGpaRecalculationJob(gradeManager, gpaCalculator);
        try {
            assertTrue(job.start());
        } finally {
            job.stop();
        }
    }

    @Test
    @DisplayName("stop() calls shutdownNow() when awaitTermination times out (mocked ScheduledExecutorService, no real 30s wait)")
    void stopCallsShutdownNowOnTimeoutTest() {
        ScheduledExecutorService mockExecutor = mock(ScheduledExecutorService.class);
        ScheduledFuture<?> mockFuture = mock(ScheduledFuture.class);
        doReturn(mockFuture).when(mockExecutor).scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
        // awaitTermination is left unstubbed, so Mockito's default (false) drives the timeout branch.

        ScheduledGpaRecalculationJob job = new ScheduledGpaRecalculationJob(gradeManager, gpaCalculator, SHORT_PERIOD_MILLIS);
        job.start(mockExecutor);

        job.stop();

        verify(mockExecutor).shutdown();
        verify(mockExecutor).shutdownNow();
    }

    @Test
    @DisplayName("stop() handles being interrupted while awaiting termination, restoring the interrupt flag")
    void stopHandlesInterruptedAwaitTest() {
        ScheduledGpaRecalculationJob job = new ScheduledGpaRecalculationJob(gradeManager, gpaCalculator, SHORT_PERIOD_MILLIS);
        job.start();
        Thread.currentThread().interrupt();
        try {
            assertDoesNotThrow(job::stop);
            assertTrue(Thread.interrupted(), "interrupt flag should be restored after stop() catches InterruptedException");
        } finally {
            Thread.interrupted();
        }
    }

    private Instant awaitFirstRun(ScheduledGpaRecalculationJob job) throws InterruptedException {
        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5);
        while (System.currentTimeMillis() < deadline) {
            if (job.getLastRunAt().isPresent()) {
                return job.getLastRunAt().get();
            }
            Thread.sleep(5);
        }
        throw new AssertionError("job did not run within the expected time");
    }
}
