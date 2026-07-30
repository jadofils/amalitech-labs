package multithreading.executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Executors.newFixedThreadPool(n) - a pool of exactly n worker threads, reused across submitted
 * tasks (no per-task thread creation cost). Extra tasks beyond n queue up and wait for a free worker.
 */
public class NewFixedThreadPool {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(2); // only 2 threads, no matter how many tasks

        for (int i = 1; i <= 5; i++) {
            int taskId = i;
            pool.submit(() -> {
                System.out.println("task " + taskId + " running on " + Thread.currentThread().getName());
                sleepQuietly(200);
            });
        }

        pool.shutdown(); // stop accepting new tasks, but let the already-submitted 5 finish
        pool.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("all tasks done (only ever 2 thread names appeared above)");
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
