package lambdas.methodrefs;

import java.util.function.Function;

/** ClassName::staticMethod - a shorthand for a lambda that just calls a static method with the same argument(s). */
public class StaticMethodRef {

    public static void main(String[] args) {
        Function<String, Integer> parse = Integer::parseInt; // same as: s -> Integer.parseInt(s)
        System.out.println(parse.apply("42"));
    }
}
