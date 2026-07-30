package streams.terminal;

import java.util.Arrays;
import java.util.List;

/** toArray() - dumps the stream's elements into a new array; terminal, eager. */
public class ToArray {
    public static void arraying() {
        List<Integer> ages = Arrays.asList(25, 31, 19, 42);

        Integer[] agesArray = ages.stream()
                .toArray(Integer[]::new);

        System.out.println(Arrays.toString(agesArray));
    }

    public static void main(String[] args) {
        arraying();
    }
}
