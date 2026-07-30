package filehandling.io;

import java.io.IOException;
import java.io.Serializable;

/** ObjectInputStream - deserializes bytes back into a live object via readObject(); the cast can throw ClassCastException, and a missing class throws ClassNotFoundException. */
public class ObjectInputStream {

    private record Person(String name, int age) implements Serializable {
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        java.io.File file = new java.io.File(System.getProperty("java.io.tmpdir"), "practice-objectinputstream-demo.dat");

        try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(new java.io.FileOutputStream(file))) {
            out.writeObject(new Person("Bob", 25)); // quick setup, not the thing being demoed
        }

        try (java.io.ObjectInputStream in = new java.io.ObjectInputStream(new java.io.FileInputStream(file))) {
            Person restored = (Person) in.readObject(); // readObject() returns Object - an explicit cast is always needed
            System.out.println("deserialized: " + restored);
        }

        file.delete();
    }
}
