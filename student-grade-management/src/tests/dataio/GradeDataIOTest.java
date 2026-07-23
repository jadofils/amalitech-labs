package tests.dataio;

import dataio.GradeDataExporter;
import dataio.GradeDataImporter;
import dataio.GradeRecord;
import exceptions.ImportException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// No injected collaborators (Jackson's ObjectMapper is internal) - same reasoning as
// StudentDataIOTest / FileExporterTest, so there is no Mockito counterpart.
class GradeDataIOTest {

    private final Path tempDir = Path.of("target/test-grade-dataio-" + System.nanoTime());
    private final GradeDataExporter exporter = new GradeDataExporter();
    private final GradeDataImporter importer = new GradeDataImporter();

    private final List<GradeRecord> sample = List.of(
            new GradeRecord("GRD001", "STU001", "MATH01", 85.0, "01-01-2026"),
            new GradeRecord("GRD002", "STU002", "MUSC01", 92.5, "02-01-2026")
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
    @DisplayName("CSV export then import reproduces the original grade list exactly")
    void csvRoundTripTest() throws IOException {
        Files.createDirectories(tempDir);
        Path path = tempDir.resolve("grades.csv");

        exporter.exportCsv(sample, path);
        List<GradeRecord> imported = importer.importCsv(path);

        assertEquals(sample, imported);
    }

    @Test
    @DisplayName("JSON export then import reproduces the original grade list exactly")
    void jsonRoundTripTest() throws IOException {
        Files.createDirectories(tempDir);
        Path path = tempDir.resolve("grades.json");

        exporter.exportJson(sample, path);
        List<GradeRecord> imported = importer.importJson(path);

        assertEquals(sample, imported);
    }

    @Test
    @DisplayName("Binary export then import reproduces the original grade list exactly")
    void binaryRoundTripTest() throws IOException {
        Files.createDirectories(tempDir);
        Path path = tempDir.resolve("grades.bin");

        exporter.exportBinary(sample, path);
        List<GradeRecord> imported = importer.importBinary(path);

        assertEquals(sample, imported);
    }

    @Test
    @DisplayName("importCsv() throws ImportException for a row with the wrong number of fields")
    void csvImportRejectsMalformedRowTest() throws IOException {
        Files.createDirectories(tempDir);
        Path path = tempDir.resolve("malformed.csv");
        Files.writeString(path, "gradeId,studentId,subjectCode,grade,date\nGRD001,STU001,MATH01\n");

        assertThrows(ImportException.class, () -> importer.importCsv(path));
    }
}
