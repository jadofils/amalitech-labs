package streams.terminal;

import java.util.Arrays;
import java.util.List;

/** count() - returns how many elements reached this point in the stream; terminal, eager. */
public class Count {
    public static void counting() {
        List<Integer> ages = Arrays.asList(25, 31, 19, 42, 19, 8, 31, 55, 42);

        long count = ages.stream()
                .filter(n -> n > 20)
                .count();

        System.out.println(count);
    }

    public static void main(String[] args) {
        counting();
    }
}
