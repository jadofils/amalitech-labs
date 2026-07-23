package main.app;

import main.console.MenuAction;
import main.exceptions.ApplicationException;
import main.exceptions.ExportException;
import main.exceptions.GradeException;
import main.exceptions.ImportException;
import main.exceptions.InvalidGradeException;
import main.exceptions.StudentException;
import main.exceptions.StudentNotFoundException;
import main.exceptions.StudentValidationException;
import main.exceptions.SubjectException;
import main.exceptions.SubjectNotFoundException;
import main.exceptions.SubjectValidationException;
import main.logging.Logger;
import main.model.enums.Role;

import java.util.List;
import java.util.Scanner;

/**
 * The interactive menu loop: prints the menu, reads a choice, dispatches it
 * to the matching {@link MenuAction}, and translates a thrown exception
 * into a main.console message. Extracted out of {@code Main} specifically so it
 * can be constructed with any {@link Scanner} - including one wrapping a
 * scripted, in-memory input stream in a test - rather than being permanently
 * bound to {@code System.in} the way a {@code static final} field on Main
 * used to be. {@code Main} itself now only builds the dependency graph and
 * hands it here.
 */
public class ConsoleApp {

    private final Scanner scanner;
    private final List<MenuAction> actions;

    private boolean useRoleBased = false;
    private Role currentRole = Role.TEACHER;

    public ConsoleApp(Scanner scanner, List<MenuAction> actions) {
        this.scanner = scanner;
        this.actions = actions;
    }

    public void run() {
        askRoleBased();

        boolean running = true;
        while (running) {
            printMenu();
            MenuAction action = resolveAuthorizedAction();
            if (action != null) {
                running = !executeAction(action);
            }
        }
    }

    /**
     * Reads one menu choice and resolves it to an authorized action, printing
     * the appropriate rejection message and returning {@code null} for any
     * of the three ways that can fail - never {@code continue}s the caller's
     * loop directly, so {@link #run()} itself has none.
     */
    private MenuAction resolveAuthorizedAction() {
        Integer choice = readChoice();
        if (choice == null) {
            System.out.println("Invalid input. Please enter a number.");
            return null;
        }

        MenuAction action = findAction(choice);
        if (action == null) {
            System.out.println("Invalid choice.");
            return null;
        }

        if (useRoleBased && !action.isAuthorizedFor(currentRole)) {
            System.out.println("Access denied. This action is not available for your role.");
            return null;
        }

        return action;
    }

    private Integer readChoice() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Runs one action, translating any thrown exception into a main.console message. Returns true once the loop should stop. */
    private boolean executeAction(MenuAction action) {
        try {
            action.execute();
            return action.terminatesLoop();
        } catch (InvalidGradeException e) {
            handleInvalidGrade(action, e);
        } catch (StudentNotFoundException e) {
            handleStudentNotFound(e);
        } catch (ExportException e) {
            handleFileException("ExportException", e.getMessage(), e.getFilePath());
        } catch (ImportException e) {
            handleFileException("ImportException", e.getMessage(), e.getFilePath());
        } catch (StudentValidationException | StudentException | GradeException
                 | SubjectNotFoundException | SubjectValidationException | SubjectException e) {
            Logger.warn("Menu action " + action.getOptionNumber() + " rejected: " + e.getMessage());
            printError(e);
        } catch (ApplicationException e) {
            // Catches any other custom exception not named above -
            // deliberately not `catch (Exception e)`, so a genuinely
            // unexpected failure still surfaces instead of being masked.
            Logger.error("Unhandled application exception for menu action " + action.getOptionNumber(), e);
            printError(e);
        }
        return false;
    }

    private void handleInvalidGrade(MenuAction action, InvalidGradeException e) {
        System.out.println("\n✗ ERROR: InvalidGradeException");
        System.out.println("  " + e.getMessage());
        System.out.println("  Try again? (Y/N): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("Y")) {
            action.execute();
        }
    }

    private void handleStudentNotFound(StudentNotFoundException e) {
        System.out.println("\n✗ ERROR: StudentNotFoundException");
        System.out.println("  " + e.getMessage());
        if (e.getAvailableIds() != null && !e.getAvailableIds().isEmpty()) {
            System.out.println("  Available student IDs: " + String.join(", ", e.getAvailableIds()));
        }
    }

    private void handleFileException(String exceptionName, String message, String filePath) {
        System.out.println("\n✗ ERROR: " + exceptionName);
        System.out.println("  " + message);
        if (filePath != null) {
            System.out.println("  File: " + filePath);
        }
    }

    private void printError(ApplicationException e) {
        System.out.println("\n✗ ERROR: " + e.getClass().getSimpleName());
        System.out.println("  " + e.getMessage());
    }

    private MenuAction findAction(int choice) {
        for (MenuAction action : actions) {
            if (action.getOptionNumber() == choice) {
                return action;
            }
        }
        return null;
    }

    private void askRoleBased() {
        System.out.print("Enable role-based access control? (Y/N, default N): ");
        String input = scanner.nextLine().trim();
        if (!input.equalsIgnoreCase("Y")) {
            return;
        }
        useRoleBased = true;

        while (true) {
            System.out.println("\nSelect your role:");
            System.out.println("1. Teacher");
            System.out.println("2. Student");
            System.out.print("Choose (1-2): ");
            String roleInput = scanner.nextLine().trim();
            if (roleInput.equals("1")) {
                currentRole = Role.TEACHER;
                return;
            } else if (roleInput.equals("2")) {
                currentRole = Role.STUDENT;
                return;
            }
            System.out.println("Invalid choice. Please select 1 or 2.");
        }
    }

    private void printMenu() {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║   STUDENT GRADE MANAGEMENT - MAIN MENU    ║");
        System.out.println("╚═══════════════════════════════════════╝");
        System.out.println();
        for (MenuAction action : actions) {
            if (!useRoleBased || action.isAuthorizedFor(currentRole)) {
                System.out.println(action.getOptionNumber() + ". " + action.getLabel());
            }
        }
        if (useRoleBased) {
            System.out.println("Role: " + (currentRole == Role.TEACHER ? "Teacher" : "Student"));
        }
        System.out.println();
        System.out.print("Enter choice: ");
    }
}
