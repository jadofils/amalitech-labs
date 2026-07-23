package tests.imports;

import main.exceptions.CSVImportException;
import main.imports.CSVParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

    // Regression coverage for CHANGELOG.md KI-10 lives in the "type mismatch" row below: the
    // SubjectType column used to be read and then completely ignored - a row could claim
    // "Elective" for a Core subject (or vice versa) and still import successfully.
    @ParameterizedTest
    @CsvSource({
            "'STU001,Mathematics,85', Invalid format",
            "'STU001,Mathematics,Core,abc', Invalid grade number",
            "'STU001,Mathematics,Core,105', out of range",
            "'STU001,Philosophy,Core,85', Unknown subject",
            "'STU001,Mathematics,Elective,85', type mismatch",
            "'STU001,Mathematics,Bogus,85', Unknown subject type"
    })
    @DisplayName("A single malformed data row is reported as exactly one error containing the expected message")
    void singleRowErrorTest(String dataRow, String expectedErrorSubstring) throws IOException {
        File file = writeCsv("StudentID,SubjectName,SubjectType,Grade\n" + dataRow + "\n");

        CSVParser.CSVParseResult result = parser.parse(file);

        assertEquals(0, result.getValidCount());
        assertEquals(1, result.getErrorCount());
        assertTrue(result.getErrors().get(0).contains(expectedErrorSubstring));
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
