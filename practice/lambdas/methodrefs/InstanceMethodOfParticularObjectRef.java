package lambdas.methodrefs;

import java.util.function.Supplier;

/** particularObject::instanceMethod - bound to one specific, already-existing object; the lambda captures that object. */
public class InstanceMethodOfParticularObjectRef {

    public static void main(String[] args) {
        String greeting = "hello there";

        Supplier<String> shout = greeting::toUpperCase; // same as: () -> greeting.toUpperCase()
        System.out.println(shout.get());
    }
}
