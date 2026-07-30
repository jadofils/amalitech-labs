package streams.boxing;

import java.util.Arrays;
import java.util.List;

/** mapToLong() - unboxes/converts a stream into a primitive LongStream. */
public class MapToLong {
    public static void unboxingToLong() {
        List<Integer> ages = Arrays.asList(25, 31, 19, 42);

        long total = ages.stream()
                .mapToLong(Integer::longValue)
                .sum();

        System.out.println(total);
    }

    public static void main(String[] args) {
        unboxingToLong();
    }
}
