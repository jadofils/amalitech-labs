package lambdas.builtin;

/** Function<T, R> - takes a T, returns an R. Single abstract method: apply(T). Composable via andThen()/compose(). */
public class Function {

    public static void main(String[] args) {
        applying();
        composing();
    }

    private static void applying() {
        java.util.function.Function<String, Integer> length = String::length;
        System.out.println("apply(\"hello\"): " + length.apply("hello"));
    }

    private static void composing() {
        java.util.function.Function<Integer, Integer> square = n -> n * n;
        java.util.function.Function<Integer, Integer> addOne = n -> n + 1;

        // andThen: run this function, THEN the argument -> square first, then addOne
        System.out.println("square.andThen(addOne).apply(3): " + square.andThen(addOne).apply(3)); // (3*3)+1 = 10

        // compose: run the argument FIRST, then this function -> addOne first, then square
        System.out.println("square.compose(addOne).apply(3): " + square.compose(addOne).apply(3)); // (3+1)^2 = 16
    }
}
