package lambdas.custom;

import java.util.function.Function;

/**
 * A custom functional interface - java.util.function has no 3-argument Function, so this fills
 * that real gap. @FunctionalInterface isn't required, but it makes the intent explicit and makes
 * the compiler enforce exactly one abstract method (adding a second one here would be a compile
 * error). A default method is still allowed - a functional interface just needs exactly ONE
 * ABSTRACT method, not exactly one method overall.
 */
@FunctionalInterface
public interface TriFunction<A, B, C, R> {

    R apply(A a, B b, C c);

    default <V> TriFunction<A, B, C, V> andThen(Function<? super R, ? extends V> after) {
        return (a, b, c) -> after.apply(apply(a, b, c));
    }

    static void main(String[] args) {
        TriFunction<Integer, Integer, Integer, Integer> sumOfThree = (a, b, c) -> a + b + c;
        System.out.println("apply(1, 2, 3): " + sumOfThree.apply(1, 2, 3));

        TriFunction<Integer, Integer, Integer, String> sumAsText = sumOfThree.andThen(sum -> "sum = " + sum);
        System.out.println("andThen(...): " + sumAsText.apply(1, 2, 3));
    }
}
