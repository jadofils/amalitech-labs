package lambdas.builtin;

/** UnaryOperator<T> - a Function<T, T>: input and output are the same type. Single abstract method: apply(T). */
public class UnaryOperator {

    public static void main(String[] args) {
        java.util.function.UnaryOperator<Integer> doubleIt = n -> n * 2;
        System.out.println("apply(5): " + doubleIt.apply(5));

        // Because it's just a specialized Function<T, T>, it plugs directly into a List.replaceAll(UnaryOperator<T>).
        java.util.List<Integer> numbers = new java.util.ArrayList<>(java.util.List.of(1, 2, 3));
        numbers.replaceAll(doubleIt);
        System.out.println("replaceAll(doubleIt): " + numbers);
    }
}
