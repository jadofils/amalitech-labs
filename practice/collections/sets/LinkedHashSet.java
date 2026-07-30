package collections.sets;

import java.util.List;

/** LinkedHashSet - a HashSet plus a doubly-linked list threading the entries, so iteration follows insertion order. */
public class LinkedHashSet {

    public static void main(String[] args) {
        preservesInsertionOrder();
    }

    private static void preservesInsertionOrder() {
        java.util.LinkedHashSet<String> names = new java.util.LinkedHashSet<>(List.of("Zoe", "Alice", "Mike"));
        System.out.println("LinkedHashSet (insertion order):  " + names);

        java.util.HashSet<String> plain = new java.util.HashSet<>(names);
        System.out.println("plain HashSet (order unspecified): " + plain);

        // Re-adding an existing element does NOT move it - insertion order means "first added", not "last touched".
        names.add("Alice");
        System.out.println("after re-adding Alice (unchanged position): " + names);
    }
}
