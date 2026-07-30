package multithreading.executors;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * CompletableFuture - a Future you can CHAIN callbacks onto (thenApply/thenAccept/...) instead of
 * blocking on get() and processing the result yourself. Each stage runs once the previous one
 * completes, without the calling thread needing to block in between.
 */
public class CompletableFutureDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<String> pipeline = CompletableFuture
                .supplyAsync(() -> {                       // runs asynchronously, produces a value
                    sleepQuietly(100);
                    return 21;
                })
                .thenApply(n -> n * 2)                      // transforms the result: 21 -> 42
                .thenApply(n -> "the answer is " + n);       // transforms again: 42 -> "the answer is 42"

        System.out.println("blocking on get(): " + pipeline.get());

        // thenAccept: consume the result with a side effect instead of transforming it further (returns no value).
        CompletableFuture.supplyAsync(() -> "hello")
                .thenAccept(s -> System.out.println("thenAccept received: " + s))
                .join(); // like get(), but join() doesn't declare checked exceptions

        // thenCombine: merge two INDEPENDENT async pipelines once both are done.
        CompletableFuture<Integer> a = CompletableFuture.supplyAsync(() -> 10);
        CompletableFuture<Integer> b = CompletableFuture.supplyAsync(() -> 32);
        CompletableFuture<Integer> combined = a.thenCombine(b, Integer::sum);
        System.out.println("thenCombine(10, 32): " + combined.get());
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
