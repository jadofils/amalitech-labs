package streams.boxing;

import java.util.Arrays;
import java.util.List;

/** mapToDouble() - unboxes/converts a stream into a primitive DoubleStream. */
public class MapToDouble {
    public static void unboxingToDouble() {
        List<Integer> ages = Arrays.asList(25, 31, 19, 42);

        double average = ages.stream()
                .mapToDouble(Integer::doubleValue)
                .average()
                .orElse(0.0);

        System.out.println(average);
    }

    public static void main(String[] args) {
        unboxingToDouble();
    }
}
