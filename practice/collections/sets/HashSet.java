package collections.sets;

import java.util.List;

/** HashSet - backed by a hash table. O(1) average add/remove/contains, but iteration order is unspecified and can change. */
public class HashSet {

    private record Point(int x, int y) {
    } // a record gets equals()/hashCode() for free, based on its fields

    public static void main(String[] args) {
        orderIsNotGuaranteed();
        equalsAndHashCodeDecideDuplicates();
    }

    private static void orderIsNotGuaranteed() {
        java.util.HashSet<String> names = new java.util.HashSet<>(List.of("Zoe", "Alice", "Mike"));
        // Insertion order ("Zoe", "Alice", "Mike") is NOT preserved - iteration order depends on hash bucket layout.
        System.out.println("iteration order (unspecified): " + names);
    }

    private static void equalsAndHashCodeDecideDuplicates() {
        // Two DIFFERENT Point instances that are equal() (and have the same hashCode()) count as one entry.
        java.util.HashSet<Point> points = new java.util.HashSet<>();
        points.add(new Point(1, 2));
        points.add(new Point(1, 2)); // a distinct object, but equals() says it's a duplicate
        System.out.println("size after adding two equal() points: " + points.size());
    }
}
