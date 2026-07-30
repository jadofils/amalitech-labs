package filehandling.io;

import java.io.IOException;
import java.nio.file.Files;

/**
 * FileOutputStream - writes raw BYTES, not characters - no charset involved at all. Use this for
 * binary data (images, serialized objects, network payloads); use FileWriter for text.
 */
public class FileOutputStream {

    public static void main(String[] args) throws IOException {
        java.io.File file = new java.io.File(System.getProperty("java.io.tmpdir"), "practice-fileoutputstream-demo.bin");

        byte[] data = {0x01, 0x02, 0x03, 0x04};
        try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
            out.write(data);
        }
        System.out.println("wrote " + data.length + " bytes, file size on disk: " + file.length());

        try (java.io.FileOutputStream appender = new java.io.FileOutputStream(file, true)) { // append=true
            appender.write(new byte[]{0x05});
        }
        System.out.println("file size after append: " + file.length());

        file.delete();
    }
}
