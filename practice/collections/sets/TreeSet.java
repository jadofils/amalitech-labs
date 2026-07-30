package collections.sets;

import java.util.Comparator;
import java.util.List;

/** TreeSet - backed by a red-black tree. Always sorted; O(log n) operations; adds navigation methods (NavigableSet). */
public class TreeSet {

    public static void main(String[] args) {
        alwaysSorted();
        navigating();
        customOrder();
    }

    private static void alwaysSorted() {
        java.util.TreeSet<Integer> ages = new java.util.TreeSet<>(List.of(31, 8, 42, 19, 25));
        System.out.println("iteration order (always sorted): " + ages);
    }

    private static void navigating() {
        java.util.TreeSet<Integer> ages = new java.util.TreeSet<>(List.of(8, 19, 25, 31, 42));

        System.out.println("first()/last(): " + ages.first() + " / " + ages.last());
        System.out.println("lower(25) [strictly <]: " + ages.lower(25));
        System.out.println("floor(25) [<=]: " + ages.floor(25));
        System.out.println("ceiling(25) [>=]: " + ages.ceiling(25));
        System.out.println("higher(25) [strictly >]: " + ages.higher(25));
        System.out.println("headSet(25) [< 25]: " + ages.headSet(25));
        System.out.println("tailSet(25) [>= 25]: " + ages.tailSet(25));
    }

    private static void customOrder() {
        java.util.TreeSet<String> byLength = new java.util.TreeSet<>(Comparator.comparingInt(String::length));
        byLength.addAll(List.of("Bob", "Alice", "Zoe", "Carol"));
        // TreeSet uses the comparator (not equals()) to decide duplicates: "Zoe" (len 3) and "Carol" (len 5)
        // each compare equal to an element already present ("Bob", "Alice"), so they're dropped as "duplicates".
        System.out.println("sorted by length (comparator ties = duplicates): " + byLength);
    }
}
