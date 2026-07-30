package multithreading.basics;

/**
 * interrupt() - a cooperative cancellation signal, not a forceful kill. A thread BLOCKED in
 * sleep()/wait()/join() wakes up immediately with an InterruptedException; a thread that's just
 * running a loop must check Thread.currentThread().isInterrupted() itself to notice and stop.
 */
public class ThreadInterrupt {

    public static void main(String[] args) throws InterruptedException {
        interruptingASleepingThread();
        interruptingABusyLoop();
    }

    private static void interruptingASleepingThread() throws InterruptedException {
        Thread sleeper = new Thread(() -> {
            try {
                Thread.sleep(5000); // would sleep 5s, but gets interrupted almost immediately below
            } catch (InterruptedException e) {
                System.out.println("sleeper woke up early via InterruptedException");
            }
        });
        sleeper.start();
        Thread.sleep(100); // let it actually get into sleep() first
        sleeper.interrupt();
        sleeper.join();
    }

    private static void interruptingABusyLoop() throws InterruptedException {
        Thread looper = new Thread(() -> {
            int iterations = 0;
            // No blocking call to wake up here - the loop MUST check the flag itself to ever stop.
            while (!Thread.currentThread().isInterrupted()) {
                iterations++;
            }
            System.out.println("looper noticed the interrupt after ~" + iterations + " iterations");
        });
        looper.start();
        Thread.sleep(50); // let it spin for a bit
        looper.interrupt();
        looper.join();
    }
}
