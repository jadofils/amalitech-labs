package console;

import manager.StudentManager;
import model.enums.Role;
import model.student.HonorsStudent;
import model.student.RegularStudent;
import model.student.Student;
import utils.InputSanitizer;

import java.util.Scanner;

/** Menu option 1: Add Student. */
public class AddStudentAction implements MenuAction {

    private final Scanner scanner;
    private final StudentManager studentManager;

    public AddStudentAction(Scanner scanner, StudentManager studentManager) {
        this.scanner = scanner;
        this.studentManager = studentManager;
    }

    @Override
    public int getOptionNumber() {
        return 1;
    }

    @Override
    public String getLabel() {
        return "Add Student";
    }

    @Override
    public boolean isAuthorizedFor(Role role) {
        return role == Role.TEACHER;
    }

    @Override
    public void execute() {
        System.out.println("\nADD STUDENT");
        System.out.println("─────────────────────────");

        System.out.print("Enter student name: ");
        String name = InputSanitizer.sanitize(scanner.nextLine());

        System.out.print("Enter student age: ");
        int age;
        try {
            age = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid age.");
            return;
        }

        System.out.print("Enter student email: ");
        String email = InputSanitizer.sanitize(scanner.nextLine());

        System.out.print("Enter student phone: ");
        String phone = InputSanitizer.sanitize(scanner.nextLine());

        System.out.println("\nStudent type:");
        System.out.println("1. Regular Student (Passing grade: 50%)");
        System.out.println("2. Honors Student (Passing grade: 60%, honors recognition)");
        System.out.print("Select type (1-2): ");
        String typeChoice = scanner.nextLine().trim();

        Student student;
        boolean isHonors;
        if (typeChoice.equals("2")) {
            student = new HonorsStudent(name, age, email, phone);
            isHonors = true;
        } else if (typeChoice.equals("1")) {
            student = new RegularStudent(name, age, email, phone);
            isHonors = false;
        } else {
            System.out.println("Invalid selection.");
            return;
        }

        studentManager.addStudent(student);
        System.out.println("\n✓ Student added successfully!");
        System.out.println("  Student ID: " + student.getStudentId());
        System.out.println("  Name: " + student.getName());
        System.out.println("  Type: " + student.getStudentType());
        System.out.println("  Age: " + student.getAge());
        System.out.println("  Email: " + student.getEmail());
        System.out.printf("  Passing Grade: %.0f%%%n", student.getPassingGrade());
        if (isHonors) {
            System.out.println("  Honors Eligible: Yes");
        }
        System.out.println("  Status: Active");

        ConsoleUtils.promptEnter(scanner);
    }
}
