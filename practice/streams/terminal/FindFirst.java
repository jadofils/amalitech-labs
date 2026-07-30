package streams.terminal;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** findFirst() - returns the first element as an Optional, respecting encounter order; terminal, short-circuits. */
public class FindFirst {
    public static void findingFirst() {
        List<Integer> ages = Arrays.asList(25, 31, 19, 42);

        Optional<Integer> firstAdult = ages.stream()
                .filter(n -> n >= 18)
                .findFirst();

        System.out.println(firstAdult.orElse(-1));
    }

    public static void main(String[] args) {
        findingFirst();
    }
}
