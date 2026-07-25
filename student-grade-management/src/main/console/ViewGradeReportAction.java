package main.console;

import main.manager.GradeManager;
import main.manager.StudentManager;
import main.model.student.Student;
import main.utils.InputSanitizer;

import java.util.Scanner;

/** Menu option 4: View Grade Report. */
public class ViewGradeReportAction extends AbstractGradeAction {

    public ViewGradeReportAction(Scanner scanner, StudentManager studentManager, GradeManager gradeManager) {
        super(4, "View Grade Report", scanner, studentManager, gradeManager);
    }

    @Override
    public void execute() {
        System.out.println("\nVIEW GRADE REPORT");
        System.out.println(ConsoleUtils.DIVIDER);

        System.out.print("Enter Student ID: ");
        String studentId = InputSanitizer.sanitize(scanner.nextLine());

        Student student = ConsoleUtils.requireStudent(studentManager, studentId);

        System.out.println("\nStudent: " + studentId + " - " + student.getName());
        System.out.println("Type: " + student.getStudentType() + " Student");
        System.out.printf("Passing Grade: %.0f%%%n", student.getPassingGrade());

        gradeManager.viewGradesByStudent(studentId);

        ConsoleUtils.promptEnter(scanner);
    }
}
