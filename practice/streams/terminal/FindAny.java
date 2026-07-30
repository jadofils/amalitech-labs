package streams.terminal;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** findAny() - returns any matching element as an Optional; terminal, short-circuits (order not guaranteed, cheaper in parallel streams). */
public class FindAny {
    public static void findingAny() {
        List<Integer> ages = Arrays.asList(25, 31, 19, 42);

        Optional<Integer> anyAdult = ages.stream()
                .filter(n -> n >= 18)
                .findAny();

        System.out.println(anyAdult.orElse(-1));
    }

    public static void main(String[] args) {
        findingAny();
    }
}
