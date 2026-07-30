package filehandling.nio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** FileChannel - a lower-level, buffer-based way to read/write files; unlike a Stream it also supports random access via position(long). */
public class FileChannel {

    public static void main(String[] args) throws IOException {
        Path file = Path.of(System.getProperty("java.io.tmpdir"), "practice-filechannel-demo.txt");
        Files.deleteIfExists(file);

        try (java.nio.channels.FileChannel channel = java.nio.channels.FileChannel.open(
                file, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap("hello channel".getBytes());
            channel.write(buffer); // writes everything currently between the buffer's position and limit
        }

        try (java.nio.channels.FileChannel channel = java.nio.channels.FileChannel.open(file, StandardOpenOption.READ)) {
            java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(64);
            int bytesRead = channel.read(buffer);
            buffer.flip(); // switch to reading mode before extracting the bytes just written into it
            byte[] data = new byte[bytesRead];
            buffer.get(data);
            System.out.println("read " + bytesRead + " bytes: " + new String(data));
        }

        try (java.nio.channels.FileChannel channel = java.nio.channels.FileChannel.open(file, StandardOpenOption.READ)) {
            channel.position(6); // random access - jump straight to byte offset 6, no need to read from the start
            java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(64);
            int bytesRead = channel.read(buffer);
            buffer.flip();
            byte[] data = new byte[bytesRead];
            buffer.get(data);
            System.out.println("read from position(6): " + new String(data));
        }

        Files.delete(file);
    }
}
