package lambdas.methodrefs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** ClassName::new - a shorthand for a lambda that just calls a constructor with the same argument(s). */
public class ConstructorRef {

    public static void main(String[] args) {
        Supplier<List<String>> newList = ArrayList::new; // same as: () -> new ArrayList<>()

        List<String> names = newList.get();
        names.add("Alice");
        System.out.println(names);
    }
}
