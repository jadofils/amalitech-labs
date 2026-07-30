package lambdas.methodrefs;

import java.util.List;
import java.util.function.Function;

/**
 * ClassName::instanceMethod (unbound) - no specific object yet; whatever argument the lambda
 * receives BECOMES the object the method is called on. Easy to confuse with a static method
 * reference since the syntax looks identical.
 */
public class InstanceMethodOfArbitraryObjectRef {

    public static void main(String[] args) {
        Function<String, Integer> length = String::length; // same as: s -> s.length() - `s` becomes the receiver

        System.out.println(length.apply("hello"));

        List.of("Bob", "Alice", "Zoe").stream()
                .map(String::toUpperCase) // each element in turn becomes the receiver of toUpperCase()
                .forEach(System.out::println);
    }
}
