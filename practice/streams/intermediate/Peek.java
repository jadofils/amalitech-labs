package streams.intermediate;

import java.util.Arrays;
import java.util.List;

/** peek() - runs a side-effect on each element without changing the stream; lazy, returns a Stream. */
public class Peek {
    public static void peeking() {
        List<Integer> ages = Arrays.asList(25, 31, 19, 42);

        int total = ages.stream()
                .peek(n -> System.out.println("saw: " + n))
                .mapToInt(Integer::intValue)
                .sum();

        System.out.println("total: " + total);
    }

    public static void main(String[] args) {
        peeking();
    }
}
