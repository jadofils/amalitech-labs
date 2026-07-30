package filehandling.nio;

/**
 * java.nio.file.Path - represents a file system path (doesn't have to exist yet), replacing java.io.File
 * for path manipulation. Path.of(...) is the modern factory - purely string/segment manipulation below,
 * nothing here touches the actual file system.
 */
public class Path {

    public static void main(String[] args) {
        java.nio.file.Path path = java.nio.file.Path.of("practice", "filehandling", "nio", "Path.java");

        System.out.println("path: " + path);
        System.out.println("getFileName(): " + path.getFileName());
        System.out.println("getParent(): " + path.getParent());
        System.out.println("getNameCount(): " + path.getNameCount());
        System.out.println("subpath(0, 2): " + path.subpath(0, 2));

        java.nio.file.Path messy = java.nio.file.Path.of("practice/./filehandling/../filehandling/nio");
        System.out.println("normalize(\"" + messy + "\"): " + messy.normalize()); // collapses "." and ".." segments

        java.nio.file.Path relative = java.nio.file.Path.of("some-file.txt");
        System.out.println("toAbsolutePath(): " + relative.toAbsolutePath()); // resolved against the current working directory

        java.nio.file.Path base = java.nio.file.Path.of("a", "b");
        java.nio.file.Path resolved = base.resolve("c.txt"); // appends, as if joining path segments
        System.out.println("resolve(\"c.txt\"): " + resolved);
    }
}
