package multithreading.basics;

/**
 * Extending Thread and overriding run() - the original (pre-Runnable) way to define a thread's work.
 * Downside: Java has single inheritance, so a class that extends Thread can't extend anything else.
 */
public class ExtendingThread extends Thread {

    @Override
    public void run() {
        System.out.println("running on: " + Thread.currentThread().getName());
    }

    public static void main(String[] args) throws InterruptedException {
        ExtendingThread worker = new ExtendingThread();

        // Calling run() directly is just a normal method call on the current thread - no concurrency at all.
        System.out.println("calling run() directly (no new thread):");
        worker.run();

        // start() is what actually spins up a new OS-backed thread, which then calls run() on ITS OWN stack.
        System.out.println("calling start() (real new thread):");
        ExtendingThread realThread = new ExtendingThread();
        realThread.start();
        realThread.join(); // wait for it to finish so output ordering stays predictable for this demo
    }
}
