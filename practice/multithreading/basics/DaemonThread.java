package multithreading.basics;

/**
 * setDaemon(true) - marks a thread as a background/daemon thread. The JVM exits once every
 * NON-daemon thread has finished, without waiting for daemon threads at all (even mid-execution).
 */
public class DaemonThread {

    public static void main(String[] args) throws InterruptedException {
        Thread daemon = new Thread(() -> {
            int i = 0;
            while (true) { // would run forever if the JVM waited for it
                System.out.println("daemon tick " + (i++));
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });

        daemon.setDaemon(true); // must be set BEFORE start() - throws IllegalThreadStateException after
        daemon.start();

        Thread.sleep(500); // let the daemon tick a couple of times
        System.out.println("main is done - JVM will exit now even though the daemon loop never finished");
        // No daemon.join() here on purpose: the whole point is the JVM does NOT wait for it.
    }
}
