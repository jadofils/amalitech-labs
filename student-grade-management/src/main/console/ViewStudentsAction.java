package main.console;

import main.manager.StudentManager;
import main.model.enums.Role;
import main.model.student.HonorsStudent;
import main.model.student.Student;

import java.util.List;
import java.util.Scanner;

/** Menu option 2: View Students. */
public class ViewStudentsAction implements MenuAction {

    private final Scanner scanner;
    private final StudentManager studentManager;

    public ViewStudentsAction(Scanner scanner, StudentManager studentManager) {
        this.scanner = scanner;
        this.studentManager = studentManager;
    }

    @Override
    public int getOptionNumber() {
        return 2;
    }

    @Override
    public String getLabel() {
        return "View Students";
    }

    @Override
    public boolean isAuthorizedFor(Role role) {
        return role == Role.TEACHER;
    }

    @Override
    public void execute() {
        System.out.println("\nSTUDENT LISTING");
        System.out.println(ConsoleUtils.WIDE_DIVIDER);
        System.out.printf("%-8s | %-18s | %-12s | %-9s | %s%n", "STU ID", "NAME", "TYPE", "AVG GRADE", "STATUS");
        System.out.println(ConsoleUtils.WIDE_DIVIDER);

        List<Student> students = studentManager.getAllStudents();
        double classTotal = 0;

        for (Student student : students) {
            String status = student.isPassing() ? "Passing" : "Failing";
            System.out.printf("%-8s | %-18s | %-12s | %.1f%%     | %s%n",
                    student.getStudentId(), student.getName(),
                    student.getStudentType(), student.calculateAverageGrade(), status);

            int enrolledCount = student.getGrades().size();
            if (student instanceof HonorsStudent hs) {
                String honorsStatus = hs.checkHonorsEligibility() ? " | Honors Eligible" : "";
                System.out.printf("          | Enrolled Subjects: %d | Passing Grade: %.0f%%%s%n",
                        enrolledCount, student.getPassingGrade(), honorsStatus);
            } else {
                System.out.printf("          | Enrolled Subjects: %d | Passing Grade: %.0f%%%n",
                        enrolledCount, student.getPassingGrade());
            }
            System.out.println(ConsoleUtils.WIDE_DIVIDER);
            classTotal += student.calculateAverageGrade();
        }

        System.out.printf("%nTotal Students: %d%n", students.size());
        if (!students.isEmpty()) {
            System.out.printf("Average Class Grade: %.1f%%%n", classTotal / students.size());
        }

        ConsoleUtils.promptEnter(scanner);
    }
}
