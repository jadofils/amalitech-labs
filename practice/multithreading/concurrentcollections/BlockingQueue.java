package multithreading.concurrentcollections;

/**
 * BlockingQueue<T> - a queue where put() blocks if it's full (bounded capacity) and take() blocks
 * if it's empty, instead of throwing or returning a sentinel. This is what WaitNotify.java hand-rolls
 * with wait()/notify() for a single slot - a producer/consumer handoff, but built-in and thread-safe.
 */
public class BlockingQueue {

    public static void main(String[] args) throws InterruptedException {
        java.util.concurrent.BlockingQueue<Integer> queue = new java.util.concurrent.LinkedBlockingQueue<>(2); // capacity 2

        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    queue.put(i); // blocks here once the queue already holds 2 items
                    System.out.println("produced: " + i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    Thread.sleep(150); // consume slower than production, to actually trigger put() blocking above
                    int value = queue.take(); // blocks if the queue is empty
                    System.out.println("consumed: " + value);
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
