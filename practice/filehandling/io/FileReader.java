package filehandling.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * FileReader - reads CHARACTERS from a file one at a time (or into a char[] chunk); read() returns
 * -1 at end of file. Reading one character at a time is slow for real files - see BufferedReader,
 * which wraps a FileReader to read in efficient chunks and adds readLine().
 */
public class FileReader {

    public static void main(String[] args) throws IOException {
        java.io.File file = new java.io.File(System.getProperty("java.io.tmpdir"), "practice-filereader-demo.txt");
        Files.writeString(file.toPath(), "abc", StandardCharsets.UTF_8); // quick setup, not the thing being demoed

        try (java.io.FileReader reader = new java.io.FileReader(file)) {
            StringBuilder collected = new StringBuilder();
            int nextChar;
            while ((nextChar = reader.read()) != -1) { // -1 is the end-of-file sentinel, not a real char value
                collected.append((char) nextChar);
            }
            System.out.println("read one char at a time: " + collected);
        }

        file.delete();
    }
}
