package multithreading.executors;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Future<V> - a handle to a result that isn't ready yet. get() blocks until it is (or throws); isDone()/cancel() don't block. */
public class FutureDemo {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Callable<Integer> slowComputation = () -> {
            Thread.sleep(300);
            return 42;
        };

        Future<Integer> future = executor.submit(slowComputation);

        System.out.println("isDone() right after submit: " + future.isDone()); // almost certainly false - it just started
        System.out.println("get() blocks until ready: " + future.get());       // waits for the 300ms to elapse
        System.out.println("isDone() after get(): " + future.isDone());

        getWithTimeout(executor);
        cancelling(executor);

        executor.shutdown();
    }

    private static void getWithTimeout(ExecutorService executor) throws InterruptedException, ExecutionException {
        Future<Integer> slowFuture = executor.submit(() -> {
            Thread.sleep(2000);
            return 1;
        });
        try {
            slowFuture.get(100, TimeUnit.MILLISECONDS); // don't wait the full 2s - give up after 100ms
        } catch (TimeoutException e) {
            System.out.println("get(timeout) gave up early instead of blocking for the full 2s");
        }
        slowFuture.cancel(true); // stop waiting on it entirely
    }

    private static void cancelling(ExecutorService executor) {
        Future<Integer> future = executor.submit(() -> {
            Thread.sleep(5000);
            return 1;
        });
        boolean cancelled = future.cancel(true); // true = interrupt it if already running
        System.out.println("cancel(true): " + cancelled + ", isCancelled(): " + future.isCancelled());
    }
}
