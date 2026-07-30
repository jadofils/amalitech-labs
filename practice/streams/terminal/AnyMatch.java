package streams.terminal;

import java.util.Arrays;
import java.util.List;

/** anyMatch() - true if at least one element matches; terminal, short-circuits on the first hit. */
public class AnyMatch {
    public static void matchingAny() {
        List<Integer> ages = Arrays.asList(25, 31, 19, 42);

        boolean hasMinor = ages.stream()
                .anyMatch(n -> n < 18);

        System.out.println(hasMinor);
    }

    public static void main(String[] args) {
        matchingAny();
    }
}
