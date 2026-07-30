package streams.intermediate;

import java.util.Arrays;
import java.util.List;

/** skip(n) - discards the first n elements; lazy, returns a Stream. */
public class Skip {
    public static void skipping() {
        List<Integer> ages = Arrays.asList(25, 31, 19, 42, 19, 8, 31, 55, 42);

        ages.stream()
                .skip(3)
                .forEach(System.out::println);
    }

    public static void main(String[] args) {
        skipping();
    }
}
