package main.concurrent;

import main.calculators.GPACalculator;
import main.logging.Logger;
import main.manager.GradeManager;
import main.model.student.Student;

import java.time.Instant;
import java.util.List;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Recalculates class-wide GPA rankings on a fixed schedule (daily by default, US-6/PBI-6) via a
 * single-thread {@link ScheduledExecutorService} - deliberately not the {@code CachedThreadPool}
 * {@link StatisticsDashboard} uses, so the two Sprint 2 stories each demonstrate a different
 * {@code java.util.concurrent} tool rather than reusing one. A single background thread is exactly
 * right here: unlike the dashboard's bursty per-tick work, this job is one task on a fixed
 * schedule, and only one run should ever be in flight at a time.
 *
 * <p>Reads through {@link GradeManager#readLocked}, the same lock {@link GradeManager#addGrade}
 * takes - the recalculation can't observe a grade addition mid-write, for the same reason
 * {@link StatisticsDashboard} needs to (PBI-5).
 */
public class ScheduledGpaRecalculationJob {

    public static final long DEFAULT_PERIOD_MILLIS = TimeUnit.DAYS.toMillis(1);

    private final GradeManager gradeManager;
    private final GPACalculator gpaCalculator;
    private final long periodMillis;

    private ScheduledExecutorService executor;
    private ScheduledFuture<?> scheduledTask;
    private volatile Instant lastRunAt;
    private volatile Instant nextRunAt;
    private volatile NavigableMap<Double, List<Student>> lastResult;

    public ScheduledGpaRecalculationJob(GradeManager gradeManager, GPACalculator gpaCalculator) {
        this(gradeManager, gpaCalculator, DEFAULT_PERIOD_MILLIS);
    }

    public ScheduledGpaRecalculationJob(GradeManager gradeManager, GPACalculator gpaCalculator, long periodMillis) {
        this.gradeManager = gradeManager;
        this.gpaCalculator = gpaCalculator;
        this.periodMillis = periodMillis;
    }

    /** @return true if the job was actually started; false if it was already running. */
    public boolean start() {
        return start(Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "gpa-recalculation-scheduler");
            thread.setDaemon(true);
            return thread;
        }));
    }

    /**
     * PBI-10: lets a test substitute a mocked {@link ScheduledExecutorService}, to verify
     * {@link #stop()}'s timeout/interrupt handling deterministically instead of waiting on a real
     * schedule. Ordinary callers should use the no-argument {@link #start()} above.
     *
     * @return true if the job was actually started; false if it was already running.
     */
    public boolean start(ScheduledExecutorService executor) {
        if (isRunning()) {
            return false;
        }
        this.executor = executor;
        scheduledTask = executor.scheduleAtFixedRate(this::runOnce, 0, periodMillis, TimeUnit.MILLISECONDS);
        return true;
    }

    /** @return true if the job was actually stopped; false if it wasn't running. */
    public boolean stop() {
        if (!isRunning()) {
            return false;
        }
        scheduledTask.cancel(false);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            nextRunAt = null;
            Logger.info("Scheduled GPA recalculation job stopped.");
        }
        return true;
    }

    public boolean isRunning() {
        return executor != null && !executor.isShutdown();
    }

    /** Runs one recalculation on demand, instead of waiting on the schedule - also used internally by the scheduler. */
    public void runOnce() {
        lastResult = gradeManager.readLocked(gpaCalculator::classRankings);
        lastRunAt = Instant.now();
        nextRunAt = isRunning() ? lastRunAt.plusMillis(periodMillis) : null;
        Logger.info("Scheduled GPA recalculation completed at " + lastRunAt + "; next run at " + nextRunAt);
    }

    public Optional<Instant> getLastRunAt() {
        return Optional.ofNullable(lastRunAt);
    }

    public Optional<Instant> getNextRunAt() {
        return Optional.ofNullable(nextRunAt);
    }

    public Optional<NavigableMap<Double, List<Student>>> getLastResult() {
        return Optional.ofNullable(lastResult);
    }
}
