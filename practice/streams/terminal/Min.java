package streams.terminal;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** min(comparator) - the smallest element per the given Comparator, as an Optional; terminal, eager. */
public class Min {
    public static void findingMin() {
        List<Integer> ages = Arrays.asList(25, 31, 19, 42);

        Optional<Integer> youngest = ages.stream()
                .min(Comparator.naturalOrder());

        System.out.println(youngest.orElse(-1));
    }

    public static void main(String[] args) {
        findingMin();
    }
}
