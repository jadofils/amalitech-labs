package streams.intermediate;

import java.util.Arrays;
import java.util.List;

/** filter() - keeps only elements matching a predicate; lazy, returns a Stream. */
public class Filter {
    public static void filtering() {
        List<Integer> ages = Arrays.asList(25, 31, 19, 42, 19, 8, 31, 55, 42);

        ages.stream()
                .filter(n -> n % 2 == 0)   // keep even numbers
                .forEach(System.out::println);
    }

    public static void main(String[] args) {
        filtering();
    }
}
