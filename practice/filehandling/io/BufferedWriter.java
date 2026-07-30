package filehandling.io;

import java.io.IOException;
import java.nio.file.Files;

/** BufferedWriter - wraps another Writer (usually a FileWriter) to batch writes in memory instead of hitting disk on every call. Adds newLine(). */
public class BufferedWriter {

    public static void main(String[] args) throws IOException {
        java.io.File file = new java.io.File(System.getProperty("java.io.tmpdir"), "practice-bufferedwriter-demo.txt");

        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(file))) {
            for (int i = 1; i <= 3; i++) {
                writer.write("line " + i);
                writer.newLine(); // the platform-correct line separator - portable across Windows/Unix, unlike a literal "\n"
            }
        } // close() flushes the buffer - without it, some writes could still be sitting in memory, never reaching disk

        System.out.println(Files.readString(file.toPath()));
        file.delete();
    }
}
