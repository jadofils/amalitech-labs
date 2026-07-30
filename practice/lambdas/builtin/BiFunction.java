package lambdas.builtin;

/** BiFunction<T, U, R> - takes a T and a U, returns an R. Single abstract method: apply(T, U). */
public class BiFunction {

    public static void main(String[] args) {
        java.util.function.BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
        System.out.println("apply(3, 4): " + add.apply(3, 4));

        java.util.function.BiFunction<String, Integer, String> repeat = (text, times) -> text.repeat(times);
        System.out.println("apply(\"ab\", 3): " + repeat.apply("ab", 3));
    }
}
