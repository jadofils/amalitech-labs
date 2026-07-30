package lambdas.basics;

import java.util.function.BinaryOperator;

/** Two or more parameters always require parentheses: (a, b) -> ... */
public class MultiArgLambda {

    public static void main(String[] args) {
        BinaryOperator<Integer> add = (a, b) -> a + b;

        System.out.println(add.apply(3, 4));
    }
}
