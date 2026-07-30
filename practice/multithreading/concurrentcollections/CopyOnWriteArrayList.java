package multithreading.concurrentcollections;

/**
 * CopyOnWriteArrayList - every write (add/remove/set) copies the ENTIRE underlying array. Reads and
 * iteration never need a lock and never throw ConcurrentModificationException, because an iterator
 * just walks a frozen snapshot from the moment it was created - it simply won't see writes that
 * happen after that point. Best for read-heavy, write-rare use (e.g. listener lists).
 */
public class CopyOnWriteArrayList {

    public static void main(String[] args) {
        java.util.concurrent.CopyOnWriteArrayList<String> names =
                new java.util.concurrent.CopyOnWriteArrayList<>(java.util.List.of("Alice", "Bob"));

        // Mutating the list WHILE iterating - a plain ArrayList would throw ConcurrentModificationException here.
        for (String name : names) {
            System.out.println("iterating: " + name);
            names.add("Carol-from-inside-loop"); // safe - the iterator is on its own snapshot, unaffected by this
        }

        System.out.println("list after the loop (includes the additions): " + names);
    }
}
