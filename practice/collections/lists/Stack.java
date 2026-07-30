package collections.lists;

/** java.util.Stack - a legacy LIFO stack (extends Vector, so it's synchronized); ArrayDeque is the modern replacement. */
public class Stack {

    public static void main(String[] args) {
        pushingAndPopping();
        peekingAndSearching();
    }

    private static void pushingAndPopping() {
        java.util.Stack<Integer> stack = new java.util.Stack<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        System.out.println("after 3 pushes: " + stack);

        System.out.println("pop(): " + stack.pop() + " -> " + stack); // removes and returns the top
    }

    private static void peekingAndSearching() {
        java.util.Stack<Integer> stack = new java.util.Stack<>();
        stack.push(10);
        stack.push(20);
        stack.push(30);

        System.out.println("peek() (no removal): " + stack.peek() + " -> " + stack);
        System.out.println("empty(): " + stack.empty());

        // search() returns 1-based distance from the top, or -1 if absent.
        System.out.println("search(10): " + stack.search(10));
    }
}
