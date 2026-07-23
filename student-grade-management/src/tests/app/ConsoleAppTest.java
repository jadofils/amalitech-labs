package tests.app;

import main.app.ConsoleApp;
import main.console.MenuAction;
import main.model.enums.Role;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Behavioral tests for the menu loop that used to live directly inside
 * Main. Main bound its Scanner to System.in in a static field, which made
 * it impossible to hand it scripted input in a test; ConsoleApp takes its
 * Scanner as a constructor argument instead, so a test can wrap a
 * ByteArrayInputStream and capture System.out to assert on the exact
 * main.console output, the same way a real session would look.
 */
class ConsoleAppTest {

    private String runWithInput(List<MenuAction> actions, String scriptedInput) {
        Scanner scanner = new Scanner(new ByteArrayInputStream(scriptedInput.getBytes(StandardCharsets.UTF_8)));
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            new ConsoleApp(scanner, actions).run();
        } finally {
            System.setOut(originalOut);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    private StubAction exit() {
        return new StubAction(10, "Exit", null, true, null);
    }

    @Test
    void printsEveryActionsLabelWhenRoleBasedAccessIsOffTest() {
        StubAction addStudent = new StubAction(1, "Add Student", null, false, null);
        String output = runWithInput(List.of(addStudent, exit()), "N\n10\n");

        assertTrue(output.contains("1. Add Student"));
        assertTrue(output.contains("10. Exit"));
    }

    @Test
    void hidesAndDeniesAnUnauthorizedActionForAStudentTest() {
        StubAction teacherOnly = new StubAction(1, "Add Student", Role.TEACHER, false, null);
        String output = runWithInput(List.of(teacherOnly, exit()), "Y\n2\n1\n10\n");

        assertFalse(output.contains("1. Add Student"), "unauthorized option must not be listed in the menu");
        assertTrue(output.contains("Access denied"));
        assertEquals(0, teacherOnly.executeCount, "a denied action must never actually execute");
    }

    @Test
    void authorizedActionRemainsAvailableToATeacherTest() {
        StubAction teacherOnly = new StubAction(1, "Add Student", Role.TEACHER, false, null);
        String output = runWithInput(List.of(teacherOnly, exit()), "Y\n1\n1\n10\n");

        assertTrue(output.contains("1. Add Student"));
        assertEquals(1, teacherOnly.executeCount);
    }

    @Test
    void nonNumericInputShowsFriendlyMessageAndDoesNotCrashTest() {
        String output = runWithInput(List.of(exit()), "N\nabc\n10\n");

        assertTrue(output.contains("Invalid input. Please enter a number."));
    }

    @Test
    void unknownOptionNumberShowsInvalidChoiceTest() {
        String output = runWithInput(List.of(exit()), "N\n999\n10\n");

        assertTrue(output.contains("Invalid choice."));
    }

    @Test
    void invalidGradeExceptionOffersRetryAndDeclinesCorrectlyTest() {
        StubAction alwaysFails = new StubAction(3, "Record Grade", null, false,
                count -> new main.exceptions.InvalidGradeException("Grade must be between 0 and 100.", 150));

        String output = runWithInput(List.of(alwaysFails, exit()), "N\n3\nN\n10\n");

        assertTrue(output.contains("ERROR: InvalidGradeException"));
        assertEquals(1, alwaysFails.executeCount, "declining the retry must not call execute() again");
    }

    @Test
    void invalidGradeExceptionRetriesAndSucceedsOnYTest() {
        StubAction failsOnce = new StubAction(3, "Record Grade", null, false,
                count -> count == 1 ? new main.exceptions.InvalidGradeException("bad grade", 150) : null);

        String output = runWithInput(List.of(failsOnce, exit()), "N\n3\nY\n10\n");

        assertTrue(output.contains("ERROR: InvalidGradeException"));
        assertEquals(2, failsOnce.executeCount, "answering Y must retry execute() exactly once more");
    }

    @Test
    void studentNotFoundExceptionPrintsAvailableIdsTest() {
        StubAction notFound = new StubAction(4, "View Grade Report", null, false,
                count -> new main.exceptions.StudentNotFoundException("not found", "STU999", List.of("STU001", "STU002")));

        String output = runWithInput(List.of(notFound, exit()), "N\n4\n10\n");

        assertTrue(output.contains("ERROR: StudentNotFoundException"));
        assertTrue(output.contains("Available student IDs: STU001, STU002"));
    }

    @Test
    void exportExceptionPrintsTheFilePathTest() {
        StubAction exportFails = new StubAction(5, "Export Grade Report", null, false,
                count -> new main.exceptions.ExportException("write failed", "reports/x.txt", new RuntimeException("io")));

        String output = runWithInput(List.of(exportFails, exit()), "N\n5\n10\n");

        assertTrue(output.contains("ERROR: ExportException"));
        assertTrue(output.contains("File: reports/x.txt"));
    }

    @Test
    void importExceptionPrintsTheFilePathTest() {
        StubAction importFails = new StubAction(7, "Bulk Import Grades", null, false,
                count -> new main.exceptions.ImportException("read failed", "main.imports/x.csv", new RuntimeException("io")));

        String output = runWithInput(List.of(importFails, exit()), "N\n7\n10\n");

        assertTrue(output.contains("ERROR: ImportException"));
        assertTrue(output.contains("File: main.imports/x.csv"));
    }

    @Test
    void unnamedApplicationExceptionStillReachesTheFinalCatchAllTest() {
        StubAction genericFailure = new StubAction(8, "View Class Statistics", null, false,
                count -> new main.exceptions.CSVImportException("unexpected failure"));

        String output = runWithInput(List.of(genericFailure, exit()), "N\n8\n10\n");

        assertTrue(output.contains("ERROR: CSVImportException"));
    }

    @Test
    void terminatingActionEndsTheLoopWithoutRunningAnotherMenuCycleTest() {
        StubAction exitAction = exit();
        String output = runWithInput(List.of(exitAction), "N\n10\n");

        // Only ever printed the menu once - the loop returned instead of looping again.
        assertEquals(1, countOccurrences(output, "MAIN MENU"));
    }

    private int countOccurrences(String haystack, String needle) {
        int count = 0, index = 0;
        while ((index = haystack.indexOf(needle, index)) != -1) {
            count++;
            index += needle.length();
        }
        return count;
    }

    /** Minimal, fully-controllable MenuAction test double. */
    private static class StubAction implements MenuAction {
        private final int optionNumber;
        private final String label;
        private final Role requiredRole;
        private final boolean terminates;
        private final IntFunction<RuntimeException> thrower;
        int executeCount = 0;

        StubAction(int optionNumber, String label, Role requiredRole, boolean terminates, IntFunction<RuntimeException> thrower) {
            this.optionNumber = optionNumber;
            this.label = label;
            this.requiredRole = requiredRole;
            this.terminates = terminates;
            this.thrower = thrower;
        }

        @Override public int getOptionNumber() { return optionNumber; }
        @Override public String getLabel() { return label; }
        @Override public boolean isAuthorizedFor(Role role) { return requiredRole == null || requiredRole == role; }
        @Override public boolean terminatesLoop() { return terminates; }

        @Override
        public void execute() {
            executeCount++;
            RuntimeException ex = thrower == null ? null : thrower.apply(executeCount);
            if (ex != null) {
                throw ex;
            }
        }
    }
}
