package collections.lists;

import java.util.List;

/** ArrayList - a resizable array. O(1) random-access get()/set(), O(n) insert/remove in the middle (shifts elements). */
public class ArrayList {

    public static void main(String[] args) {
        randomAccessIsFast();
        capacityGrowsAutomatically();
        insertingInTheMiddleShifts();
    }

    private static void randomAccessIsFast() {
        List<Integer> ages = new java.util.ArrayList<>(List.of(25, 31, 19, 42, 8));
        // Backed by a real array, so get(index) is O(1) - it's just array[index] under the hood.
        System.out.println("get(2): " + ages.get(2));
    }

    private static void capacityGrowsAutomatically() {
        // An initial capacity is just a hint to avoid early resizing - size() still starts at 0.
        java.util.ArrayList<Integer> numbers = new java.util.ArrayList<>(2);
        System.out.println("size before adding: " + numbers.size());
        for (int i = 0; i < 5; i++) {
            numbers.add(i); // the backing array silently grows past its initial capacity as needed
        }
        System.out.println("after adding 5: " + numbers);

        numbers.trimToSize(); // ArrayList-specific: shrinks the backing array to exactly size()
        System.out.println("trimToSize(): " + numbers);
    }

    private static void insertingInTheMiddleShifts() {
        List<String> names = new java.util.ArrayList<>(List.of("Alice", "Carol"));
        // Every element from index 1 onward is copied one slot right to make room - O(n).
        names.add(1, "Bob");
        System.out.println("add(1, Bob): " + names);
    }
}
