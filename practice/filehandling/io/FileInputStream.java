package filehandling.io;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

/** FileInputStream - reads raw BYTES from a file; read() returns an int 0-255 (or -1 at end of file), NOT a char. */
public class FileInputStream {

    public static void main(String[] args) throws IOException {
        java.io.File file = new java.io.File(System.getProperty("java.io.tmpdir"), "practice-fileinputstream-demo.bin");
        Files.write(file.toPath(), new byte[]{10, 20, 30, 40});

        try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
            byte[] buffer = new byte[4];
            int bytesRead = in.read(buffer); // reads up to buffer.length bytes in one call, returns how many it actually got
            System.out.println("read " + bytesRead + " bytes: " + Arrays.toString(buffer));
        }

        file.delete();
    }
}
