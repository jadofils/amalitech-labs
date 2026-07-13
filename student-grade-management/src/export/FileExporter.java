package export;

import exceptions.ExportException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileExporter {
    private final String reportsDir;

    public FileExporter() {
        this("reports");
    }

    public FileExporter(String reportsDir) {
        this.reportsDir = reportsDir;
    }

    public FileExportResult exportToFile(String filename, String content) {
        ensureDirectoryExists();
        String path = reportsDir + "/" + filename;
        File file = new File(path);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
            writer.flush();
        } catch (IOException e) {
            throw new ExportException("Failed to export report: " + e.getMessage(), path, e);
        }

        return new FileExportResult(path, file.length());
    }

    private void ensureDirectoryExists() {
        File dir = new File(reportsDir);
        if (!dir.exists()) {
            dir.mkdirs();
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
