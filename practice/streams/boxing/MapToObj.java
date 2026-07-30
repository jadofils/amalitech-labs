package streams.boxing;

import java.util.stream.IntStream;

/** mapToObj() - maps a primitive IntStream back to a Stream<T> of objects. */
public class MapToObj {
    public static void mappingToObject() {
        IntStream.rangeClosed(1, 5)
                .mapToObj(n -> "item-" + n)
                .forEach(System.out::println);
    }

    public static void main(String[] args) {
        mappingToObject();
    }
}
