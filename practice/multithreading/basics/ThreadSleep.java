package multithreading.basics;

/** Thread.sleep(ms) - pauses the CURRENT thread for at least ms milliseconds; it's a checked InterruptedException, not a RuntimeException. */
public class ThreadSleep {

    public static void main(String[] args) {
        System.out.println("before sleep: " + System.currentTimeMillis());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // sleep() throws if another thread interrupts this one while it's paused - restore the flag, don't swallow it.
            Thread.currentThread().interrupt();
        }
        System.out.println("after sleep: " + System.currentTimeMillis());
    }
}
