package streams.boxing;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** boxed() - wraps each primitive int into an Integer, turning an IntStream into a Stream<Integer>. */
public class Boxed {
    public static void boxingToObject() {
        List<Integer> ages = IntStream.of(25, 31, 19, 42)
                .boxed()
                .collect(Collectors.toList());

        System.out.println(ages);
    }

    public static void main(String[] args) {
        boxingToObject();
    }
}
