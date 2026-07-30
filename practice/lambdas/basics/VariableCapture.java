package lambdas.basics;

import java.util.function.Supplier;

/**
 * A lambda can read a local variable from its enclosing scope only if that variable is
 * "effectively final" - never reassigned after initialization. Reassigning it would be a compile
 * error: "local variables referenced from a lambda expression must be final or effectively final".
 */
public class VariableCapture {

    public static void main(String[] args) {
        String name = "Alice"; // never reassigned -> effectively final, so the lambda below can capture it

        Supplier<String> greeting = () -> "Hello, " + name + "!";
        System.out.println(greeting.get());

        // Uncommenting the next line would make `name` NOT effectively final,
        // and the lambda above would then fail to compile:
        // name = "Bob";
    }
}
