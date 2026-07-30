package streams.boxing;

import java.util.stream.IntStream;

/** asDoubleStream() - widens a primitive IntStream into a DoubleStream (int -> double, no boxing involved). */
public class AsDoubleStream {
    public static void wideningToDouble() {
        double average = IntStream.of(25, 31, 19, 42)
                .asDoubleStream()
                .average()
                .orElse(0.0);

        System.out.println(average);
    }

    public static void main(String[] args) {
        wideningToDouble();
    }
}
