package collections.lists;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

/** The List interface's core contract - an ordered, index-accessible, duplicate-allowing collection. Backed here by ArrayList. */
public class List {

    public static void main(String[] args) {
        adding();
        gettingAndSetting();
        removing();
        searching();
        sortingAndSlicing();
        iteratingSafely();
    }

    private static void adding() {
        java.util.List<String> names = new ArrayList<>();
        names.add("Alice");
        names.add("Carol");
        names.add(1, "Bob"); // insert at index 1, shifting Carol right
        System.out.println("add: " + names);
    }

    private static void gettingAndSetting() {
        java.util.List<String> names = new ArrayList<>(java.util.List.of("Alice", "Bob", "Carol"));
        System.out.println("get(0): " + names.get(0));

        String previous = names.set(1, "Bianca"); // replaces, returns the old value
        System.out.println("set(1, Bianca), replaced: " + previous + " -> " + names);
    }

    private static void removing() {
        // The classic List<Integer> gotcha: remove(int) is by index, remove(Object) is by value.
        java.util.List<Integer> numbers = new ArrayList<>(java.util.List.of(10, 20, 30));
        numbers.remove(1);                   // index 1 -> removes 20
        System.out.println("remove(1) [by index]: " + numbers);
        numbers.remove(Integer.valueOf(30)); // value 30 -> removes wherever it is
        System.out.println("remove(Integer.valueOf(30)) [by value]: " + numbers);

        java.util.List<Integer> ages = new ArrayList<>(java.util.List.of(25, 31, 19, 42, 8));
        ages.removeIf(n -> n < 20); // removes every match in one safe pass
        System.out.println("removeIf(< 20): " + ages);
    }

    private static void searching() {
        java.util.List<String> names = java.util.List.of("Alice", "Bob", "Carol", "Bob");
        System.out.println("contains(Bob): " + names.contains("Bob"));
        System.out.println("indexOf(Bob): " + names.indexOf("Bob"));
        System.out.println("lastIndexOf(Bob): " + names.lastIndexOf("Bob"));
        System.out.println("indexOf(Dave): " + names.indexOf("Dave"));
    }

    private static void sortingAndSlicing() {
        java.util.List<Integer> ages = new ArrayList<>(java.util.List.of(25, 31, 19, 42, 8));
        ages.sort(Comparator.reverseOrder());
        System.out.println("sort(reverseOrder): " + ages);

        // subList() is a VIEW, not a copy - edits to it write through to the original list.
        java.util.List<Integer> middle = ages.subList(1, 3);
        System.out.println("subList(1, 3): " + middle);
    }

    private static void iteratingSafely() {
        java.util.List<Integer> ages = new ArrayList<>(java.util.List.of(25, 31, 19, 42, 8));

        // Removing through the list directly during a for-each throws ConcurrentModificationException;
        // Iterator.remove() is the safe way.
        Iterator<Integer> it = ages.iterator();
        while (it.hasNext()) {
            if (it.next() < 20) {
                it.remove();
            }
        }
        System.out.println("iterator().remove() for < 20: " + ages);
    }
}
