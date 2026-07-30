package streams.terminal;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** collect() - gathers elements into a container (List, Set, Map, ...) via a Collector; terminal, eager. */
public class Collect {
    public static void collecting() {
        List<Integer> ages = Arrays.asList(25, 31, 19, 42, 19);

        List<Integer> evenAges = ages.stream()
                .filter(n -> n % 2 == 0)
                .collect(Collectors.toList());

        System.out.println(evenAges);
    }

    public static void main(String[] args) {
        collecting();
    }
}
