package console;

import exceptions.StudentNotFoundException;
import manager.GradeManager;
import manager.StudentManager;
import model.enums.Role;
import model.enums.SubjectType;
import model.grade.Grade;
import model.student.Student;
import model.subject.Subject;
import utils.InputSanitizer;

import java.util.List;
import java.util.Scanner;

/** Menu option 3: Record Grade. */
public class RecordGradeAction implements MenuAction {

    private final Scanner scanner;
    private final StudentManager studentManager;
    private final GradeManager gradeManager;

    public RecordGradeAction(Scanner scanner, StudentManager studentManager, GradeManager gradeManager) {
        this.scanner = scanner;
        this.studentManager = studentManager;
        this.gradeManager = gradeManager;
    }

    @Override
    public int getOptionNumber() {
        return 3;
    }

    @Override
    public String getLabel() {
        return "Record Grade";
    }

    @Override
    public boolean isAuthorizedFor(Role role) {
        return role == Role.TEACHER;
    }

    @Override
    public void execute() {
        System.out.println("\nRECORD GRADE");
        System.out.println(ConsoleUtils.DIVIDER);

        System.out.print("Enter Student ID: ");
        String studentId = InputSanitizer.sanitize(scanner.nextLine());

        Student student = studentManager.findStudent(studentId);
        if (student == null) {
            throw new StudentNotFoundException("Student with ID '" + studentId + "' not found in the system.",
                    studentId, ConsoleUtils.getAvailableStudentIds(studentManager));
        }

        System.out.println("\nStudent Details:");
        System.out.println("  Name: " + student.getName());
        System.out.println("  Type: " + student.getStudentType() + " Student");
        System.out.printf("  Current Average: %.1f%%%n", student.calculateAverageGrade());

        System.out.println("\nSubject type:");
        System.out.println("1. Core Subject (Mathematics, English, Science)");
        System.out.println("2. Elective Subject (Music, Art, Physical Education)");
        System.out.print("Select type (1-2): ");
        String typeChoice = scanner.nextLine().trim();

        SubjectType subjectType = typeChoice.equals("2") ? SubjectType.ELECTIVE : SubjectType.CORE;

        List<Subject> subjects = gradeManager.getSubjectsByType(subjectType);
        if (subjects.isEmpty()) {
            System.out.println("No subjects available for this type.");
            return;
        }

        System.out.println("\nAvailable " + subjectType + " Subjects:");
        for (int i = 0; i < subjects.size(); i++) {
            System.out.println((i + 1) + ". " + subjects.get(i).getSubjectName());
        }
        System.out.print("Select subject (1-" + subjects.size() + "): ");
        int subjectChoice;
        try {
            subjectChoice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
            return;
        }
        if (subjectChoice < 1 || subjectChoice > subjects.size()) {
            System.out.println("Invalid selection.");
            return;
        }
        Subject subject = subjects.get(subjectChoice - 1);

        System.out.print("\nEnter grade (0-100): ");
        double gradeValue;
        try {
            gradeValue = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid grade.");
            return;
        }

        Grade grade = new Grade(studentId, subject, gradeValue);

        System.out.println("\nGRADE CONFIRMATION");
        System.out.println(ConsoleUtils.DIVIDER);
        System.out.println("Grade ID: " + grade.getGradeId());
        System.out.println("Student: " + studentId + " - " + student.getName());
        System.out.println("Subject: " + subject.getSubjectName() + " (" + subject.getSubjectType() + ")");
        System.out.printf("Grade: %.1f%%%n", gradeValue);
        System.out.println("Date: " + grade.getDate());
        System.out.println(ConsoleUtils.DIVIDER);

        System.out.print("Confirm grade? (Y/N): ");
        if (!scanner.nextLine().equalsIgnoreCase("Y")) {
            System.out.println("Grade recording cancelled.");
            return;
        }

        gradeManager.addGrade(grade);
        System.out.println("\n✓ Grade recorded successfully!");

        ConsoleUtils.promptEnter(scanner);
    }
}
