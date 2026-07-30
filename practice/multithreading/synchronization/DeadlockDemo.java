package multithreading.synchronization;

/**
 * The classic AB-BA deadlock: threadA locks A then waits for B; threadB locks B then waits for A -
 * each holds what the other needs, so neither ever proceeds. The fix is always the same: every
 * thread must acquire shared locks in the SAME global order (both A-then-B, never one thread doing
 * B-then-A) - see fixedNoDeadlock() below.
 *
 * The deadlocked threads here are marked daemon and joined with a timeout specifically so this
 * demo itself can finish and the JVM can exit, instead of hanging forever like a real deadlock would.
 */
public class DeadlockDemo {

    public static void main(String[] args) throws InterruptedException {
        deadlockProne();
        fixedNoDeadlock();
    }

    private static void deadlockProne() throws InterruptedException {
        Object lockA = new Object();
        Object lockB = new Object();

        Thread threadA = new Thread(() -> {
            synchronized (lockA) {
                sleepQuietly(100); // give threadB time to grab lockB first
                synchronized (lockB) {
                    System.out.println("threadA got both locks (won't happen if deadlocked)");
                }
            }
        });
        Thread threadB = new Thread(() -> {
            synchronized (lockB) { // opposite order from threadA - this is the bug
                sleepQuietly(100);
                synchronized (lockA) {
                    System.out.println("threadB got both locks (won't happen if deadlocked)");
                }
            }
        });

        threadA.setDaemon(true); // so the JVM can still exit even if these two never finish
        threadB.setDaemon(true);
        threadA.start();
        threadB.start();

        threadA.join(1000);
        threadB.join(1000);
        System.out.println("deadlockProne: threadA still stuck? " + threadA.isAlive() + ", threadB still stuck? " + threadB.isAlive());
    }

    private static void fixedNoDeadlock() throws InterruptedException {
        Object lockA = new Object();
        Object lockB = new Object();

        Runnable acquireInOrder = () -> {
            synchronized (lockA) { // BOTH threads always take lockA first, lockB second
                sleepQuietly(50);
                synchronized (lockB) {
                    System.out.println(Thread.currentThread().getName() + " got both locks");
                }
            }
        };

        Thread threadA = new Thread(acquireInOrder);
        Thread threadB = new Thread(acquireInOrder);
        threadA.start();
        threadB.start();
        threadA.join();
        threadB.join();
        System.out.println("fixedNoDeadlock: both threads completed - consistent lock ordering prevents the cycle");
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
