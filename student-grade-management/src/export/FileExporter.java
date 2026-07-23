package export;

import exceptions.ExportException;
import logging.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes report text content to a file under a configurable directory (default {@code reports/}).
 * v3: NIO.2 ({@link Path}/{@link Files}) instead of {@code java.io.File}/{@code FileWriter}
 * underneath - the string shape of {@link FileExportResult#getFilePath()} is unchanged
 * ({@code "<reportsDir>/<filename>"}, forward slash, regardless of OS) since existing tests and
 * console messages depend on that exact format, not on whatever separator {@link Path#toString()}
 * would use on a given platform.
 */
public class FileExporter {
    private final String reportsDir;

    public FileExporter() {
        this("reports");
    }

    public FileExporter(String reportsDir) {
        this.reportsDir = reportsDir;
    }

    /**
     * Writes {@code content} to {@code <reportsDir>/<filename>}, creating the
     * directory if it doesn't exist yet.
     *
     * @throws ExportException if the write fails
     */
    public FileExportResult exportToFile(String filename, String content) {
        ensureDirectoryExists();
        String path = reportsDir + "/" + filename;
        Path file = Path.of(path);

        try {
            Files.writeString(file, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Logger.error("Failed to export report to " + path, e);
            throw new ExportException("Failed to export report: " + e.getMessage(), path, e);
        }

        long size = sizeOf(file, path);
        Logger.info("Report exported: " + path + " (" + size + " bytes)");
        return new FileExportResult(path, size);
    }

    private long sizeOf(Path file, String path) {
        try {
            return Files.size(file);
        } catch (IOException e) {
            throw new ExportException("Failed to export report: " + e.getMessage(), path, e);
        }
    }

    private void ensureDirectoryExists() {
        Path dir = Path.of(reportsDir);
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new ExportException("Failed to create reports directory: " + e.getMessage(), reportsDir, e);
            }
        }
    }

    public static class FileExportResult {
        private final String filePath;
        private final long size;

        public FileExportResult(String filePath, long size) {
            this.filePath = filePath;
            this.size = size;
        }

        public String getFilePath() { return filePath; }
        public long getSize() { return size; }
        public String getFileName() { return filePath.substring(filePath.lastIndexOf('/') + 1); }
    }
}
