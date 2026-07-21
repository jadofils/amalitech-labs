import calculators.GPACalculator;
import calculators.StatisticsCalculator;
import console.AddStudentAction;
import console.BulkImportAction;
import console.CalculateGpaAction;
import console.ClassStatisticsAction;
import console.ExitAction;
import console.ExportGradeReportAction;
import console.MenuAction;
import console.RecordGradeAction;
import console.SearchStudentsAction;
import console.ViewGradeReportAction;
import console.ViewStudentsAction;
import exceptions.ApplicationException;
import exceptions.ExportException;
import exceptions.ImportException;
import exceptions.InvalidGradeException;
import exceptions.StudentNotFoundException;
import exceptions.StudentValidationException;
import exceptions.GradeException;
import exceptions.SubjectNotFoundException;
import exceptions.SubjectValidationException;
import export.FileExporter;
import export.ReportGenerator;
import imports.BulkImportService;
import logging.Logger;
import manager.GradeManager;
import manager.StudentManager;
import manager.StudentSearcher;
import model.enums.Role;
import repository.student.StudentRepository;
import repository.student.StudentRepositoryImpl;
import repository.subject.SubjectRepository;
import repository.subject.SubjectRepositoryImpl;
import service.GradeService;
import service.StudentService;
import service.GradeServiceImpl;
import service.StudentServiceImpl;

import java.util.List;
import java.util.Scanner;

/**
 * Console entry point: builds the dependency graph (composition root),
 * prints the menu, and dispatches each chosen number to its
 * {@link MenuAction}. Per-feature behavior lives entirely in the
 * {@code console} package's action classes - this class only owns the
 * session-level concerns that don't belong to any single feature: the
 * role-based-access toggle and translating a thrown exception into a
 * console message.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    private static final StudentRepository studentRepository = new StudentRepositoryImpl();
    private static final SubjectRepository subjectRepository = new SubjectRepositoryImpl();
    private static final GradeService gradeService = new GradeServiceImpl(studentRepository, subjectRepository);
    private static final GradeManager gradeManager = new GradeManager(gradeService, subjectRepository);
    private static final StudentService studentService = new StudentServiceImpl(studentRepository);
    private static final StudentManager studentManager = new StudentManager(studentService, gradeManager);
    private static final ReportGenerator reportGenerator = new ReportGenerator(gradeManager, studentManager);
    private static final FileExporter fileExporter = new FileExporter();
    private static final GPACalculator gpaCalculator = new GPACalculator(gradeManager, studentManager);
    private static final BulkImportService bulkImportService = new BulkImportService(subjectRepository, studentManager, gradeManager);
    private static final StatisticsCalculator statisticsCalculator = new StatisticsCalculator();
    private static final StudentSearcher studentSearcher = new StudentSearcher(studentManager);

    private static final List<MenuAction> actions = List.of(
            new AddStudentAction(scanner, studentManager),
            new ViewStudentsAction(scanner, studentManager),
            new RecordGradeAction(scanner, studentManager, gradeManager),
            new ViewGradeReportAction(scanner, studentManager, gradeManager),
            new ExportGradeReportAction(scanner, studentManager, gradeManager, reportGenerator, fileExporter),
            new CalculateGpaAction(scanner, studentManager, gradeManager, gpaCalculator),
            new BulkImportAction(scanner, bulkImportService),
            new ClassStatisticsAction(scanner, studentManager, gradeManager, statisticsCalculator, subjectRepository),
            new SearchStudentsAction(scanner, studentManager, studentSearcher, fileExporter),
            new ExitAction()
    );

    private static boolean useRoleBased = false;
    private static Role currentRole = Role.TEACHER;

    public static void main(String[] args) {
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

    private static MenuAction findAction(int choice) {
        for (MenuAction action : actions) {
            if (action.getOptionNumber() == choice) {
                return action;
            }
        }
        return null;
    }

    private static void askRoleBased() {
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

    private static void printMenu() {
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
