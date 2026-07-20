package console;

import dto.StudentDTO;
import exceptions.ApplicationException;
import export.FileExporter;
import manager.StudentManager;
import manager.StudentSearcher;
import mapper.StudentMapper;
import model.enums.StudentType;
import model.student.Student;
import utils.InputSanitizer;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/** Menu option 9: Search Students (has its own result/action sub-menu). */
public class SearchStudentsAction implements MenuAction {

    private final Scanner scanner;
    private final StudentManager studentManager;
    private final StudentSearcher studentSearcher;
    private final FileExporter fileExporter;

    public SearchStudentsAction(Scanner scanner, StudentManager studentManager, StudentSearcher studentSearcher,
                                 FileExporter fileExporter) {
        this.scanner = scanner;
        this.studentManager = studentManager;
        this.studentSearcher = studentSearcher;
        this.fileExporter = fileExporter;
    }

    @Override
    public int getOptionNumber() {
        return 9;
    }

    @Override
    public String getLabel() {
        return "Search Students";
    }

    @Override
    public void execute() {
        System.out.println("\nSEARCH STUDENTS");
        System.out.println("─────────────────────────");

        while (true) {
            System.out.println("\nSearch options:");
            System.out.println("1. By Student ID");
            System.out.println("2. By Name (partial match)");
            System.out.println("3. By Grade Range");
            System.out.println("4. By Student Type");
            System.out.print("Select option (1-4): ");
            String option = scanner.nextLine().trim();

            List<Student> results;
            String rawInput = "";
            String searchDesc;

            switch (option) {
                case "1" -> {
                    System.out.print("Enter Student ID: ");
                    rawInput = InputSanitizer.sanitize(scanner.nextLine());
                    results = studentSearcher.searchById(rawInput);
                }
                case "2" -> {
                    System.out.print("Enter name (partial or full): ");
                    rawInput = InputSanitizer.sanitize(scanner.nextLine());
                    results = studentSearcher.searchByName(rawInput);
                }
                case "3" -> {
                    try {
                        System.out.print("Enter minimum grade: ");
                        double min = Double.parseDouble(scanner.nextLine());
                        System.out.print("Enter maximum grade: ");
                        double max = Double.parseDouble(scanner.nextLine());
                        rawInput = (int) min + "-" + (int) max + "%";
                        results = studentSearcher.searchByGradeRange(min, max);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input.");
                        continue;
                    }
                }
                case "4" -> {
                    System.out.println("Student type:");
                    System.out.println("1. Regular");
                    System.out.println("2. Honors");
                    System.out.print("Select (1-2): ");
                    rawInput = scanner.nextLine().trim();
                    results = studentSearcher.searchByType(rawInput.equals("2") ? StudentType.HONORS : StudentType.REGULAR);
                }
                default -> {
                    System.out.println("Invalid option.");
                    continue;
                }
            }

            searchDesc = studentSearcher.getSearchDescription(option, rawInput);

            List<StudentDTO> resultDtos = results.stream().map(StudentMapper::toDto).collect(Collectors.toList());

            System.out.println("\nSEARCH RESULTS (" + resultDtos.size() + " found)");
            System.out.println("─────────────────────────");
            System.out.printf("%-8s | %-18s | %-9s | %s%n", "STU ID", "NAME", "TYPE", "AVG");
            System.out.println("─────────────────────────");
            for (StudentDTO s : resultDtos) {
                System.out.printf("%-8s | %-18s | %-9s | %.1f%%%n",
                        s.getStudentId(), s.getName(), s.getStudentType(), s.getAverageGrade());
            }

            System.out.println("\nActions:");
            System.out.println("1. View full details for a student");
            System.out.println("2. Export search results");
            System.out.println("3. New search");
            System.out.println("4. Return to main menu");
            System.out.print("Enter choice: ");
            String action = scanner.nextLine().trim();

            if (action.equals("1")) {
                System.out.print("Enter Student ID to view: ");
                String viewId = InputSanitizer.sanitize(scanner.nextLine());
                Student viewS = studentManager.findStudent(viewId);
                if (viewS != null) {
                    viewS.displayStudentDetails();
                } else {
                    System.out.println("Student not found.");
                }
            } else if (action.equals("2")) {
                System.out.print("Enter filename: ");
                String expName = scanner.nextLine().trim();
                if (!expName.isEmpty()) {
                    try {
                        String content = "Search Results: " + searchDesc + "\nFound: " + resultDtos.size() + " students\n\n";
                        for (StudentDTO s : resultDtos) {
                            content += s.getStudentId() + " | " + s.getName() + " | " + s.getStudentType() + " | " + String.format("%.1f%%", s.getAverageGrade()) + "\n";
                        }
                        fileExporter.exportToFile("search_" + expName + ".txt", content);
                        System.out.println("Results exported to reports/search_" + expName + ".txt");
                    } catch (ApplicationException e) {
                        System.out.println("Export failed: " + e.getMessage());
                    }
                }
            } else if (action.equals("3")) {
                continue;
            } else {
                break;
            }

            if (!action.equals("3")) break;
        }

        ConsoleUtils.promptEnter(scanner);
    }
}
