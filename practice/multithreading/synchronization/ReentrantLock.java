package multithreading.synchronization;

/**
 * java.util.concurrent.locks.ReentrantLock - an explicit alternative to synchronized: you call
 * lock()/unlock() yourself (ALWAYS unlock in a finally block), but gain tryLock() (non-blocking
 * attempts) and fairness options that synchronized doesn't offer. "Reentrant" means the same
 * thread can acquire it again while already holding it, without deadlocking itself.
 */
public class ReentrantLock {

    private final java.util.concurrent.locks.ReentrantLock lock = new java.util.concurrent.locks.ReentrantLock();
    private int count = 0;

    public void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock(); // MUST be in finally - an exception between lock() and unlock() would leave it locked forever otherwise
        }
    }

    private void reentrantCall() {
        lock.lock(); // acquiring it a 2nd time on the same thread - fine, holdCount just goes to 2
        try {
            System.out.println("holdCount while nested: " + lock.getHoldCount());
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ReentrantLock demo = new ReentrantLock();

        demo.lock.lock();
        try {
            System.out.println("holdCount before nesting: " + demo.lock.getHoldCount());
            demo.reentrantCall(); // same thread re-acquiring the same lock it already holds
        } finally {
            demo.lock.unlock();
        }

        // tryLock(): returns immediately (true/false) instead of blocking - useful to avoid waiting forever.
        boolean acquired = demo.lock.tryLock();
        System.out.println("tryLock() when free: " + acquired);
        if (acquired) {
            demo.lock.unlock();
        }

        int threads = 10;
        int incrementsEach = 10_000;
        Thread[] workers = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(() -> {
                for (int j = 0; j < incrementsEach; j++) {
                    demo.increment();
                }
            });
            workers[i].start();
        }
        for (Thread t : workers) {
            t.join();
        }
        System.out.println("expected: " + (threads * incrementsEach) + ", actual: " + demo.count);
    }
}
