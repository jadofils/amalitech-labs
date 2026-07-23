package tests.console;

import main.console.BulkImportAction;
import main.exceptions.ImportException;
import main.imports.BulkImportService;
import main.model.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Mocks BulkImportService to verify BulkImportAction's branching in
 * isolation: the empty-filename short-circuit (importFromFile() must never
 * be called), a non-zero fail count that the real seeded stack rarely
 * produces on demand, and the ImportException path being caught internally
 * instead of propagating out of execute().
 */
class BulkImportActionMockitoTest {

    private String runWithInput(BulkImportService bulkImportService, String scriptedInput) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try (Scanner scanner = new Scanner(new ByteArrayInputStream(scriptedInput.getBytes(StandardCharsets.UTF_8)))) {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            new BulkImportAction(scanner, bulkImportService).execute();
        } finally {
            System.setOut(originalOut);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Empty filename prints a message and never calls importFromFile()")
    void emptyFilenameNeverCallsServiceTest() {
        BulkImportService bulkImportService = mock(BulkImportService.class);

        String output = runWithInput(bulkImportService, "\n");

        assertTrue(output.contains("Filename cannot be empty."));
        verify(bulkImportService, never()).importFromFile(anyString());
    }

    @Test
    @DisplayName("A result with no failures prints the summary and completion message but no Failed Records section")
    void successfulResultPrintsSummaryTest() {
        BulkImportService bulkImportService = mock(BulkImportService.class);
        BulkImportService.ImportResult result = new BulkImportService.ImportResult(
                2, 2, 0, List.of(), "import_log_20260722_000000.txt");
        when(bulkImportService.importFromFile("grades")).thenReturn(result);

        String output = runWithInput(bulkImportService, "grades\n\n");

        assertTrue(output.contains("IMPORT SUMMARY"));
        assertTrue(output.contains("Total Rows: 2"));
        assertTrue(output.contains("Successfully Imported: 2"));
        assertTrue(output.contains("Failed: 0"));
        assertFalse(output.contains("Failed Records:"));
        assertTrue(output.contains("Import completed!"));
        assertTrue(output.contains("See import_log_20260722_000000.txt for details"));
    }

    @Test
    @DisplayName("A result with failures prints the Failed Records section listing each reason")
    void resultWithFailuresPrintsFailedRecordsTest() {
        BulkImportService bulkImportService = mock(BulkImportService.class);
        BulkImportService.ImportResult result = new BulkImportService.ImportResult(
                3, 1, 2, List.of("Row 2: Invalid student ID (NOPE999)", "Row 3: Unknown subject (Woodworking)"),
                "import_log_20260722_000001.txt");
        when(bulkImportService.importFromFile("grades")).thenReturn(result);

        String output = runWithInput(bulkImportService, "grades\n\n");

        assertTrue(output.contains("Successfully Imported: 1"));
        assertTrue(output.contains("Failed: 2"));
        assertTrue(output.contains("Failed Records:"));
        assertTrue(output.contains("Row 2: Invalid student ID (NOPE999)"));
        assertTrue(output.contains("Row 3: Unknown subject (Woodworking)"));
    }

    @Test
    @DisplayName("ImportException is caught internally: execute() doesn't throw, and the message plus file path are printed")
    void importExceptionIsCaughtAndPrintedTest() {
        BulkImportService bulkImportService = mock(BulkImportService.class);
        when(bulkImportService.importFromFile("missing"))
                .thenThrow(new ImportException("File not found: main.imports/missing.csv", "main.imports/missing.csv", null));

        String output = assertDoesNotThrow(() -> runWithInput(bulkImportService, "missing\n\n"));

        assertTrue(output.contains("ERROR: File not found: main.imports/missing.csv"));
        assertTrue(output.contains("File: main.imports/missing.csv"));
    }

    @Test
    @DisplayName("getOptionNumber(), getLabel() and isAuthorizedFor() report the expected menu metadata")
    void menuMetadataTest() {
        BulkImportService bulkImportService = mock(BulkImportService.class);
        BulkImportAction action = new BulkImportAction(new Scanner(new ByteArrayInputStream(new byte[0])),
                bulkImportService);

        assertEquals(7, action.getOptionNumber());
        assertEquals("Bulk Import Grades", action.getLabel());
        assertTrue(action.isAuthorizedFor(Role.TEACHER));
        assertFalse(action.isAuthorizedFor(Role.STUDENT));
    }
}
