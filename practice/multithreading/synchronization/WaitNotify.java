package multithreading.synchronization;

/**
 * Object.wait()/notify() - the original, low-level way for threads to coordinate: wait() releases
 * the monitor and blocks until another thread calls notify()/notifyAll() on that SAME object. Both
 * must be called from inside a synchronized block on that object, and wait() must sit in a while
 * loop (not if) to guard against spurious wakeups. A single-slot producer/consumer handoff below.
 */
public class WaitNotify {

    private final Object lock = new Object();
    private Integer slot = null; // null = empty, a value = full

    private void produce(int value) throws InterruptedException {
        synchronized (lock) {
            while (slot != null) {
                lock.wait(); // release the lock and sleep until notified that the slot freed up
            }
            slot = value;
            System.out.println("produced: " + value);
            lock.notify(); // wake the consumer, which is waiting for the slot to become full
        }
    }

    private int consume() throws InterruptedException {
        synchronized (lock) {
            while (slot == null) {
                lock.wait();
            }
            int value = slot;
            slot = null;
            System.out.println("consumed: " + value);
            lock.notify(); // wake the producer, which is waiting for the slot to become empty
            return value;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        WaitNotify handoff = new WaitNotify();

        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    handoff.produce(i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    handoff.consume();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
    }
}
