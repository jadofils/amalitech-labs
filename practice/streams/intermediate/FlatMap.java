package streams.intermediate;

import java.util.Arrays;
import java.util.List;

/** flatMap() - maps each element to its own Stream, then flattens all of them into one; lazy. */
public class FlatMap {
    public static void flatMapping() {
        List<List<Integer>> groups = Arrays.asList(
                Arrays.asList(1, 2, 3),
                Arrays.asList(4, 5),
                Arrays.asList(6, 7, 8, 9)
        );

        groups.stream()
                .flatMap(List::stream)
                .forEach(System.out::println);
    }

    public static void main(String[] args) {
        flatMapping();
    }
}
