package filehandling.io;

import java.io.IOException;
import java.io.Serializable;

/** ObjectOutputStream - serializes a whole object graph to bytes via writeObject(); the object (and everything it references) must implement Serializable. */
public class ObjectOutputStream {

    private record Person(String name, int age) implements Serializable {
    }

    public static void main(String[] args) throws IOException {
        java.io.File file = new java.io.File(System.getProperty("java.io.tmpdir"), "practice-objectoutputstream-demo.dat");

        Person original = new Person("Alice", 30);
        try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(new java.io.FileOutputStream(file))) {
            out.writeObject(original); // serializes the record's full state, not just a toString()
        }

        System.out.println("serialized " + original + " to a " + file.length() + "-byte file");
        file.delete();
    }
}
