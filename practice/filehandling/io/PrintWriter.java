package filehandling.io;

import java.io.IOException;
import java.nio.file.Files;

/**
 * PrintWriter - wraps another Writer to add println()/printf()/print(Object) convenience methods
 * (the same API System.out uses). Unlike most Writers, its methods never throw IOException - check
 * checkError() instead if you need to know something failed.
 */
public class PrintWriter {

    public static void main(String[] args) throws IOException {
        java.io.File file = new java.io.File(System.getProperty("java.io.tmpdir"), "practice-printwriter-demo.txt");

        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(file))) {
            writer.println("plain line");
            writer.printf("formatted: %d + %d = %d%n", 2, 2, 4);
            writer.print("no newline after this");
        }

        System.out.println(Files.readString(file.toPath()));
        file.delete();
    }
}
