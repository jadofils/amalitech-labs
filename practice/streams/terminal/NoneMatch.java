package streams.terminal;

import java.util.Arrays;
import java.util.List;

/** noneMatch() - true only if no element matches; terminal, short-circuits on the first hit. */
public class NoneMatch {
    public static void matchingNone() {
        List<Integer> ages = Arrays.asList(25, 31, 19, 42);

        boolean noMinors = ages.stream()
                .noneMatch(n -> n < 18);

        System.out.println(noMinors);
    }

    public static void main(String[] args) {
        matchingNone();
    }
}
