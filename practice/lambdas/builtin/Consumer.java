package lambdas.builtin;

/** Consumer<T> - takes a T, returns nothing (a side effect). Single abstract method: accept(T). Chainable via andThen(). */
public class Consumer {

    public static void main(String[] args) {
        java.util.function.Consumer<String> printIt = s -> System.out.println("plain: " + s);
        printIt.accept("hello");

        java.util.function.Consumer<String> printUpper = s -> System.out.println("upper: " + s.toUpperCase());
        // andThen: runs this Consumer, then the next one, on the same input
        printIt.andThen(printUpper).accept("world");
    }
}
