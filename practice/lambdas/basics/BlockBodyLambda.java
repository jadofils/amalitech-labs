package lambdas.basics;

import java.util.function.Function;

/** A single expression is the implicit return value; a { } block body needs its own explicit return statement. */
public class BlockBodyLambda {

    public static void main(String[] args) {
        Function<Integer, String> expressionBody = n -> n % 2 == 0 ? "even" : "odd"; // no braces, no return

        Function<Integer, String> blockBody = n -> {          // braces -> a block body
            String parity = n % 2 == 0 ? "even" : "odd";      // can hold multiple statements
            return parity;                                    // return is required inside a block body
        };

        System.out.println(expressionBody.apply(4));
        System.out.println(blockBody.apply(7));
    }
}
