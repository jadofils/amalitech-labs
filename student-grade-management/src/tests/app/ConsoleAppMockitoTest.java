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

import static org.mockito.Mockito.*;

/**
 * Mocks every MenuAction to verify ConsoleApp's own dispatch logic in
 * isolation: which action gets called, how many times, and whether the
 * role check runs before execute() - independent of any action's real
 * behavior (that belongs to each action's own test class).
 */
class ConsoleAppMockitoTest {

    private void run(List<MenuAction> actions, String scriptedInput) {
        Scanner scanner = new Scanner(new ByteArrayInputStream(scriptedInput.getBytes(StandardCharsets.UTF_8)));
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
            new ConsoleApp(scanner, actions).run();
        } finally {
            System.setOut(originalOut);
        }
    }

    private MenuAction mockAction(int optionNumber, String label, boolean authorized, boolean terminates) {
        MenuAction action = mock(MenuAction.class);
        when(action.getOptionNumber()).thenReturn(optionNumber);
        when(action.getLabel()).thenReturn(label);
        when(action.isAuthorizedFor(any())).thenReturn(authorized);
        when(action.terminatesLoop()).thenReturn(terminates);
        return action;
    }

    @Test
    void dispatchesToExactlyOneMatchingActionByOptionNumberTest() {
        MenuAction one = mockAction(1, "One", true, false);
        MenuAction two = mockAction(2, "Two", true, false);
        MenuAction exit = mockAction(10, "Exit", true, true);

        run(List.of(one, two, exit), "N\n2\n10\n");

        verify(one, never()).execute();
        verify(two, times(1)).execute();
        verify(exit, times(1)).execute();
    }

    @Test
    void checksAuthorizationBeforeExecutingWhenRoleBasedAccessIsOnTest() {
        MenuAction teacherOnly = mockAction(1, "Add Student", false, false);
        MenuAction exit = mockAction(10, "Exit", true, true);

        run(List.of(teacherOnly, exit), "Y\n2\n1\n10\n");

        verify(teacherOnly, atLeastOnce()).isAuthorizedFor(Role.STUDENT);
        verify(teacherOnly, never()).execute();
    }

    @Test
    void doesNotCheckAuthorizationAtAllWhenRoleBasedAccessIsOffTest() {
        MenuAction action = mockAction(1, "Add Student", true, false);
        MenuAction exit = mockAction(10, "Exit", true, true);

        run(List.of(action, exit), "N\n1\n10\n");

        verify(action, times(1)).execute();
        verify(action, never()).isAuthorizedFor(any());
    }

    @Test
    void stopsCallingExecuteOnAnyActionOnceATerminatingActionRunsTest() {
        MenuAction exit = mockAction(10, "Exit", true, true);
        MenuAction unreachable = mockAction(1, "Add Student", true, false);

        run(List.of(exit, unreachable), "N\n10\n");

        verify(exit, times(1)).execute();
        verify(unreachable, never()).execute();
    }

    @Test
    void retriesExecuteExactlyOnceMoreWhenUserConfirmsAfterInvalidGradeExceptionTest() {
        MenuAction recordGrade = mockAction(3, "Record Grade", true, false);
        MenuAction exit = mockAction(10, "Exit", true, true);
        doThrow(new main.exceptions.InvalidGradeException("bad", 150))
                .doNothing()
                .when(recordGrade).execute();

        run(List.of(recordGrade, exit), "N\n3\nY\n10\n");

        verify(recordGrade, times(2)).execute();
    }
}
