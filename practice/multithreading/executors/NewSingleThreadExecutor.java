package multithreading.executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Executors.newSingleThreadExecutor() - exactly ONE worker thread, so submitted tasks always run
 * one at a time in submission order. A cheap way to serialize work without hand-written locking.
 */
public class NewSingleThreadExecutor {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        for (int i = 1; i <= 5; i++) {
            int taskId = i;
            executor.submit(() -> System.out.println("task " + taskId + " on " + Thread.currentThread().getName()));
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("tasks always print in order 1..5 - same single thread, one at a time");
    }
}
