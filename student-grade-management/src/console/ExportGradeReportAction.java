package console;

import exceptions.StudentNotFoundException;
import export.FileExporter;
import export.ReportGenerator;
import manager.GradeManager;
import manager.StudentManager;
import model.student.Student;
import utils.InputSanitizer;

import java.util.Scanner;

/** Menu option 5: Export Grade Report. */
public class ExportGradeReportAction implements MenuAction {

    private final Scanner scanner;
    private final StudentManager studentManager;
    private final GradeManager gradeManager;
    private final ReportGenerator reportGenerator;
    private final FileExporter fileExporter;

    public ExportGradeReportAction(Scanner scanner, StudentManager studentManager, GradeManager gradeManager,
                                    ReportGenerator reportGenerator, FileExporter fileExporter) {
        this.scanner = scanner;
        this.studentManager = studentManager;
        this.gradeManager = gradeManager;
        this.reportGenerator = reportGenerator;
        this.fileExporter = fileExporter;
    }

    @Override
    public int getOptionNumber() {
        return 5;
    }

    @Override
    public String getLabel() {
        return "Export Grade Report";
    }

    @Override
    public void execute() {
        System.out.println("\nEXPORT GRADE REPORT");
        System.out.println("─────────────────────────");

        System.out.print("Enter Student ID: ");
        String studentId = InputSanitizer.sanitize(scanner.nextLine());

        Student student = studentManager.findStudent(studentId);
        if (student == null) {
            throw new StudentNotFoundException("Student with ID '" + studentId + "' not found.",
                    studentId, ConsoleUtils.getAvailableStudentIds(studentManager));
        }

        System.out.println("\nStudent: " + studentId + " - " + student.getName());
        System.out.println("Type: " + student.getStudentType() + " Student");
        System.out.println("Total Grades: " + gradeManager.getGradesForStudent(studentId).size());

        System.out.println("\nExport options:");
        System.out.println("1. Summary Report (overview only)");
        System.out.println("2. Detailed Report (all grades)");
        System.out.println("3. Both");
        System.out.print("Select option (1-3): ");
        String option = scanner.nextLine().trim();

        System.out.print("\nEnter filename (without extension): ");
        String filename = scanner.nextLine().trim();

        if (filename.isEmpty()) {
            System.out.println("Filename cannot be empty.");
            return;
        }

        boolean summary = option.equals("1") || option.equals("3");
        boolean detailed = option.equals("2") || option.equals("3");

        int filesCreated = 0;
        long totalSize = 0;

        if (summary) {
            String content = reportGenerator.exportSummary(studentId);
            FileExporter.FileExportResult result = fileExporter.exportToFile(filename + "_summary.txt", content);
            filesCreated++;
            totalSize += result.getSize();
        }

        if (detailed) {
            String content = reportGenerator.exportDetailed(studentId);
            FileExporter.FileExportResult result = fileExporter.exportToFile(filename + "_detailed.txt", content);
            filesCreated++;
            totalSize += result.getSize();
        }

        System.out.println("\n✓ Report exported successfully!");
        for (int i = 0; i < filesCreated; i++) {
            String suffix = summary && detailed ? (i == 0 ? "_summary" : "_detailed") : "";
            System.out.println("  File: " + filename + suffix + ".txt");
        }
        System.out.println("  Location: ./reports/");
        System.out.println("  Size: " + (totalSize / 1024.0) + " KB");
        System.out.println("  Contains: " + gradeManager.getGradesForStudent(studentId).size() + " grades");

        ConsoleUtils.promptEnter(scanner);
    }
}
