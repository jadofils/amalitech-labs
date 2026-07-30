package filehandling.nio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.concurrent.TimeUnit;

/**
 * WatchService - subscribes to file system change notifications (create/modify/delete) for a
 * directory, with NO equivalent at all in the older java.io API - this is nio-only. poll(timeout)
 * is used here instead of the blocking take(), so this demo can't hang if an event is somehow missed.
 */
public class WatchService {

    public static void main(String[] args) throws IOException, InterruptedException {
        Path dir = Files.createTempDirectory("practice-watchservice-demo");

        try (java.nio.file.WatchService watcher = dir.getFileSystem().newWatchService()) {
            dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);

            Path newFile = dir.resolve("watched.txt");
            Files.writeString(newFile, "trigger a create event"); // this should show up as an ENTRY_CREATE event

            WatchKey key = watcher.poll(5, TimeUnit.SECONDS); // waits up to 5s, unlike take() which waits forever
            if (key == null) {
                System.out.println("no event arrived within 5s");
            } else {
                for (WatchEvent<?> event : key.pollEvents()) {
                    System.out.println("event: " + event.kind() + " -> " + event.context());
                }
                key.reset(); // required to keep receiving further events on this key
            }

            Files.delete(newFile);
        }

        Files.delete(dir);
    }
}
