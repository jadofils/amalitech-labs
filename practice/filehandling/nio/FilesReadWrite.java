package filehandling.nio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * java.nio.file.Files - a utility class of static methods for whole-file operations; no explicit
 * Reader/Writer/Stream setup needed for the common cases. This class doesn't collide with its own
 * name here since it's called "FilesReadWrite", not "Files" - so, unlike the io/ classes named
 * exactly after their JDK type, this file can import java.nio.file.Files normally.
 */
public class FilesReadWrite {

    public static void main(String[] args) throws IOException {
        Path file = Path.of(System.getProperty("java.io.tmpdir"), "practice-filesreadwrite-demo.txt");

        Files.writeString(file, "hello\nworld\n"); // one call - no stream to open/close yourself
        System.out.println("readString(): " + Files.readString(file).strip());

        List<String> lines = Files.readAllLines(file);
        System.out.println("readAllLines(): " + lines);

        Files.write(file, "raw bytes".getBytes());
        byte[] bytes = Files.readAllBytes(file);
        System.out.println("readAllBytes(): " + bytes.length + " bytes -> " + new String(bytes));

        Files.delete(file);
    }
}
