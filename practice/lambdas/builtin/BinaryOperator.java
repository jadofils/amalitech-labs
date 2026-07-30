package lambdas.builtin;

/** BinaryOperator<T> - a BiFunction<T, T, T>: both inputs and the output are the same type. Single abstract method: apply(T, T). */
public class BinaryOperator {

    public static void main(String[] args) {
        java.util.function.BinaryOperator<Integer> max = (a, b) -> a > b ? a : b;
        System.out.println("apply(3, 7): " + max.apply(3, 7));

        // Because it's a specialized BiFunction<T, T, T>, it plugs directly into Stream.reduce(BinaryOperator<T>).
        java.util.List<Integer> numbers = java.util.List.of(3, 7, 2, 9, 4);
        int biggest = numbers.stream().reduce(Integer.MIN_VALUE, max);
        System.out.println("reduce(max): " + biggest);
    }
}
