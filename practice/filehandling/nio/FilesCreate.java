package filehandling.nio;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Files.createFile()/createDirectory() throw FileAlreadyExistsException if the path is already
 * taken (unlike java.io.File.createNewFile()/mkdir(), which just return false). createDirectories()
 * additionally creates any missing PARENT directories along the way.
 */
public class FilesCreate {

    public static void main(String[] args) throws IOException {
        Path tmpDir = Path.of(System.getProperty("java.io.tmpdir"));
        Path file = tmpDir.resolve("practice-filescreate-demo.txt");
        Files.deleteIfExists(file); // make sure we start clean

        Files.createFile(file);
        System.out.println("created: " + Files.exists(file));

        try {
            Files.createFile(file); // already exists now
        } catch (FileAlreadyExistsException e) {
            System.out.println("createFile() on an existing path throws: " + e.getClass().getSimpleName());
        }

        Path nested = tmpDir.resolve("practice-filescreate-a/b/c"); // "a" and "a/b" don't exist yet either
        Files.createDirectories(nested); // creates all 3 missing levels in one call
        System.out.println("createDirectories() built the whole chain: " + Files.exists(nested));

        Files.delete(file);
        Files.delete(nested);
        Files.delete(tmpDir.resolve("practice-filescreate-a/b"));
        Files.delete(tmpDir.resolve("practice-filescreate-a"));
    }
}
