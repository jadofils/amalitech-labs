package tests.console;

import console.SearchStudentsAction;
import exceptions.ExportException;
import export.FileExporter;
import manager.StudentManager;
import manager.StudentSearcher;
import model.enums.StudentType;
import model.student.RegularStudent;
import model.student.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Verifies SearchStudentsAction's own control flow (branch selection, input
 * sanitization/parsing, delegation to its collaborators) with StudentManager,
 * StudentSearcher, and FileExporter all mocked - including the
 * ApplicationException export-failure path that SearchStudentsActionTest
 * cannot exercise with a real FileExporter. Happy-path search results and
 * real file writes are covered there instead.
 */
class SearchStudentsActionMockitoTest {

    private int countOccurrences(String haystack, String needle) {
        int count = 0, index = 0;
        while ((index = haystack.indexOf(needle, index)) != -1) {
            count++;
            index += needle.length();
        }
        return count;
    }

    private String runWithInput(StudentManager studentManager, StudentSearcher studentSearcher,
                                 FileExporter fileExporter, String scriptedInput) {
        Scanner scanner = new Scanner(new ByteArrayInputStream(scriptedInput.getBytes(StandardCharsets.UTF_8)));
        SearchStudentsAction action = new SearchStudentsAction(scanner, studentManager, studentSearcher, fileExporter);
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            action.execute();
        } finally {
            System.setOut(originalOut);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Non-numeric grade-range input prints 'Invalid input.' and retries without calling searchByGradeRange()")
    void invalidGradeRangeNonNumericInputRetriesAndSearchesAgainTest() {
        StudentManager studentManager = mock(StudentManager.class);
        StudentSearcher studentSearcher = mock(StudentSearcher.class);
        FileExporter fileExporter = mock(FileExporter.class);
        Student student = new RegularStudent("Mock Student", 17, "m@school.edu", "1234567890");
        when(studentSearcher.searchByType(StudentType.REGULAR)).thenReturn(List.of(student));
        when(studentSearcher.getSearchDescription(eq("4"), any())).thenReturn("Type: Regular");

        String output = runWithInput(studentManager, studentSearcher, fileExporter, "3\nabc\n4\n1\n4\n\n");

        assertTrue(output.contains("Invalid input."));
        assertEquals(2, countOccurrences(output, "Search options:"));
        assertTrue(output.contains("SEARCH RESULTS (1 found)"));
        assertTrue(output.contains("Mock Student"));
        verify(studentSearcher, never()).searchByGradeRange(anyDouble(), anyDouble());
        verify(studentSearcher, times(1)).searchByType(StudentType.REGULAR);
    }

    @Test
    @DisplayName("An ExportException from fileExporter.exportToFile() is caught and reported as 'Export failed:'")
    void exportApplicationExceptionIsCaughtAndReportedTest() {
        StudentManager studentManager = mock(StudentManager.class);
        StudentSearcher studentSearcher = mock(StudentSearcher.class);
        FileExporter fileExporter = mock(FileExporter.class);
        Student student = new RegularStudent("Mock Student", 17, "m@school.edu", "1234567890");
        when(studentSearcher.searchById(anyString())).thenReturn(List.of(student));
        when(studentSearcher.getSearchDescription(eq("1"), any())).thenReturn("ID: X");
        when(fileExporter.exportToFile(anyString(), anyString())).thenThrow(new ExportException("disk full"));

        String output = runWithInput(studentManager, studentSearcher, fileExporter, "1\nX\n2\nfname\n\n");

        assertTrue(output.contains("Export failed: disk full"));
        verify(fileExporter, times(1)).exportToFile(eq("search_fname.txt"), anyString());
    }

    @Test
    @DisplayName("Option 1 sanitizes (trims) the raw ID before delegating to searchById()")
    void sanitizesAndDelegatesIdSearchTest() {
        StudentManager studentManager = mock(StudentManager.class);
        StudentSearcher studentSearcher = mock(StudentSearcher.class);
        FileExporter fileExporter = mock(FileExporter.class);
        when(studentSearcher.searchById("ID123")).thenReturn(List.of());

        String output = runWithInput(studentManager, studentSearcher, fileExporter, "1\n  ID123  \n4\n\n");

        assertTrue(output.contains("SEARCH RESULTS (0 found)"));
        verify(studentSearcher, times(1)).searchById("ID123");
    }

    @Test
    @DisplayName("Option 2 sanitizes (trims) the raw name before delegating to searchByName()")
    void sanitizesAndDelegatesNameSearchTest() {
        StudentManager studentManager = mock(StudentManager.class);
        StudentSearcher studentSearcher = mock(StudentSearcher.class);
        FileExporter fileExporter = mock(FileExporter.class);
        when(studentSearcher.searchByName("ali")).thenReturn(List.of());

        runWithInput(studentManager, studentSearcher, fileExporter, "2\n  ali  \n4\n\n");

        verify(studentSearcher, times(1)).searchByName("ali");
    }

    @Test
    @DisplayName("Option 3 parses min/max as doubles and builds the 'min-max%' description input")
    void delegatesGradeRangeSearchWithParsedDoublesTest() {
        StudentManager studentManager = mock(StudentManager.class);
        StudentSearcher studentSearcher = mock(StudentSearcher.class);
        FileExporter fileExporter = mock(FileExporter.class);
        when(studentSearcher.searchByGradeRange(70.5, 95.0)).thenReturn(List.of());
        when(studentSearcher.getSearchDescription(eq("3"), eq("70-95%"))).thenReturn("Grade range: 70-95%");

        runWithInput(studentManager, studentSearcher, fileExporter, "3\n70.5\n95\n4\n\n");

        verify(studentSearcher, times(1)).searchByGradeRange(70.5, 95.0);
        verify(studentSearcher, times(1)).getSearchDescription("3", "70-95%");
    }

    @Test
    @DisplayName("Option 4 maps choice '2' to HONORS and any other choice to REGULAR")
    void delegatesTypeSearchHonorsThenRegularAcrossNewSearchTest() {
        StudentManager studentManager = mock(StudentManager.class);
        StudentSearcher studentSearcher = mock(StudentSearcher.class);
        FileExporter fileExporter = mock(FileExporter.class);
        when(studentSearcher.searchByType(any())).thenReturn(List.of());

        runWithInput(studentManager, studentSearcher, fileExporter, "4\n2\n3\n4\n9\n4\n\n");

        verify(studentSearcher, times(1)).searchByType(StudentType.HONORS);
        verify(studentSearcher, times(1)).searchByType(StudentType.REGULAR);
    }

    @Test
    @DisplayName("Result action 1 delegates to StudentManager.findStudent() and displays the found student")
    void viewStudentDetailsDelegatesToFindStudentAndDisplaysTest() {
        StudentManager studentManager = mock(StudentManager.class);
        StudentSearcher studentSearcher = mock(StudentSearcher.class);
        FileExporter fileExporter = mock(FileExporter.class);
        when(studentSearcher.searchById(anyString())).thenReturn(List.of());
        Student mockStudent = mock(Student.class);
        when(studentManager.findStudent("VIEWID")).thenReturn(mockStudent);

        runWithInput(studentManager, studentSearcher, fileExporter, "1\nX\n1\nVIEWID\n\n");

        verify(studentManager, times(1)).findStudent("VIEWID");
        verify(mockStudent, times(1)).displayStudentDetails();
    }

    @Test
    @DisplayName("An unrecognized result action (e.g. '9') ends the loop without viewing or exporting anything")
    void unrecognizedResultActionEndsLoopWithoutSideEffectsTest() {
        StudentManager studentManager = mock(StudentManager.class);
        StudentSearcher studentSearcher = mock(StudentSearcher.class);
        FileExporter fileExporter = mock(FileExporter.class);
        when(studentSearcher.searchById(anyString())).thenReturn(List.of());

        String output = runWithInput(studentManager, studentSearcher, fileExporter, "1\nX\n9\n\n");

        assertEquals(1, countOccurrences(output, "Search options:"));
        verify(studentManager, never()).findStudent(anyString());
        verify(fileExporter, never()).exportToFile(anyString(), anyString());
    }

    @Test
    @DisplayName("buildExportContent() includes the search description, count, and each result row")
    void exportContentIncludesSearchDescriptionAndResultRowsTest() {
        StudentManager studentManager = mock(StudentManager.class);
        StudentSearcher studentSearcher = mock(StudentSearcher.class);
        FileExporter fileExporter = mock(FileExporter.class);
        Student student = new RegularStudent("Zoe Park", 16, "zoe@school.edu", "1234567890");
        when(studentSearcher.searchById(anyString())).thenReturn(List.of(student));
        when(studentSearcher.getSearchDescription(eq("1"), any())).thenReturn("ID: X");
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);

        runWithInput(studentManager, studentSearcher, fileExporter, "1\nX\n2\nrpt\n\n");

        verify(fileExporter, times(1)).exportToFile(eq("search_rpt.txt"), contentCaptor.capture());
        String content = contentCaptor.getValue();
        assertTrue(content.contains("Search Results: ID: X"));
        assertTrue(content.contains("Found: 1 students"));
        assertTrue(content.contains("Zoe Park"));
        assertTrue(content.contains("0.0%"));
    }
}
