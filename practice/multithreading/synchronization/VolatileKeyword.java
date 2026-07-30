package multithreading.synchronization;

/**
 * volatile guarantees VISIBILITY across threads (every read sees the latest write - no per-thread
 * caching, no instruction reordering around it), but NOT atomicity - "count++" on a volatile int is
 * still a read-modify-write race (see AtomicInteger for that problem's actual fix).
 *
 * Without volatile here, running would be a plain field: the JIT compiler is legally allowed to
 * cache its value in a register and never re-read it, so the loop below could spin forever even
 * after another thread sets it to false - a real, JIT-dependent bug, not just a theoretical one.
 */
public class VolatileKeyword {

    private volatile boolean running = true;

    public static void main(String[] args) throws InterruptedException {
        VolatileKeyword demo = new VolatileKeyword();

        Thread worker = new Thread(() -> {
            long iterations = 0;
            while (demo.running) { // without volatile, this read could be cached and never see the flip below
                iterations++;
            }
            System.out.println("worker stopped after ~" + iterations + " iterations");
        });
        worker.start();

        Thread.sleep(200); // let it spin for a bit
        demo.running = false; // this write must become visible to the worker thread promptly
        worker.join(2000);
        System.out.println("worker still alive after 2s? " + worker.isAlive() + " (volatile -> should be false)");
    }
}
