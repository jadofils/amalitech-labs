package tests.export;

import export.FileExporter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

// FileExporter only wraps java.io.File/FileWriter directly - it has no
// injected collaborator to mock, so there's no FileExporterMockitoTest
// (see tests/README.md's note on LetterGradeTest for the same reasoning).
class FileExporterTest {

    private final String testDir = "target/test-file-exporter-" + System.nanoTime();

    @AfterEach
    void cleanUp() throws IOException {
        File dir = new File(testDir);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                Files.deleteIfExists(f.toPath());
            }
        }
        Files.deleteIfExists(Path.of(testDir));
    }

    @Test
    @DisplayName("exportToFile() creates the directory if it doesn't exist yet")
    void createsDirectoryIfMissingTest() {
        assertFalse(new File(testDir).exists());
        FileExporter exporter = new FileExporter(testDir);

        exporter.exportToFile("report.txt", "hello");

        assertTrue(new File(testDir).isDirectory());
    }

    @Test
    @DisplayName("exportToFile() writes the exact given content and reports its real size")
    void writesContentAndReportsSizeTest() throws IOException {
        FileExporter exporter = new FileExporter(testDir);
        String content = "STUDENT GRADE REPORT\nhello world\n";

        FileExporter.FileExportResult result = exporter.exportToFile("report.txt", content);

        assertEquals(testDir + "/report.txt", result.getFilePath());
        assertEquals("report.txt", result.getFileName());
        assertEquals(content.getBytes().length, result.getSize());
        assertEquals(content, Files.readString(Path.of(result.getFilePath())));
    }
}
