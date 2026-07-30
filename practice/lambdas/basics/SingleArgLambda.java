package lambdas.basics;

import java.util.function.Function;

/** A single parameter's parentheses are optional: n -> ... is the same as (n) -> ... */
public class SingleArgLambda {

    public static void main(String[] args) {
        Function<Integer, Integer> square = n -> n * n;       // no parens
        Function<Integer, Integer> squareToo = (n) -> n * n;  // with parens - identical meaning

        System.out.println(square.apply(5));
        System.out.println(squareToo.apply(5));
    }
}
