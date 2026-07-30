package multithreading.executors;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** ScheduledExecutorService - runs a task once after a delay, or repeatedly on a fixed schedule. */
public class ScheduledExecutorServiceDemo {

    public static void main(String[] args) throws InterruptedException {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.schedule(() -> System.out.println("ran once, after a 200ms delay"), 200, TimeUnit.MILLISECONDS);

        AtomicInteger runs = new AtomicInteger();
        // Repeats every 150ms, measured from the START of each run (not from when the previous one finished).
        scheduler.scheduleAtFixedRate(
                () -> System.out.println("periodic run #" + runs.incrementAndGet()),
                0, 150, TimeUnit.MILLISECONDS
        );

        Thread.sleep(600); // let it fire a handful of times
        scheduler.shutdown(); // MUST explicitly stop it - a periodic task never ends on its own
        System.out.println("stopped after " + runs.get() + " periodic runs");
    }
}
