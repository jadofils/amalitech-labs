package tests.imports;

import main.exceptions.CSVImportException;
import main.imports.CSVParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import main.repository.subject.SubjectRepositoryImpl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class CSVParserTest {

    private final SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
    private final CSVParser parser = new CSVParser(subjects);
    private File csvFile;

    private File writeCsv(String content) throws IOException {
        csvFile = File.createTempFile("bulk-import-test", ".csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write(content);
        }
        return csvFile;
    }

    @AfterEach
    void cleanUp() throws IOException {
        if (csvFile != null) {
            Files.deleteIfExists(csvFile.toPath());
        }
    }

    @Test
    @DisplayName("A well-formed row parses successfully with the matched subject")
    void validRowParsesTest() throws IOException {
        File file = writeCsv("StudentID,SubjectName,SubjectType,Grade\nSTU001,Mathematics,Core,85\n");

        CSVParser.CSVParseResult result = parser.parse(file);

        assertEquals(1, result.getValidCount());
        assertEquals(0, result.getErrorCount());
        assertEquals("MATH01", result.getValidRows().get(0).getSubject().getSubjectCode());
        assertEquals(85.0, result.getValidRows().get(0).getGrade());
    }

    @Test
    @DisplayName("Wrong column count is reported as an error, not thrown")
    void wrongColumnCountIsErrorTest() throws IOException {
        File file = writeCsv("StudentID,SubjectName,SubjectType,Grade\nSTU001,Mathematics,85\n");

        CSVParser.CSVParseResult result = parser.parse(file);

        assertEquals(0, result.getValidCount());
        assertEquals(1, result.getErrorCount());
        assertTrue(result.getErrors().get(0).contains("Invalid format"));
    }

    @Test
    @DisplayName("A non-numeric grade is reported as an error")
    void nonNumericGradeIsErrorTest() throws IOException {
        File file = writeCsv("StudentID,SubjectName,SubjectType,Grade\nSTU001,Mathematics,Core,abc\n");

        CSVParser.CSVParseResult result = parser.parse(file);

        assertEquals(1, result.getErrorCount());
        assertTrue(result.getErrors().get(0).contains("Invalid grade number"));
    }

    @Test
    @DisplayName("A grade above 100 is reported as out of range")
    void outOfRangeGradeIsErrorTest() throws IOException {
        File file = writeCsv("StudentID,SubjectName,SubjectType,Grade\nSTU001,Mathematics,Core,105\n");

        CSVParser.CSVParseResult result = parser.parse(file);

        assertEquals(1, result.getErrorCount());
        assertTrue(result.getErrors().get(0).contains("out of range"));
    }

    @Test
    @DisplayName("An unknown subject name is reported as an error")
    void unknownSubjectIsErrorTest() throws IOException {
        File file = writeCsv("StudentID,SubjectName,SubjectType,Grade\nSTU001,Philosophy,Core,85\n");

        CSVParser.CSVParseResult result = parser.parse(file);

        assertEquals(1, result.getErrorCount());
        assertTrue(result.getErrors().get(0).contains("Unknown subject"));
    }

    // Regression test for CHANGELOG.md KI-10: the SubjectType column used to
    // be read and then completely ignored - a row could claim "Elective"
    // for a Core subject (or vice versa) and still import successfully.
    @Test
    @DisplayName("A row whose declared SubjectType doesn't match the subject's real type is rejected")
    void mismatchedSubjectTypeIsErrorTest() throws IOException {
        File file = writeCsv("StudentID,SubjectName,SubjectType,Grade\nSTU001,Mathematics,Elective,85\n");

        CSVParser.CSVParseResult result = parser.parse(file);

        assertEquals(0, result.getValidCount());
        assertEquals(1, result.getErrorCount());
        assertTrue(result.getErrors().get(0).contains("type mismatch"));
    }

    @Test
    @DisplayName("An unrecognized SubjectType string is reported as an error")
    void unknownSubjectTypeStringIsErrorTest() throws IOException {
        File file = writeCsv("StudentID,SubjectName,SubjectType,Grade\nSTU001,Mathematics,Bogus,85\n");

        CSVParser.CSVParseResult result = parser.parse(file);

        assertEquals(1, result.getErrorCount());
        assertTrue(result.getErrors().get(0).contains("Unknown subject type"));
    }

    @Test
    @DisplayName("Blank lines in the file are skipped without producing an error")
    void blankLinesAreSkippedTest() throws IOException {
        File file = writeCsv("StudentID,SubjectName,SubjectType,Grade\n\nSTU001,Mathematics,Core,85\n\n");

        CSVParser.CSVParseResult result = parser.parse(file);

        assertEquals(1, result.getValidCount());
        assertEquals(0, result.getErrorCount());
    }

    @Test
    @DisplayName("A missing file throws CSVImportException rather than an unchecked IOException")
    void missingFileThrowsCSVImportExceptionTest() {
        File missing = new File("does-not-exist-" + System.nanoTime() + ".csv");

        assertThrows(CSVImportException.class, () -> parser.parse(missing));
    }
}
