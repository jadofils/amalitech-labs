package lambdas.basics;

/** A lambda with zero parameters still needs the empty parens: () -> ... */
public class NoArgLambda {

    public static void main(String[] args) {
        Runnable greet = () -> System.out.println("Hello from a no-arg lambda!");
        greet.run();
    }
}
