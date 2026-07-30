package streams.intermediate;

import java.util.Arrays;
import java.util.List;

/** sorted() - orders elements (natural order, or a given Comparator); lazy, returns a Stream. */
public class Sorted {
    public static void sorting() {
        List<Integer> ages = Arrays.asList(25, 31, 19, 42, 19, 8, 31, 55, 42);

        ages.stream()
                .sorted()
                .forEach(System.out::println);
    }

    public static void main(String[] args) {
        sorting();
    }
}
