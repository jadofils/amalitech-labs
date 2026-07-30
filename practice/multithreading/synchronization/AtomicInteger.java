package multithreading.synchronization;

/**
 * java.util.concurrent.atomic.AtomicInteger - fixes the same "count++ isn't atomic" race as
 * SynchronizedMethod, but without ever taking a lock: incrementAndGet() uses a hardware
 * compare-and-swap (CAS) instruction in a retry loop instead, which is typically faster under contention.
 */
public class AtomicInteger {

    public static void main(String[] args) throws InterruptedException {
        java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger(0);
        int threads = 10;
        int incrementsEach = 10_000;

        Thread[] workers = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(() -> {
                for (int j = 0; j < incrementsEach; j++) {
                    counter.incrementAndGet(); // atomic: read, add 1, write - as one indivisible CAS-retry operation
                }
            });
            workers[i].start();
        }
        for (Thread t : workers) {
            t.join();
        }

        int expected = threads * incrementsEach;
        System.out.println("expected: " + expected + ", actual: " + counter.get() + " (lock-free -> always equal)");

        // compareAndSet: the primitive CAS operation itself - only writes if the current value matches the expected one.
        boolean swapped = counter.compareAndSet(expected, 0);
        System.out.println("compareAndSet(expected, 0): " + swapped + " -> now " + counter.get());
    }
}
