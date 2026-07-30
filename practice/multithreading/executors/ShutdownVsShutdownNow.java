package multithreading.executors;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * shutdown() - stops accepting new tasks but lets everything already queued/running finish normally.
 * shutdownNow() - attempts to stop immediately: interrupts running tasks and returns the tasks that
 * were still queued and never got to start, as a List you can inspect or reschedule.
 */
public class ShutdownVsShutdownNow {

    public static void main(String[] args) throws InterruptedException {
        gracefulShutdown();
        immediateShutdownNow();
    }

    private static void gracefulShutdown() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        for (int i = 1; i <= 3; i++) {
            int taskId = i;
            pool.submit(() -> {
                sleepQuietly(100);
                System.out.println("graceful: task " + taskId + " completed");
            });
        }
        pool.shutdown(); // the 3 queued tasks still all run to completion
        pool.awaitTermination(5, TimeUnit.SECONDS);
    }

    private static void immediateShutdownNow() {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.submit(() -> sleepQuietly(1000)); // occupies the only worker thread
        for (int i = 1; i <= 3; i++) {
            int taskId = i;
            pool.submit(() -> System.out.println("this should never print: task " + taskId)); // never gets a turn
        }

        List<Runnable> neverStarted = pool.shutdownNow(); // interrupts the running task, drops the queued ones
        System.out.println("shutdownNow() returned " + neverStarted.size() + " tasks that never started");
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
