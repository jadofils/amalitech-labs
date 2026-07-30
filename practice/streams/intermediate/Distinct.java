package streams.intermediate;

import java.util.Arrays;
import java.util.List;

/** distinct() - removes duplicate elements (using equals()); lazy, returns a Stream. */
public class Distinct {
    public static void distinction() {
        List<Integer> ages = Arrays.asList(25, 31, 19, 42, 19, 8, 31, 55, 42);
        ages.stream()
                .distinct()
                .forEach(System.out::println);
    }

    public static void main(String[] args) {
        distinction();
    }
}
