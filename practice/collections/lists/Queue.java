package collections.lists;

import java.util.LinkedList;

/** The Queue interface - FIFO. offer()/poll()/peek() return a sentinel (true/false or null) on failure; add()/remove()/element() throw instead. */
public class Queue {

    public static void main(String[] args) {
        fifoOrder();
        failSoftVsFailFast();
    }

    private static void fifoOrder() {
        java.util.Queue<Integer> queue = new LinkedList<>(); // programming to the interface; ArrayDeque is another common choice
        queue.offer(1);
        queue.offer(2);
        queue.offer(3);
        System.out.println("after 3 offers: " + queue);

        System.out.println("poll() (removes head): " + queue.poll() + " -> " + queue);
        System.out.println("peek() (no removal): " + queue.peek() + " -> " + queue);
    }

    private static void failSoftVsFailFast() {
        java.util.Queue<Integer> empty = new LinkedList<>();

        System.out.println("poll() on empty (returns null): " + empty.poll());
        System.out.println("peek() on empty (returns null): " + empty.peek());

        try {
            empty.remove(); // remove()/element() throw instead of returning null/false
        } catch (java.util.NoSuchElementException e) {
            System.out.println("remove() on empty throws: " + e.getClass().getSimpleName());
        }
    }
}
