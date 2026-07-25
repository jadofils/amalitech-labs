package main.concurrent;

import main.calculators.StatisticsCalculator;
import main.logging.Logger;
import main.manager.GradeManager;
import main.manager.StudentManager;
import main.model.grade.Grade;
import main.model.student.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Refreshes and prints a compact class-statistics snapshot every {@code refreshIntervalMillis}
 * (5000 by default, per US-5/PBI-5) while running, until explicitly {@link #stop()}ped - never
 * silently always-on. A single dedicated "ticker" thread drives the timing (a plain sleep loop);
 * the actual per-tick statistics computation runs on a {@code CachedThreadPool}
 * ({@link ThreadPoolExecutor} configured exactly as {@link java.util.concurrent.Executors#newCachedThreadPool()}
 * would build one), per the v3 spec's own "CachedThreadPool (stats)" pairing - kept separate from
 * the fixed-rate {@code ScheduledExecutorService} PBI-6 uses for its own daily job, since the two
 * stories are deliberately meant to demonstrate different {@code java.util.concurrent} tools.
 *
 * <p>Race-safety against a concurrent grade-entry action (this story's specific concern) comes
 * from reading through {@link GradeManager#readLocked}, which shares a lock with
 * {@link GradeManager#addGrade} - a refresh can never observe a grade addition mid-write.
 *
 * <p>Also prints {@link GradeManager#getGradeCacheHitRate()} (US-8/PBI-8) each tick - the
 * dashboard's own repeated per-student reads are exactly the workload that cache is meant to
 * speed up, so its hit rate climbing across ticks is a direct, visible measure of the cache doing
 * its job.
 */
public class StatisticsDashboard {

    public static final long DEFAULT_REFRESH_INTERVAL_MILLIS = 5000;

    private final StudentManager studentManager;
    private final GradeManager gradeManager;
    private final StatisticsCalculator statisticsCalculator;
    private final long refreshIntervalMillis;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private ThreadPoolExecutor executor;
    private Thread ticker;

    public StatisticsDashboard(StudentManager studentManager, GradeManager gradeManager,
                               StatisticsCalculator statisticsCalculator) {
        this(studentManager, gradeManager, statisticsCalculator, DEFAULT_REFRESH_INTERVAL_MILLIS);
    }

    public StatisticsDashboard(StudentManager studentManager, GradeManager gradeManager,
                               StatisticsCalculator statisticsCalculator, long refreshIntervalMillis) {
        this.studentManager = studentManager;
        this.gradeManager = gradeManager;
        this.statisticsCalculator = statisticsCalculator;
        this.refreshIntervalMillis = refreshIntervalMillis;
    }

    /** @return true if the dashboard was actually started; false if it was already running. */
    public boolean start() {
        if (!running.compareAndSet(false, true)) {
            return false;
        }
        executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());
        ticker = new Thread(this::tickLoop, "dashboard-ticker");
        ticker.setDaemon(true);
        ticker.start();
        return true;
    }

    /** @return true if the dashboard was actually stopped; false if it wasn't running. */
    public boolean stop() {
        if (!running.compareAndSet(true, false)) {
            return false;
        }
        ticker.interrupt();
        try {
            ticker.join(TimeUnit.SECONDS.toMillis(5));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        shutdownExecutor();
        return true;
    }

    public boolean isRunning() {
        return running.get();
    }

    private void tickLoop() {
        while (running.get()) {
            try {
                Thread.sleep(refreshIntervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            if (running.get()) {
                executor.submit(this::refreshOnce);
            }
        }
    }

    /** Computes and prints one snapshot on demand, instead of waiting on the schedule - also used internally by the ticker loop. */
    public void refreshOnce() {
        DashboardSnapshot snapshot = gradeManager.readLocked(this::captureSnapshot);
        print(snapshot);
    }

    private DashboardSnapshot captureSnapshot() {
        List<Student> allStudents = studentManager.getAllStudents();
        List<Grade> allGrades = new ArrayList<>();
        for (Student student : allStudents) {
            allGrades.addAll(gradeManager.getGradesForStudent(student.getStudentId()));
        }
        StatisticsCalculator.StatsResult stats = statisticsCalculator.calculateStats(allGrades, allStudents);
        int activeThreads = executor == null ? 0 : executor.getActiveCount();
        return new DashboardSnapshot(allStudents.size(), allGrades.size(), stats.getMean(), activeThreads,
                gradeManager.getGradeCacheHitRate());
    }

    private void print(DashboardSnapshot snapshot) {
        System.out.println("\nREAL-TIME STATISTICS DASHBOARD");
        System.out.println("Active Threads: " + snapshot.activeThreadCount());
        System.out.println("Cache Hit Rate: " + String.format("%.1f%%", snapshot.gradeCacheHitRate() * 100));
        System.out.println("Students: " + snapshot.studentCount() + " | Grades: " + snapshot.gradeCount()
                + " | Mean: " + String.format("%.1f%%", snapshot.meanGrade()));
        System.out.println("Grade Distribution updating live");
    }

    private void shutdownExecutor() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            Logger.info("Statistics dashboard stopped.");
        }
    }

    /** One dashboard refresh's numbers. */
    public record DashboardSnapshot(int studentCount, int gradeCount, double meanGrade, int activeThreadCount,
                                     double gradeCacheHitRate) {
    }
}
