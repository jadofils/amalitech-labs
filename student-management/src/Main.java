import config.DatabaseConfig;
import exceptions.StudentNotFoundException;
import exceptions.ValidationException;
import model.*;
import repository.StudentRepository;
import repository.impl.StudentRepositoryImpl;
import service.StudentService;
import serviceimpl.StudentServiceImpl;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final StudentRepository studentRepository = new StudentRepositoryImpl();
    private static final StudentService studentService = new StudentServiceImpl(studentRepository);

    public static void main(String[] args) {
        if (DatabaseConfig.testConnection()) {
            System.out.println("Database connected successfully.");
        } else {
            System.out.println("Cannot start application because database connection failed.");
            return;
        }

        while (true) {
            System.out.println("\n===== Student Management Menu =====");
            System.out.println("1. Add Student");
            System.out.println("2. View Student by ID");
            System.out.println("3. View All Students");
            System.out.println("4. Update Student");
            System.out.println("5. Delete Student");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and 6.");
                continue;
            }

            try {
                switch (choice) {
                    case 1 -> addStudent();
                    case 2 -> viewStudentById();
                    case 3 -> viewAllStudents();
                    case 4 -> updateStudent();
                    case 5 -> deleteStudent();
                    case 6 -> {
                        System.out.println("Exiting...");
                        return;
                    }
                    default -> System.out.println("Invalid choice. Please select 1-6.");
                }
            } catch (ValidationException | StudentNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    private static void addStudent() {
        System.out.print("Enter name: ");
        String name = scanner.nextLine();

        System.out.print("Enter age: ");
        int age;
        try {
            age = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid age. Please enter a number.");
            return;
        }

        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        System.out.print("Enter phone: ");
        String phone = scanner.nextLine();

        System.out.print("Enter type (REGULAR/HONORS): ");
        String type = scanner.nextLine();

        Student student = type.equalsIgnoreCase("HONORS")
                ? new HonorsStudent(name, age, email, phone)
                : new RegularStudent(name, age, email, phone);

        studentService.addStudent(student);
        System.out.println("Student added successfully.");
    }

    private static void viewStudentById() {
        System.out.print("Enter student ID: ");
        String id = scanner.nextLine();
        Student student = studentService.getStudentById(id);
        student.displayStudentDetails();
    }

    private static void viewAllStudents() {
        List<Student> students = studentService.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("No students found.");
        } else {
            students.forEach(Student::displayStudentDetails);
        }
    }

    private static void updateStudent() {
        System.out.print("Enter student ID to update: ");
        String id = scanner.nextLine();

        Student existing = studentService.getStudentById(id);

        System.out.print("Enter new name (" + existing.getName() + "): ");
        String name = scanner.nextLine();

        System.out.print("Enter new age (" + existing.getAge() + "): ");
        int age;
        try {
            age = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid age. Please enter a number.");
            return;
        }

        System.out.print("Enter new email (" + existing.getEmail() + "): ");
        String email = scanner.nextLine();

        System.out.print("Enter new phone (" + existing.getPhone() + "): ");
        String phone = scanner.nextLine();

        existing.setName(name);
        existing.setAge(age);
        existing.setEmail(email);
        existing.setPhone(phone);

        studentService.updateStudent(existing);
        System.out.println("Student updated successfully.");
    }

    private static void deleteStudent() {
        System.out.print("Enter student ID to delete: ");
        String id = scanner.nextLine();
        studentService.deleteStudent(id);
        System.out.println("Student deleted successfully.");
    }
}