package lambdas.builtin;

/** Supplier<T> - takes nothing, returns a T. Single abstract method: get(). Useful for lazy/deferred value production. */
public class Supplier {

    public static void main(String[] args) {
        java.util.function.Supplier<String> greeting = () -> "Hello, generated lazily!";
        System.out.println(greeting.get());

        // Classic use: pass a Supplier so the (possibly expensive) value is only computed if actually needed.
        java.util.function.Supplier<Double> expensiveDefault = () -> {
            System.out.println("computing the expensive default...");
            return 42.0;
        };
        System.out.println(orElseCompute(null, expensiveDefault));
    }

    private static double orElseCompute(Double value, java.util.function.Supplier<Double> fallback) {
        return value != null ? value : fallback.get();
    }
}
