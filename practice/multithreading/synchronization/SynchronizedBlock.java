package multithreading.synchronization;

/**
 * synchronized(lockObject) { ... } - locks only the critical section, not the whole method, and lets
 * you choose exactly which object's monitor to hold (useful when a method does unrelated work too).
 */
public class SynchronizedBlock {

    private final Object lock = new Object();
    private int unsafeCount = 0;
    private int safeCount = 0;

    private void incrementUnsafe() {
        unsafeCount++; // no lock at all - a race condition
    }

    private void incrementSafe() {
        synchronized (lock) {
            safeCount++; // only this line needs protecting, not the whole method
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SynchronizedBlock demo = new SynchronizedBlock();
        int threads = 10;
        int incrementsEach = 10_000;
        int expected = threads * incrementsEach;

        Thread[] workers = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(() -> {
                for (int j = 0; j < incrementsEach; j++) {
                    demo.incrementUnsafe();
                    demo.incrementSafe();
                }
            });
            workers[i].start();
        }
        for (Thread t : workers) {
            t.join();
        }

        System.out.println("expected: " + expected);
        System.out.println("unsafeCount (no lock, likely < expected - lost updates): " + demo.unsafeCount);
        System.out.println("safeCount (synchronized block, always == expected): " + demo.safeCount);
    }
}
