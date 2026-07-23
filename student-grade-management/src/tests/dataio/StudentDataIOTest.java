package tests.dataio;

import dataio.StudentDataExporter;
import dataio.StudentDataImporter;
import dataio.StudentRecord;
import exceptions.ImportException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// StudentDataExporter/Importer have no injected collaborators (Jackson's ObjectMapper is created
// internally, not passed in) - same reasoning as FileExporterTest (see tests/README.md), so there
// is no Mockito counterpart to this file.
class StudentDataIOTest {

    private final Path tempDir = Path.of("target/test-dataio-" + System.nanoTime());
    private final StudentDataExporter exporter = new StudentDataExporter();
    private final StudentDataImporter importer = new StudentDataImporter();

    private final List<StudentRecord> sample = List.of(
            new StudentRecord("STU001", "Alice Johnson", "REGULAR", 16, "alice@school.edu", "1234567890", "ACTIVE"),
            new StudentRecord("STU002", "Bob Smith", "HONORS", 18, "bob@school.edu", "0987654321", "GRADUATED")
    );

    @AfterEach
    void cleanUp() throws IOException {
        if (Files.exists(tempDir)) {
            try (var files = Files.list(tempDir)) {
                for (Path file : files.toList()) {
                    Files.deleteIfExists(file);
                }
            }
            Files.deleteIfExists(tempDir);
        }
    }

    @Test
    @DisplayName("CSV export then import reproduces the original list exactly")
    void csvRoundTripTest() throws IOException {
        Files.createDirectories(tempDir);
        Path path = tempDir.resolve("students.csv");

        exporter.exportCsv(sample, path);
        List<StudentRecord> imported = importer.importCsv(path);

        assertEquals(sample, imported);
    }

    @Test
    @DisplayName("JSON export then import reproduces the original list exactly")
    void jsonRoundTripTest() throws IOException {
        Files.createDirectories(tempDir);
        Path path = tempDir.resolve("students.json");

        exporter.exportJson(sample, path);
        List<StudentRecord> imported = importer.importJson(path);

        assertEquals(sample, imported);
    }

    @Test
    @DisplayName("Binary export then import reproduces the original list exactly")
    void binaryRoundTripTest() throws IOException {
        Files.createDirectories(tempDir);
        Path path = tempDir.resolve("students.bin");

        exporter.exportBinary(sample, path);
        List<StudentRecord> imported = importer.importBinary(path);

        assertEquals(sample, imported);
    }

    @Test
    @DisplayName("importCsv() throws ImportException for a row with the wrong number of fields")
    void csvImportRejectsMalformedRowTest() throws IOException {
        Files.createDirectories(tempDir);
        Path path = tempDir.resolve("malformed.csv");
        Files.writeString(path, "studentId,name,studentType,age,email,phone,status\nSTU001,Alice,REGULAR,16\n");

        assertThrows(ImportException.class, () -> importer.importCsv(path));
    }

    @Test
    @DisplayName("JSON export produces human-readable (pretty-printed) output")
    void jsonExportIsPrettyPrintedTest() throws IOException {
        Files.createDirectories(tempDir);
        Path path = tempDir.resolve("pretty.json");

        exporter.exportJson(sample, path);

        String content = Files.readString(path);
        assertTrue(content.contains("\n"), "pretty-printed JSON should span multiple lines");
        assertTrue(content.contains("STU001"));
    }
}
