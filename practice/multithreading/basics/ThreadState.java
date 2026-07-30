package multithreading.basics;

/** Thread.getState() - the 6 lifecycle states: NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED. */
public class ThreadState {

    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> {
            try {
                Thread.sleep(300); // will be TIMED_WAITING during this
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        System.out.println("before start(): " + worker.getState());       // NEW - created, never started

        worker.start();
        System.out.println("just after start(): " + worker.getState());   // RUNNABLE - running or ready to run

        Thread.sleep(50); // give it a moment to reach the sleep() call
        System.out.println("while sleeping inside: " + worker.getState()); // TIMED_WAITING - Thread.sleep(ms) with a bound

        worker.join();
        System.out.println("after join() returns: " + worker.getState()); // TERMINATED - run() has returned
    }
}
