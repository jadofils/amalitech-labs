package multithreading.executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Executors.newCachedThreadPool() - creates new threads on demand (no fixed cap) and reuses idle
 * ones; idle threads are killed after 60s. Good for many short-lived tasks, dangerous for
 * unbounded/long-running ones (nothing stops it from creating a huge number of threads).
 */
public class NewCachedThreadPool {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService pool = Executors.newCachedThreadPool();

        for (int i = 1; i <= 5; i++) {
            int taskId = i;
            pool.submit(() -> System.out.println("task " + taskId + " running on " + Thread.currentThread().getName()));
        }

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("all tasks done (likely up to 5 distinct thread names -> no fixed cap)");
    }
}
