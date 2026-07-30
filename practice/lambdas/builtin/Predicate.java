package lambdas.builtin;

/** Predicate<T> - takes a T, returns a boolean. Single abstract method: test(T). Combinable via and()/or()/negate(). */
public class Predicate {

    public static void main(String[] args) {
        testing();
        combining();
    }

    private static void testing() {
        java.util.function.Predicate<Integer> isEven = n -> n % 2 == 0;
        System.out.println("test(4): " + isEven.test(4));
        System.out.println("test(5): " + isEven.test(5));
    }

    private static void combining() {
        java.util.function.Predicate<Integer> isEven = n -> n % 2 == 0;
        java.util.function.Predicate<Integer> isPositive = n -> n > 0;

        System.out.println("isEven.and(isPositive).test(-4): " + isEven.and(isPositive).test(-4));
        System.out.println("isEven.or(isPositive).test(-4): " + isEven.or(isPositive).test(-4));
        System.out.println("isEven.negate().test(4): " + isEven.negate().test(4));
    }
}
