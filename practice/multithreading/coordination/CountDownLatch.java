package multithreading.coordination;

/**
 * CountDownLatch(n) - a one-shot gate: countDown() decrements the count, await() blocks until it
 * reaches 0. Once it hits 0 it stays open forever (unlike CyclicBarrier, it can't be reset/reused).
 * Classic use: make the main thread wait until N worker threads have all finished their setup phase.
 */
public class CountDownLatch {

    public static void main(String[] args) throws InterruptedException {
        int workerCount = 3;
        java.util.concurrent.CountDownLatch readyLatch = new java.util.concurrent.CountDownLatch(workerCount);

        for (int i = 1; i <= workerCount; i++) {
            int workerId = i;
            new Thread(() -> {
                sleepQuietly(workerId * 100L); // each worker "gets ready" at a different time
                System.out.println("worker " + workerId + " is ready");
                readyLatch.countDown(); // one fewer thread left to wait for
            }).start();
        }

        System.out.println("main waiting for all " + workerCount + " workers via await()...");
        readyLatch.await(); // blocks until the count reaches 0
        System.out.println("main continues - ALL workers are guaranteed ready now");
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
