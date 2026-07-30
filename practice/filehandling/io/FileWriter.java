package filehandling.io;

import java.io.IOException;

/**
 * FileWriter - writes CHARACTERS (text) to a file, using the platform's default charset. Always
 * close it (try-with-resources) so buffered output actually gets flushed to disk.
 */
public class FileWriter {

    public static void main(String[] args) throws IOException {
        java.io.File file = new java.io.File(System.getProperty("java.io.tmpdir"), "practice-filewriter-demo.txt");

        try (java.io.FileWriter writer = new java.io.FileWriter(file)) { // overwrites the file if it already exists
            writer.write("first line\n");
            writer.write("second line\n");
        }
        System.out.println("wrote (overwrite mode): " + java.nio.file.Files.readString(file.toPath()).strip());

        try (java.io.FileWriter appender = new java.io.FileWriter(file, true)) { // append=true - keeps existing content
            appender.write("appended line\n");
        }
        System.out.println("after append: " + java.nio.file.Files.readString(file.toPath()).strip());

        file.delete();
    }
}
