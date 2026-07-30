package multithreading.basics;

/** join() - blocks the calling thread until the target thread finishes. Without it, thread completion order isn't guaranteed. */
public class ThreadJoin {

    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> {
            try {
                Thread.sleep(300); // simulate some work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("worker finished");
        });

        worker.start();
        System.out.println("main waiting for worker via join()...");
        worker.join(); // main blocks here until worker's run() returns
        System.out.println("main continues - worker is guaranteed done by now");

        // join(timeout) waits at most timeout ms, then returns anyway - useful to avoid waiting forever.
        Thread slowWorker = new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        slowWorker.setDaemon(true); // so this demo can exit immediately instead of waiting out the full 2s
        slowWorker.start();
        slowWorker.join(100);
        System.out.println("still alive after join(100ms)? " + slowWorker.isAlive());
    }
}
