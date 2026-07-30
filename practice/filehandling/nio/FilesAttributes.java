package filehandling.nio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Files.exists()/isDirectory()/isRegularFile()/size()/getLastModifiedTime() - metadata queries, no content read. */
public class FilesAttributes {

    public static void main(String[] args) throws IOException {
        Path file = Path.of(System.getProperty("java.io.tmpdir"), "practice-filesattributes-demo.txt");
        Files.writeString(file, "12345");

        System.out.println("exists(): " + Files.exists(file));
        System.out.println("isRegularFile(): " + Files.isRegularFile(file));
        System.out.println("isDirectory(): " + Files.isDirectory(file));
        System.out.println("size(): " + Files.size(file) + " bytes");
        System.out.println("getLastModifiedTime(): " + Files.getLastModifiedTime(file));
        System.out.println("isReadable()/isWritable(): " + Files.isReadable(file) + " / " + Files.isWritable(file));

        Files.delete(file);
    }
}
