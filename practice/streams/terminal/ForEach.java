package streams.terminal;

import java.util.Arrays;
import java.util.List;

/** forEach() - runs an action on every element; terminal, eager, no result. */
public class ForEach {
    public static void iterating() {
        List<Integer> ages = Arrays.asList(25, 31, 19, 42);

        ages.stream()
                .forEach(n -> System.out.println("age: " + n));
    }

    public static void main(String[] args) {
        iterating();
    }
}
