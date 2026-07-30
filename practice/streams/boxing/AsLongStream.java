package streams.boxing;

import java.util.stream.IntStream;

/** asLongStream() - widens a primitive IntStream into a LongStream (int -> long, no boxing involved). */
public class AsLongStream {
    public static void wideningToLong() {
        long total = IntStream.of(25, 31, 19, 42)
                .asLongStream()
                .sum();

        System.out.println(total);
    }

    public static void main(String[] args) {
        wideningToLong();
    }
}
