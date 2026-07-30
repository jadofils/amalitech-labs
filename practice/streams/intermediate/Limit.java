package streams.intermediate;

import java.util.Arrays;
import java.util.List;

/** limit(n) - truncates the stream to at most n elements; lazy, returns a Stream. */
public class Limit {
    public static void limiting() {
        List<Integer> ages = Arrays.asList(25, 31, 19, 42, 19, 8, 31, 55, 42);

        ages.stream()
                .limit(3)
                .forEach(System.out::println);
    }

    public static void main(String[] args) {
        limiting();
    }
}
