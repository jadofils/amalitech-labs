package multithreading.synchronization;

/**
 * synchronized on a method - only one thread at a time can execute ANY synchronized method on the
 * same object, because each holds that object's intrinsic lock (monitor) for the call's duration.
 * Without it, "read old value, add 1, write back" from two threads can interleave and lose updates.
 */
public class SynchronizedMethod {

    private int count = 0;

    public synchronized void increment() {
        count++; // not atomic by itself: a read, an add, and a write - synchronized makes the WHOLE thing atomic
    }

    public int getCount() {
        return count;
    }

    public static void main(String[] args) throws InterruptedException {
        SynchronizedMethod counter = new SynchronizedMethod();
        int threads = 10;
        int incrementsEach = 10_000;

        Thread[] workers = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(() -> {
                for (int j = 0; j < incrementsEach; j++) {
                    counter.increment();
                }
            });
            workers[i].start();
        }
        for (Thread t : workers) {
            t.join();
        }

        int expected = threads * incrementsEach;
        System.out.println("expected: " + expected + ", actual: " + counter.getCount() + " (synchronized -> always equal)");
    }
}
