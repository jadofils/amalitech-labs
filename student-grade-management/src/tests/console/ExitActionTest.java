package tests.console;

import console.ExitAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// ExitAction has no collaborators at all (no constructor args), so - per the
// same reasoning as LetterGradeTest/InputSanitizerTest - there is no
// ExitActionMockitoTest counterpart; see tests/README.md.
class ExitActionTest {

    private final ExitAction exitAction = new ExitAction();

    @Test
    @DisplayName("getOptionNumber() is 10")
    void getOptionNumberTest() {
        assertEquals(10, exitAction.getOptionNumber());
    }

    @Test
    @DisplayName("getLabel() is \"Exit\"")
    void getLabelTest() {
        assertEquals("Exit", exitAction.getLabel());
    }

    @Test
    @DisplayName("terminatesLoop() is true")
    void terminatesLoopTest() {
        assertTrue(exitAction.terminatesLoop());
    }

    @Test
    @DisplayName("execute() prints a goodbye message to stdout")
    void executePrintsGoodbyeMessageTest() {
        String output = captureStdOut(exitAction::execute);

        assertTrue(output.contains("Thank you for using Student Grade Management System!"));
        assertTrue(output.contains("Goodbye!"));
    }

    private String captureStdOut(Runnable action) {
        java.io.PrintStream original = System.out;
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(buffer));
        try {
            action.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString();
    }
}
