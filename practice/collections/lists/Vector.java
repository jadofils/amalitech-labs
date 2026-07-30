package collections.lists;

import java.util.Enumeration;
import java.util.List;

/** java.util.Vector - a legacy synchronized dynamic array; behaves like ArrayList but every method is synchronized. */
public class Vector {

    public static void main(String[] args) {
        modernListStyle();
        legacyElementStyle();
    }

    private static void modernListStyle() {
        // Vector implements List, so the same methods you'd use on ArrayList work here too.
        List<String> names = new java.util.Vector<>(List.of("Alice", "Bob", "Carol"));
        System.out.println("get(1): " + names.get(1));
    }

    private static void legacyElementStyle() {
        // Predates the Collections Framework - its own parallel API: elementAt/firstElement/lastElement/elements().
        java.util.Vector<String> names = new java.util.Vector<>(List.of("Alice", "Bob", "Carol"));
        System.out.println("elementAt(0): " + names.elementAt(0));
        System.out.println("firstElement(): " + names.firstElement());
        System.out.println("lastElement(): " + names.lastElement());

        Enumeration<String> e = names.elements(); // the pre-Iterator way of walking a Vector
        while (e.hasMoreElements()) {
            System.out.println("enumeration: " + e.nextElement());
        }
    }
}
