package lambdas.builtin;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** java.util.concurrent.Callable<V> - like Runnable, but returns a V and its call() is allowed to throw a checked Exception. */
public class Callable {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        java.util.concurrent.Callable<Integer> answer = () -> 42;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Integer> future = executor.submit(answer); // Runnable can't do this - it has no return value
            System.out.println("submit(Callable).get(): " + future.get());
        } finally {
            executor.shutdown();
        }
    }
}
