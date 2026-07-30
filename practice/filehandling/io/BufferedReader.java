package filehandling.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** BufferedReader - wraps another Reader to read in efficient chunks, and adds readLine() (returns null at end of file, not -1). */
public class BufferedReader {

    public static void main(String[] args) throws IOException {
        java.io.File file = new java.io.File(System.getProperty("java.io.tmpdir"), "practice-bufferedreader-demo.txt");
        Files.writeString(file.toPath(), "line 1\nline 2\nline 3\n", StandardCharsets.UTF_8);

        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) { // null is the end-of-file sentinel here, unlike read()'s -1
                System.out.println("read: " + line);
            }
        }

        file.delete();
    }
}
