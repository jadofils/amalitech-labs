package app;

import console.MenuAction;
import exceptions.ApplicationException;
import exceptions.ExportException;
import exceptions.GradeException;
import exceptions.ImportException;
import exceptions.InvalidGradeException;
import exceptions.StudentNotFoundException;
import exceptions.StudentValidationException;
import exceptions.SubjectNotFoundException;
import exceptions.SubjectValidationException;
import logging.Logger;
import model.enums.Role;

import java.util.List;
import java.util.Scanner;

/**
 * The interactive menu loop: prints the menu, reads a choice, dispatches it
 * to the matching {@link MenuAction}, and translates a thrown exception
 * into a console message. Extracted out of {@code Main} specifically so it
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

        while (true) {
            printMenu();

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            MenuAction action = findAction(choice);
            if (action == null) {
                System.out.println("Invalid choice.");
                continue;
            }

            if (useRoleBased && !action.isAuthorizedFor(currentRole)) {
                System.out.println("Access denied. This action is not available for your role.");
                continue;
            }

            try {
                action.execute();
                if (action.terminatesLoop()) {
                    return;
                }
            } catch (InvalidGradeException e) {
                System.out.println("\n✗ ERROR: InvalidGradeException");
                System.out.println("  " + e.getMessage());
                System.out.println("  Try again? (Y/N): ");
                if (scanner.nextLine().trim().equalsIgnoreCase("Y")) {
                    action.execute();
                }
            } catch (StudentNotFoundException e) {
                System.out.println("\n✗ ERROR: StudentNotFoundException");
                System.out.println("  " + e.getMessage());
                if (e.getAvailableIds() != null && !e.getAvailableIds().isEmpty()) {
                    System.out.println("  Available student IDs: " + String.join(", ", e.getAvailableIds()));
                }
            } catch (ExportException e) {
                System.out.println("\n✗ ERROR: ExportException");
                System.out.println("  " + e.getMessage());
                if (e.getFilePath() != null) {
                    System.out.println("  File: " + e.getFilePath());
                }
            } catch (ImportException e) {
                System.out.println("\n✗ ERROR: ImportException");
                System.out.println("  " + e.getMessage());
                if (e.getFilePath() != null) {
                    System.out.println("  File: " + e.getFilePath());
                }
            } catch (StudentValidationException | GradeException
                     | SubjectNotFoundException | SubjectValidationException e) {
                Logger.warn("Menu action " + choice + " rejected: " + e.getMessage());
                System.out.println("\n✗ ERROR: " + e.getClass().getSimpleName());
                System.out.println("  " + e.getMessage());
            } catch (ApplicationException e) {
                // Catches any other custom exception not named above -
                // deliberately not `catch (Exception e)`, so a genuinely
                // unexpected failure still surfaces instead of being masked.
                Logger.error("Unhandled application exception for menu action " + choice, e);
                System.out.println("\n✗ ERROR: " + e.getClass().getSimpleName());
                System.out.println("  " + e.getMessage());
            }
        }
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
