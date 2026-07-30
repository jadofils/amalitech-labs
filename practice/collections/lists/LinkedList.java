package collections.lists;

import java.util.List;

/** LinkedList - a doubly-linked list. O(1) insert/remove at either end, O(n) random-access get() (must walk the links). */
public class LinkedList {

    public static void main(String[] args) {
        asAList();
        asADeque();
        asAStack();
        asAQueue();
    }

    private static void asAList() {
        List<String> names = new java.util.LinkedList<>(List.of("Alice", "Bob", "Carol"));
        // get(index) here means walking `index` links from whichever end is closer - O(n), unlike ArrayList's O(1).
        System.out.println("get(1): " + names.get(1));
    }

    private static void asADeque() {
        java.util.LinkedList<Integer> deque = new java.util.LinkedList<>(List.of(2, 3, 4));
        deque.addFirst(1); // O(1) - just relinks the head
        deque.addLast(5);  // O(1) - just relinks the tail
        System.out.println("addFirst/addLast: " + deque);

        System.out.println("getFirst/getLast: " + deque.getFirst() + " / " + deque.getLast());

        deque.removeFirst();
        deque.removeLast();
        System.out.println("after removeFirst/removeLast: " + deque);
    }

    private static void asAStack() {
        // push()/pop() treat the LinkedList as a LIFO stack, operating on the head.
        java.util.LinkedList<Integer> stack = new java.util.LinkedList<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        System.out.println("after 3 pushes: " + stack);
        System.out.println("pop(): " + stack.pop() + " -> " + stack);
    }

    private static void asAQueue() {
        // offer()/poll() treat the LinkedList as a FIFO queue, adding at the tail and removing from the head.
        java.util.LinkedList<Integer> queue = new java.util.LinkedList<>();
        queue.offer(1);
        queue.offer(2);
        queue.offer(3);
        System.out.println("after 3 offers: " + queue);
        System.out.println("poll(): " + queue.poll() + " -> " + queue);
    }
}
