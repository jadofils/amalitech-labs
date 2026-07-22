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
        System.out.println(ConsoleUtils.DIVIDER);

        boolean searching = true;
        while (searching) {
            searching = runSearchRound();
        }

        ConsoleUtils.promptEnter(scanner);
    }

    /** Runs one search + result-action round. Returns true if the user asked for another search. */
    private boolean runSearchRound() {
        String option = promptSearchOption();
        SearchQuery query = readSearchQuery(option);
        if (query == null) {
            return true;
        }

        String searchDesc = studentSearcher.getSearchDescription(option, query.rawInput);
        List<StudentDTO> resultDtos = query.results.stream().map(StudentMapper::toDto).collect(Collectors.toList());
        printSearchResults(resultDtos);

        return handleResultAction(searchDesc, resultDtos);
    }

    private String promptSearchOption() {
        System.out.println("\nSearch options:");
        System.out.println("1. By Student ID");
        System.out.println("2. By Name (partial match)");
        System.out.println("3. By Grade Range");
        System.out.println("4. By Student Type");
        System.out.print("Select option (1-4): ");
        return scanner.nextLine().trim();
    }

    /** Reads whichever extra input the chosen option needs and runs the search. Null means: already reported, retry. */
    private SearchQuery readSearchQuery(String option) {
        switch (option) {
            case "1": {
                System.out.print("Enter Student ID: ");
                String id = InputSanitizer.sanitize(scanner.nextLine());
                return new SearchQuery(studentSearcher.searchById(id), id);
            }
            case "2": {
                System.out.print("Enter name (partial or full): ");
                String name = InputSanitizer.sanitize(scanner.nextLine());
                return new SearchQuery(studentSearcher.searchByName(name), name);
            }
            case "3":
                return readGradeRangeQuery();
            case "4":
                return readStudentTypeQuery();
            default:
                System.out.println("Invalid option.");
                return null;
        }
    }

    private SearchQuery readGradeRangeQuery() {
        try {
            System.out.print("Enter minimum grade: ");
            double min = Double.parseDouble(scanner.nextLine());
            System.out.print("Enter maximum grade: ");
            double max = Double.parseDouble(scanner.nextLine());
            String rawInput = (int) min + "-" + (int) max + "%";
            return new SearchQuery(studentSearcher.searchByGradeRange(min, max), rawInput);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return null;
        }
    }

    private SearchQuery readStudentTypeQuery() {
        System.out.println("Student type:");
        System.out.println("1. Regular");
        System.out.println("2. Honors");
        System.out.print("Select (1-2): ");
        String typeChoice = scanner.nextLine().trim();
        StudentType type = typeChoice.equals("2") ? StudentType.HONORS : StudentType.REGULAR;
        return new SearchQuery(studentSearcher.searchByType(type), typeChoice);
    }

    private void printSearchResults(List<StudentDTO> resultDtos) {
        System.out.println("\nSEARCH RESULTS (" + resultDtos.size() + " found)");
        System.out.println(ConsoleUtils.DIVIDER);
        System.out.printf("%-8s | %-18s | %-9s | %s%n", "STU ID", "NAME", "TYPE", "AVG");
        System.out.println(ConsoleUtils.DIVIDER);
        for (StudentDTO s : resultDtos) {
            System.out.printf("%-8s | %-18s | %-9s | %.1f%%%n",
                    s.getStudentId(), s.getName(), s.getStudentType(), s.getAverageGrade());
        }
    }

    /** Runs the chosen post-search action. Returns true only when the user asked for a new search. */
    private boolean handleResultAction(String searchDesc, List<StudentDTO> resultDtos) {
        System.out.println("\nActions:");
        System.out.println("1. View full details for a student");
        System.out.println("2. Export search results");
        System.out.println("3. New search");
        System.out.println("4. Return to main menu");
        System.out.print("Enter choice: ");
        String action = scanner.nextLine().trim();

        if (action.equals("1")) {
            viewStudentDetails();
        } else if (action.equals("2")) {
            exportSearchResults(searchDesc, resultDtos);
        }

        return action.equals("3");
    }

    private void viewStudentDetails() {
        System.out.print("Enter Student ID to view: ");
        String viewId = InputSanitizer.sanitize(scanner.nextLine());
        Student viewS = studentManager.findStudent(viewId);
        if (viewS != null) {
            viewS.displayStudentDetails();
        } else {
            System.out.println("Student not found.");
        }
    }

    private void exportSearchResults(String searchDesc, List<StudentDTO> resultDtos) {
        System.out.print("Enter filename: ");
        String expName = scanner.nextLine().trim();
        if (expName.isEmpty()) {
            return;
        }
        try {
            fileExporter.exportToFile("search_" + expName + ".txt", buildExportContent(searchDesc, resultDtos));
            System.out.println("Results exported to reports/search_" + expName + ".txt");
        } catch (ApplicationException e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    private String buildExportContent(String searchDesc, List<StudentDTO> resultDtos) {
        StringBuilder content = new StringBuilder();
        content.append("Search Results: ").append(searchDesc)
                .append("\nFound: ").append(resultDtos.size()).append(" students\n\n");
        for (StudentDTO s : resultDtos) {
            content.append(s.getStudentId()).append(" | ").append(s.getName()).append(" | ")
                    .append(s.getStudentType()).append(" | ")
                    .append(String.format("%.1f%%", s.getAverageGrade())).append("\n");
        }
        return content.toString();
    }

    /** One search's raw results plus the raw input used to run it (needed to build the search description). */
    private static final class SearchQuery {
        final List<Student> results;
        final String rawInput;

        SearchQuery(List<Student> results, String rawInput) {
            this.results = results;
            this.rawInput = rawInput;
        }
    }
}
