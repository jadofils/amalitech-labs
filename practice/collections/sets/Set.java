package collections.sets;

/** The Set interface's core contract - no duplicates (by equals()), no index access. Backed here by HashSet. */
public class Set {

    public static void main(String[] args) {
        addingAndRemoving();
        setAlgebra();
        immutableSet();
    }

    private static void addingAndRemoving() {
        java.util.Set<String> names = new java.util.HashSet<>();
        System.out.println("add(Alice) [new]: " + names.add("Alice"));
        System.out.println("add(Alice) [dup]: " + names.add("Alice")); // false - already present, ignored
        System.out.println("contains(Alice): " + names.contains("Alice"));
        System.out.println("remove(Alice): " + names.remove("Alice"));
        System.out.println("remove(Alice) again: " + names.remove("Alice")); // false - wasn't there
    }

    private static void setAlgebra() {
        java.util.Set<Integer> a = new java.util.HashSet<>(java.util.Set.of(1, 2, 3, 4));
        java.util.Set<Integer> b = java.util.Set.of(3, 4, 5);

        java.util.Set<Integer> union = new java.util.HashSet<>(a);
        union.addAll(b);
        System.out.println("union (addAll): " + union);

        java.util.Set<Integer> intersection = new java.util.HashSet<>(a);
        intersection.retainAll(b);
        System.out.println("intersection (retainAll): " + intersection);

        java.util.Set<Integer> difference = new java.util.HashSet<>(a);
        difference.removeAll(b);
        System.out.println("difference a-b (removeAll): " + difference);
    }

    private static void immutableSet() {
        java.util.Set<String> names = java.util.Set.of("Alice", "Bob"); // duplicates would throw here, at creation
        try {
            names.add("Carol");
        } catch (UnsupportedOperationException e) {
            System.out.println("Set.of(...) is immutable: " + e.getClass().getSimpleName());
        }
    }
}
