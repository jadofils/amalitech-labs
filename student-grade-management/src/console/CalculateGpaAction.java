package console;

import calculators.GPACalculator;
import exceptions.StudentNotFoundException;
import manager.GradeManager;
import manager.StudentManager;
import model.grade.Grade;
import model.student.HonorsStudent;
import model.student.Student;
import utils.InputSanitizer;

import java.util.List;
import java.util.Scanner;

/** Menu option 6: Calculate Student GPA. */
public class CalculateGpaAction implements MenuAction {

    private final Scanner scanner;
    private final StudentManager studentManager;
    private final GradeManager gradeManager;
    private final GPACalculator gpaCalculator;

    public CalculateGpaAction(Scanner scanner, StudentManager studentManager, GradeManager gradeManager,
                               GPACalculator gpaCalculator) {
        this.scanner = scanner;
        this.studentManager = studentManager;
        this.gradeManager = gradeManager;
        this.gpaCalculator = gpaCalculator;
    }

    @Override
    public int getOptionNumber() {
        return 6;
    }

    @Override
    public String getLabel() {
        return "Calculate Student GPA";
    }

    @Override
    public void execute() {
        System.out.println("\nCALCULATE STUDENT GPA");
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
        System.out.printf("Overall Average: %.1f%%%n", student.calculateAverageGrade());

        System.out.println("\nGPA CALCULATION (4.0 Scale)");
        System.out.println("─────────────────────────");
        System.out.printf("%-16s | %-7s | %s%n", "Subject", "Grade", "GPA Points");
        System.out.println("─────────────────────────");

        List<Grade> grades = gradeManager.getGradesForStudent(studentId);
        double cumulativeGPA = gpaCalculator.cumulativeGPA(studentId);

        for (Grade g : grades) {
            double gpa = gpaCalculator.percentageToGPA(g.getGrade());
            System.out.printf("%-16s | %.0f%%    | %.1f (%s)%n",
                    g.getSubject().getSubjectName(), g.getGrade(), gpa, gpaCalculator.gpaToLetter(gpa));
        }

        System.out.println("─────────────────────────");
        System.out.printf("Cumulative GPA: %.2f / 4.0%n", cumulativeGPA);
        System.out.println("Letter Grade: " + gpaCalculator.gpaToLetter(cumulativeGPA));

        List<Student> allStudents = studentManager.getAllStudents();
        int rank = gpaCalculator.classRank(studentId);
        System.out.println("Class Rank: " + rank + " of " + allStudents.size());

        System.out.println("\nPerformance Analysis:");
        if (cumulativeGPA >= 3.5) {
            System.out.println("✓ Excellent performance (3.5+ GPA)");
        } else if (cumulativeGPA >= 3.0) {
            System.out.println("✓ Good performance (3.0+ GPA)");
        } else if (cumulativeGPA >= 2.0) {
            System.out.println("✓ Satisfactory performance (2.0+ GPA)");
        } else {
            System.out.println("✗ Needs improvement (below 2.0 GPA)");
        }
        if (student instanceof HonorsStudent hs && hs.checkHonorsEligibility()) {
            System.out.println("✓ Honors eligibility maintained");
        }
        double classAvg = allStudents.isEmpty() ? 0 : allStudents.stream().mapToDouble(Student::calculateAverageGrade).average().orElse(0);
        double classAvgGPA = gpaCalculator.percentageToGPA(classAvg);
        if (cumulativeGPA > classAvgGPA) {
            System.out.printf("✓ Above class average (%.2f GPA)%n", classAvgGPA);
        } else {
            System.out.printf("✗ Below class average (%.2f GPA)%n", classAvgGPA);
        }

        ConsoleUtils.promptEnter(scanner);
    }
}
