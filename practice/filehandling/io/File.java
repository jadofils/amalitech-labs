package filehandling.io;

import java.io.IOException;

/**
 * java.io.File - represents a file/directory PATH and its metadata (exists, size, permissions),
 * not its content - reading/writing still needs a Reader/Writer/Stream. java.nio.file.Path + Files
 * is the modern replacement (see filehandling.nio), but File is still everywhere in older code.
 */
public class File {

    public static void main(String[] args) throws IOException {
        java.io.File dir = new java.io.File(System.getProperty("java.io.tmpdir"), "practice-file-demo-dir");
        java.io.File file = new java.io.File(dir, "note.txt");

        System.out.println("exists before creating: " + dir.exists());
        dir.mkdir(); // creates one directory level; mkdirs() would create any missing parent directories too
        file.createNewFile(); // creates an empty file; returns false (not an exception) if it already existed

        System.out.println("exists now: " + file.exists());
        System.out.println("isDirectory: " + dir.isDirectory() + ", isFile: " + file.isFile());
        System.out.println("absolute path: " + file.getAbsolutePath());
        System.out.println("length (empty file): " + file.length());

        java.io.File[] contents = dir.listFiles();
        System.out.println("dir.listFiles(): " + java.util.Arrays.toString(contents));

        file.delete(); // a File object doesn't need closing like a stream - it's just a path, not an open handle
        dir.delete();
        System.out.println("cleaned up: " + !dir.exists());
    }
}
