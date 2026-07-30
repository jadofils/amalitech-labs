package multithreading.coordination;

/**
 * CyclicBarrier(n) - makes n threads all wait at await() until every one of them has arrived, then
 * releases all of them at once - and unlike CountDownLatch, it's CYCLIC: it automatically resets and
 * can be reused for the next round. Good for lock-step phases where everyone must finish phase 1
 * before anyone starts phase 2.
 */
public class CyclicBarrier {

    public static void main(String[] args) {
        int workerCount = 3;
        java.util.concurrent.CyclicBarrier barrier = new java.util.concurrent.CyclicBarrier(
                workerCount,
                () -> System.out.println("--- everyone arrived: barrier tripped, starting next phase ---")
        );

        for (int i = 1; i <= workerCount; i++) {
            int workerId = i;
            new Thread(() -> runTwoPhases(workerId, barrier)).start();
        }
    }

    private static void runTwoPhases(int workerId, java.util.concurrent.CyclicBarrier barrier) {
        try {
            sleepQuietly(workerId * 100L);
            System.out.println("worker " + workerId + " finished phase 1");
            barrier.await(); // blocks here until all 3 workers have reached this point

            System.out.println("worker " + workerId + " starting phase 2");
            sleepQuietly(50);
            System.out.println("worker " + workerId + " finished phase 2");
            barrier.await(); // the SAME barrier is reused for a second round
        } catch (InterruptedException | java.util.concurrent.BrokenBarrierException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
