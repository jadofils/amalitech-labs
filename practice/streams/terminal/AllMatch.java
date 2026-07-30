package streams.terminal;

import java.util.Arrays;
import java.util.List;

/** allMatch() - true only if every element matches; terminal, short-circuits on the first miss. */
public class AllMatch {
    public static void matchingAll() {
        List<Integer> ages = Arrays.asList(25, 31, 19, 42);

        boolean allAdults = ages.stream()
                .allMatch(n -> n >= 18);

        System.out.println(allAdults);
    }

    public static void main(String[] args) {
        matchingAll();
    }
}
