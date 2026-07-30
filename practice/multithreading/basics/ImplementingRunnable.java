package multithreading.basics;

/**
 * Implementing Runnable, then handing it to a Thread - the preferred way over extending Thread,
 * since a class can implement Runnable while still extending something else. Also decouples "the
 * work" (Runnable) from "the thing that runs it" (Thread) - the same task could be handed to an
 * ExecutorService instead, with no changes to the Runnable itself.
 */
public class ImplementingRunnable implements Runnable {

    @Override
    public void run() {
        System.out.println("running on: " + Thread.currentThread().getName());
    }

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new ImplementingRunnable());
        thread.start();
        thread.join();

        // Just as often, the Runnable is a lambda instead of its own named class:
        Thread lambdaThread = new Thread(() -> System.out.println("lambda running on: " + Thread.currentThread().getName()));
        lambdaThread.start();
        lambdaThread.join();
    }
}
