package tests.console;

import console.SearchStudentsAction;
import export.FileExporter;
import manager.GradeManager;
import manager.StudentManager;
import manager.StudentSearcher;
import model.grade.Grade;
import model.student.Student;
import model.subject.Subject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.student.StudentRepositoryImpl;
import repository.subject.SubjectRepositoryImpl;
import service.GradeService;
import service.GradeServiceImpl;
import service.StudentService;
import service.StudentServiceImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Behavioral tests for SearchStudentsAction (menu option 9) using the same
 * real StudentRepositoryImpl/SubjectRepositoryImpl/GradeServiceImpl/
 * GradeManager/StudentManager wiring as StudentManagerTest, plus a real
 * StudentSearcher and a real FileExporter redirected to a temp directory
 * (see FileExporterTest for that pattern). SearchStudentsActionMockitoTest
 * covers interaction verification, sanitization, delegation, and the
 * ApplicationException export-failure path through mocked collaborators.
 */
class SearchStudentsActionTest {

    private final String testDir = "target/test-search-students-" + System.nanoTime();

    private StudentRepositoryImpl studentRepository;
    private SubjectRepositoryImpl subjectRepository;
    private GradeManager gradeManager;
    private StudentManager studentManager;
    private StudentSearcher studentSearcher;
    private FileExporter fileExporter;

    @BeforeEach
    void setUp() {
        studentRepository = new StudentRepositoryImpl();
        subjectRepository = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(studentRepository, subjectRepository);
        gradeManager = new GradeManager(gradeService, subjectRepository);
        StudentService studentService = new StudentServiceImpl(studentRepository);
        studentManager = new StudentManager(studentService, gradeManager);
        studentSearcher = new StudentSearcher(studentManager);
        fileExporter = new FileExporter(testDir);
    }

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

    private String runWithInput(String scriptedInput) {
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

    private int countOccurrences(String haystack, String needle) {
        int count = 0, index = 0;
        while ((index = haystack.indexOf(needle, index)) != -1) {
            count++;
            index += needle.length();
        }
        return count;
    }

    @Test
    @DisplayName("getOptionNumber()/getLabel() identify this as menu option 9, Search Students")
    void optionNumberAndLabelTest() {
        SearchStudentsAction action = new SearchStudentsAction(
                new Scanner(new ByteArrayInputStream(new byte[0])), studentManager, studentSearcher, fileExporter);

        assertEquals(9, action.getOptionNumber());
        assertEquals("Search Students", action.getLabel());
    }

    @Test
    @DisplayName("Option 1 (by ID) finds the matching student and returns to the main menu on action 4")
    void searchByIdHappyPathTest() {
        Student alice = studentManager.getAllStudents().get(0);

        String output = runWithInput("1\n" + alice.getStudentId() + "\n4\n\n");

        assertTrue(output.contains("SEARCH RESULTS (1 found)"));
        assertTrue(output.contains(alice.getStudentId()));
        assertTrue(output.contains(alice.getName()));
        assertEquals(1, countOccurrences(output, "Search options:"));
    }

    @Test
    @DisplayName("Option 2 (by name, partial match) finds the matching student")
    void searchByNameHappyPathTest() {
        String output = runWithInput("2\nAlice\n4\n\n");

        assertTrue(output.contains("SEARCH RESULTS (1 found)"));
        assertTrue(output.contains("Alice Johnson"));
    }

    @Test
    @DisplayName("Option 3 (by grade range) only returns students whose average falls inside the range")
    void searchByGradeRangeHappyPathTest() {
        Student alice = studentManager.getAllStudents().get(0);
        Subject subject = subjectRepository.getAllSubjects().get(0);
        gradeManager.addGrade(new Grade(alice.getStudentId(), subject, 85.0));
        // The other 4 seeded students still have no grades, so their average is 0.0 - outside 80-90.

        String output = runWithInput("3\n80\n90\n4\n\n");

        assertTrue(output.contains("SEARCH RESULTS (1 found)"));
        assertTrue(output.contains("Alice Johnson"));
    }

    @Test
    @DisplayName("Option 4 (by type, Honors) returns exactly the seeded Honors students")
    void searchByTypeHappyPathTest() {
        String output = runWithInput("4\n2\n4\n\n");

        assertTrue(output.contains("SEARCH RESULTS (2 found)"));
        assertTrue(output.contains("Bob Smith"));
        assertTrue(output.contains("David Chen"));
    }

    @Test
    @DisplayName("An invalid top-level option prints 'Invalid option.' and retries the search-options menu")
    void invalidTopLevelOptionRetriesTest() {
        Student alice = studentManager.getAllStudents().get(0);

        String output = runWithInput("9\n1\n" + alice.getStudentId() + "\n4\n\n");

        assertTrue(output.contains("Invalid option."));
        assertEquals(2, countOccurrences(output, "Search options:"));
        assertTrue(output.contains("SEARCH RESULTS (1 found)"));
    }

    @Test
    @DisplayName("Result action 1 (view details) prints the found student's full details")
    void viewDetailsFoundTest() {
        Student alice = studentManager.getAllStudents().get(0);

        String output = runWithInput("1\n" + alice.getStudentId() + "\n1\n" + alice.getStudentId() + "\n\n");

        assertTrue(output.contains("Student ID: " + alice.getStudentId()));
        assertTrue(output.contains("Name: " + alice.getName()));
    }

    @Test
    @DisplayName("Result action 1 (view details) reports 'Student not found.' for an unknown ID")
    void viewDetailsNotFoundTest() {
        Student alice = studentManager.getAllStudents().get(0);

        String output = runWithInput("1\n" + alice.getStudentId() + "\n1\nNOPE\n\n");

        assertTrue(output.contains("Student not found."));
    }

    @Test
    @DisplayName("Result action 2 (export) with a filename writes the report and reports success")
    void exportWithFilenameWritesFileTest() throws IOException {
        Student alice = studentManager.getAllStudents().get(0);

        String output = runWithInput("1\n" + alice.getStudentId() + "\n2\nmyexport\n\n");

        assertTrue(output.contains("Results exported to reports/search_myexport.txt"));
        String written = Files.readString(Path.of(testDir + "/search_myexport.txt"));
        assertTrue(written.contains(alice.getStudentId()));
        assertTrue(written.contains(alice.getName()));
        assertTrue(written.contains("Found: 1 students"));
    }

    @Test
    @DisplayName("Result action 2 (export) with an empty filename writes nothing and does not crash")
    void exportWithEmptyFilenameNoOpTest() {
        Student alice = studentManager.getAllStudents().get(0);

        String output = runWithInput("1\n" + alice.getStudentId() + "\n2\n\n\n");

        assertFalse(output.contains("Results exported"));
        assertFalse(new File(testDir).exists(), "exportToFile() must never run when the filename is empty");
    }

    @Test
    @DisplayName("Result action 3 (new search) loops back to the search-options menu")
    void newSearchLoopsBackTest() {
        Student alice = studentManager.getAllStudents().get(0);

        String output = runWithInput("1\n" + alice.getStudentId() + "\n3\n4\n2\n4\n\n");

        assertEquals(2, countOccurrences(output, "Search options:"));
        assertEquals(2, countOccurrences(output, "SEARCH RESULTS"));
    }
}
