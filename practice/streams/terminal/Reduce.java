package streams.terminal;

import java.util.Arrays;
import java.util.List;

/** reduce() - combines all elements into one result using an accumulator; terminal, eager. */
public class Reduce {
    public static void reducing() {
        List<Integer> ages = Arrays.asList(2, 3, 4, 5, 6, 7, 8, 9, 6, 3, 4, 5, 6, 8, 45, 2);
        int sum = ages.stream()
                .reduce(0, (a, b) -> a + b);
        System.out.println(sum);
    }

    public static void main(String[] args) {
        reducing();
    }
}
