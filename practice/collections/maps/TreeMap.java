package collections.maps;

import java.util.Comparator;

/** TreeMap - backed by a red-black tree, keyed and sorted by key. O(log n) operations; adds navigation methods (NavigableMap). */
public class TreeMap {

    public static void main(String[] args) {
        alwaysSortedByKey();
        navigatingByKey();
        customKeyOrder();
    }

    private static void alwaysSortedByKey() {
        java.util.TreeMap<Integer, String> byAge = new java.util.TreeMap<>();
        byAge.put(31, "Bob");
        byAge.put(8, "Sam");
        byAge.put(19, "Amy");
        System.out.println("iteration order (sorted by key): " + byAge);
    }

    private static void navigatingByKey() {
        java.util.TreeMap<Integer, String> byAge = new java.util.TreeMap<>();
        byAge.put(8, "Sam");
        byAge.put(19, "Amy");
        byAge.put(25, "Ann");
        byAge.put(31, "Bob");

        System.out.println("firstKey()/lastKey(): " + byAge.firstKey() + " / " + byAge.lastKey());
        System.out.println("lowerKey(25) [strictly <]: " + byAge.lowerKey(25));
        System.out.println("floorKey(25) [<=]: " + byAge.floorKey(25));
        System.out.println("ceilingKey(25) [>=]: " + byAge.ceilingKey(25));
        System.out.println("higherKey(25) [strictly >]: " + byAge.higherKey(25));
        System.out.println("headMap(25) [key < 25]: " + byAge.headMap(25));
        System.out.println("tailMap(25) [key >= 25]: " + byAge.tailMap(25));
    }

    private static void customKeyOrder() {
        java.util.TreeMap<String, Integer> byLength = new java.util.TreeMap<>(Comparator.comparingInt(String::length));
        byLength.put("Bob", 1);
        byLength.put("Alice", 2);
        // Same comparator-decides-duplicates rule as TreeSet: "Zoe" (len 3) collides with "Bob" (len 3) as a key.
        byLength.put("Zoe", 3);
        System.out.println("sorted by key length (comparator ties overwrite): " + byLength);
    }
}
