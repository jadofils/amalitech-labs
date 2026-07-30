package multithreading.concurrentcollections;

/**
 * ConcurrentHashMap - a thread-safe HashMap that doesn't need external synchronization and, unlike
 * a plain HashMap, tolerates concurrent reads/writes without throwing ConcurrentModificationException.
 * It also adds atomic compound operations (computeIfAbsent, merge) that would otherwise need a lock.
 */
public class ConcurrentHashMap {

    public static void main(String[] args) throws InterruptedException {
        concurrentUpdatesAreSafe();
        atomicCompoundOperations();
    }

    private static void concurrentUpdatesAreSafe() throws InterruptedException {
        java.util.concurrent.ConcurrentHashMap<String, Integer> wordCounts = new java.util.concurrent.ConcurrentHashMap<>();
        String[] words = {"a", "b", "a", "c", "b", "a"};

        Thread[] workers = new Thread[10];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Thread(() -> {
                for (String word : words) {
                    wordCounts.merge(word, 1, Integer::sum); // atomic: no lost updates across threads
                }
            });
            workers[i].start();
        }
        for (Thread t : workers) {
            t.join();
        }
        System.out.println("counts after 10 concurrent threads (always consistent): " + wordCounts);
    }

    private static void atomicCompoundOperations() {
        java.util.concurrent.ConcurrentHashMap<String, Integer> map = new java.util.concurrent.ConcurrentHashMap<>();
        // putIfAbsent: atomically "insert only if missing" - no separate containsKey()+put() race window.
        map.putIfAbsent("a", 1);
        map.putIfAbsent("a", 2); // ignored - "a" is already present
        System.out.println("putIfAbsent (2nd call ignored): " + map);
    }
}
