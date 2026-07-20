package console;

import exceptions.StudentNotFoundException;
import manager.GradeManager;
import manager.StudentManager;
import model.student.Student;
import utils.InputSanitizer;

import java.util.Scanner;

/** Menu option 4: View Grade Report. */
public class ViewGradeReportAction implements MenuAction {

    private final Scanner scanner;
    private final StudentManager studentManager;
    private final GradeManager gradeManager;

    public ViewGradeReportAction(Scanner scanner, StudentManager studentManager, GradeManager gradeManager) {
        this.scanner = scanner;
        this.studentManager = studentManager;
        this.gradeManager = gradeManager;
    }

    @Override
    public int getOptionNumber() {
        return 4;
    }

    @Override
    public String getLabel() {
        return "View Grade Report";
    }

    @Override
    public void execute() {
        System.out.println("\nVIEW GRADE REPORT");
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
        System.out.printf("Passing Grade: %.0f%%%n", student.getPassingGrade());

        gradeManager.viewGradesByStudent(studentId);

        ConsoleUtils.promptEnter(scanner);
    }
}
