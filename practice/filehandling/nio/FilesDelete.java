package filehandling.nio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/** Files.delete() throws NoSuchFileException if the path doesn't exist; Files.deleteIfExists() returns a boolean instead - no exception either way. */
public class FilesDelete {

    public static void main(String[] args) throws IOException {
        Path file = Path.of(System.getProperty("java.io.tmpdir"), "practice-filesdelete-demo.txt");
        Files.writeString(file, "temporary");

        Files.delete(file); // succeeds silently since the file exists
        System.out.println("deleted, exists now: " + Files.exists(file));

        try {
            Files.delete(file); // deleting again - the file is already gone
        } catch (NoSuchFileException e) {
            System.out.println("delete() on a missing file throws: " + e.getClass().getSimpleName());
        }

        boolean deletedSomething = Files.deleteIfExists(file); // same missing file, but no exception this time
        System.out.println("deleteIfExists() on a missing file: " + deletedSomething);
    }
}
