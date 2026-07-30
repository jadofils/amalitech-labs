package streams.intermediate;

import java.util.Arrays;
import java.util.List;

/** map() - transforms each element 1-to-1 into a new value; lazy, returns a Stream. */
public class Map {
    public static void mapping() {
        List<String> names = Arrays.asList("alice", "bob", "carol");

        names.stream()
                .map(String::toUpperCase)
                .forEach(System.out::println);
    }

    public static void main(String[] args) {
        mapping();
    }
}
