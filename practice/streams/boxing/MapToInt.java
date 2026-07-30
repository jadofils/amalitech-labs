package streams.boxing;

import java.util.Arrays;
import java.util.List;

/** mapToInt() - unboxes a Stream<Integer> (or maps any type) into a primitive IntStream. */
public class MapToInt {
    public static void unboxingToInt() {
        List<Integer> ages = Arrays.asList(25, 31, 19, 42);

        int total = ages.stream()
                .mapToInt(Integer::intValue)
                .sum();

        System.out.println(total);
    }

    public static void main(String[] args) {
        unboxingToInt();
    }
}
