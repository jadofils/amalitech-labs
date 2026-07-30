package filehandling.nio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/** Files.list() lists ONE directory's immediate entries; Files.walk() recurses into subdirectories too. Both return a Stream<Path> that MUST be closed (try-with-resources) - it holds an open directory handle. */
public class FilesWalk {

    public static void main(String[] args) throws IOException {
        Path root = Files.createTempDirectory("practice-fileswalk-demo");
        Path sub = Files.createDirectory(root.resolve("sub"));
        Files.writeString(root.resolve("top.txt"), "top");
        Files.writeString(sub.resolve("nested.txt"), "nested");

        try (var listStream = Files.list(root)) {
            List<String> topLevel = listStream.map(p -> p.getFileName().toString()).sorted().collect(Collectors.toList());
            System.out.println("list() (one level only): " + topLevel);
        }

        try (var walkStream = Files.walk(root)) {
            List<String> everything = walkStream
                    .filter(Files::isRegularFile) // walk() also yields directories themselves - filter those out
                    .map(p -> root.relativize(p).toString())
                    .sorted()
                    .collect(Collectors.toList());
            System.out.println("walk() (recursive): " + everything);
        }

        Files.delete(sub.resolve("nested.txt"));
        Files.delete(root.resolve("top.txt"));
        Files.delete(sub);
        Files.delete(root);
    }
}
