package streams.terminal;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** max(comparator) - the largest element per the given Comparator, as an Optional; terminal, eager. */
public class Max {
    public static void findingMax() {
        List<Integer> ages = Arrays.asList(25, 31, 19, 42);

        Optional<Integer> oldest = ages.stream()
                .max(Comparator.naturalOrder());

        System.out.println(oldest.orElse(-1));
    }

    public static void main(String[] args) {
        findingMax();
    }
}
