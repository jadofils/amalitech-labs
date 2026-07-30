package filehandling.nio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Files.copy() duplicates a file (source untouched); Files.move() relocates/renames it (source is gone afterward). Both need REPLACE_EXISTING to overwrite a target. */
public class FilesCopyMove {

    public static void main(String[] args) throws IOException {
        Path tmpDir = Path.of(System.getProperty("java.io.tmpdir"));
        Path original = tmpDir.resolve("practice-copymove-original.txt");
        Path copy = tmpDir.resolve("practice-copymove-copy.txt");
        Path moved = tmpDir.resolve("practice-copymove-moved.txt");

        Files.writeString(original, "original content");

        Files.copy(original, copy, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("after copy - original exists: " + Files.exists(original) + ", copy exists: " + Files.exists(copy));

        Files.move(copy, moved, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("after move - copy exists: " + Files.exists(copy) + ", moved exists: " + Files.exists(moved));

        Files.delete(original);
        Files.delete(moved);
    }
}
