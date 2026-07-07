import exceptions.StudentNotFoundException;
import exceptions.StudentValidationException;
import exceptions.grades.GradeException;
import exceptions.subjects.SubjectNotFoundException;
import exceptions.subjects.SubjectValidationException;
import manager.GradeManager;
import manager.StudentManager;
import model.enums.SubjectType;
import model.grade.Grade;
import model.student.HonorsStudent;
import model.student.RegularStudent;
import model.student.Student;
import model.subject.Subject;
import repository.student.StudentRepository;
import repository.student.StudentRepositoryImpl;
import repository.subject.SubjectRepository;
import repository.subject.impl.SubjectRepositoryImpl;
import service.GradeService;
import service.StudentService;
import serviceimpl.GradeServiceImpl;
import serviceimpl.StudentServiceImpl;

import java.util.List;
import java.util.Scanner;

import model.enums.Role;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    private static final StudentRepository studentRepository = new StudentRepositoryImpl();
    private static final SubjectRepository subjectRepository = new SubjectRepositoryImpl();
    private static final GradeService gradeService = new GradeServiceImpl(studentRepository, subjectRepository);
    private static final GradeManager gradeManager = new GradeManager(gradeService, subjectRepository);
    private static final StudentService studentService = new StudentServiceImpl(studentRepository);
    private static final StudentManager studentManager = new StudentManager(studentService, gradeManager);

    public static void main(String[] args) {
        Role role = selectRole();

        while (true) {
            printMenu(role);

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            if (!isAuthorized(role, choice)) {
                System.out.println("Access denied. This action is not available for your role.");
                continue;
            }

            try {
                switch (choice) {
                    case 1 -> addStudent();
                    case 2 -> viewStudentById();
                    case 3 -> viewAllStudents();
                    case 4 -> updateStudent();
                    case 5 -> deleteStudent();
                    case 6 -> recordGrade();
                    case 7 -> viewGradeReport();
                    case 8 -> {
                        System.out.println("Exiting...");
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (StudentValidationException | StudentNotFoundException | GradeException
                     | SubjectNotFoundException | SubjectValidationException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    private static Role selectRole() {
        while (true) {
            System.out.println("\n===== Select Your Role =====");
            System.out.println("1. Teacher");
            System.out.println("2. Student");
            System.out.print("Choose your role (1-2): ");
            String input = scanner.nextLine().trim();
            if (input.equals("1")) return Role.TEACHER;
            if (input.equals("2")) return Role.STUDENT;
            System.out.println("Invalid choice. Please select 1 for Teacher or 2 for Student.");
        }
    }

    private static void printMenu(Role role) {
        System.out.println("\n===== Student Grade Management Menu (" + role + ") =====");
        if (role == Role.TEACHER) {
            System.out.println("1. Add Student");
            System.out.println("2. View Student by ID");
            System.out.println("3. View All Students");
            System.out.println("4. Update Student");
            System.out.println("5. Delete Student");
            System.out.println("6. Record Grade");
            System.out.println("7. View Grade Report");
            System.out.println("8. Exit");
        } else {
            System.out.println("2. View Student by ID");
            System.out.println("3. View All Students");
            System.out.println("7. View Grade Report");
            System.out.println("8. Exit");
        }
        System.out.print("Choose an option: ");
    }

    private static boolean isAuthorized(Role role, int choice) {
        if (role == Role.TEACHER) {
            return choice >= 1 && choice <= 8;
        }
        // STUDENT: only view operations
        return switch (choice) {
            case 2, 3, 7, 8 -> true;
            default -> false;
        };
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
        String type = scanner.nextLine().trim();

        if (!type.equalsIgnoreCase("REGULAR") && !type.equalsIgnoreCase("HONORS")) {
            System.out.println("Invalid type. Please enter REGULAR or HONORS.");
            return;
        }

        Student student = type.equalsIgnoreCase("HONORS")
                ? new HonorsStudent(name, age, email, phone)
                : new RegularStudent(name, age, email, phone);

        studentManager.addStudent(student);
        System.out.println("\n✓ Student added successfully!");
        student.displayStudentDetails();
    }

    private static void viewStudentById() {
        System.out.print("Enter student ID: ");
        String id = scanner.nextLine();
        Student student = studentManager.findStudent(id);
        if (student == null) {
            System.out.println("Student with ID " + id + " not found.");
            return;
        }
        student.displayStudentDetails();
    }

    private static void viewAllStudents() {
        studentManager.viewAllStudents();
    }

    private static void updateStudent() {
        System.out.print("Enter student ID to update: ");
        String id = scanner.nextLine();

        Student existing = studentManager.findStudent(id);
        if (existing == null) {
            System.out.println("Student with ID " + id + " not found.");
            return;
        }

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

        studentManager.updateStudent(existing);
        System.out.println("Student updated successfully.");
    }

    private static void deleteStudent() {
        System.out.print("Enter student ID to delete: ");
        String id = scanner.nextLine();
        studentManager.deleteStudent(id);
        System.out.println("Student deleted successfully.");
    }

    private static void recordGrade() {
        System.out.print("Enter Student ID: ");
        String studentId = scanner.nextLine();

        Student student = studentManager.findStudent(studentId);
        if (student == null) {
            System.out.println("Student with ID " + studentId + " not found.");
            return;
        }

        System.out.println("\nStudent Details:");
        System.out.println("Name: " + student.getName());
        System.out.println("Type: " + student.getStudentType() + " Student");
        System.out.printf("Current Average: %.1f%%%n", gradeManager.calculateOverallAverage(studentId));

        System.out.println("\nSubject type:");
        System.out.println("1. Core Subject");
        System.out.println("2. Elective Subject");
        System.out.print("Select type (1-2): ");
        SubjectType subjectType = scanner.nextLine().trim().equals("2") ? SubjectType.ELECTIVE : SubjectType.CORE;

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

        System.out.print("Enter grade (0-100): ");
        double gradeValue;
        try {
            gradeValue = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid grade. Please enter a number.");
            return;
        }

        // Range (0-100) is enforced by Grade's constructor via the Gradable
        // contract; an invalid value throws GradeException, caught by the menu loop.
        Grade grade = new Grade(studentId, subject, gradeValue);

        System.out.println("\nGRADE CONFIRMATION");
        System.out.println("─────────────────────────────────────────────");
        System.out.println("Grade ID: " + grade.getGradeId());
        System.out.println("Student: " + studentId + " - " + student.getName());
        subject.displaySubjectDetails();
        System.out.printf("Grade: %.1f%%%n", gradeValue);
        System.out.println("Date: " + grade.getDate());
        System.out.println("─────────────────────────────────────────────");

        System.out.print("Confirm grade? (Y/N): ");
        String confirm = scanner.nextLine();
        if (!confirm.equalsIgnoreCase("Y")) {
            System.out.println("Grade recording cancelled.");
            return;
        }

        gradeManager.addGrade(grade);
        System.out.println("Grade recorded successfully!");
    }

    private static void viewGradeReport() {
        System.out.print("Enter Student ID: ");
        String studentId = scanner.nextLine();

        Student student = studentManager.findStudent(studentId);
        if (student == null) {
            System.out.println("Student with ID " + studentId + " not found.");
            return;
        }

        System.out.println("\nStudent: " + studentId + " - " + student.getName());
        System.out.println("Type: " + student.getStudentType() + " Student");
        System.out.printf("Passing Grade: %.0f%%%n", student.getPassingGrade());
        System.out.println();

        gradeManager.viewGradesByStudent(studentId);

        if (!gradeManager.getGradesForStudent(studentId).isEmpty()) {
            System.out.println("\nPerformance Summary:");
            System.out.println(student.isPassing()
                    ? "Meeting passing grade requirement (" + (int) student.getPassingGrade() + "%)"
                    : "Below passing grade requirement (" + (int) student.getPassingGrade() + "%)");
        }
    }
}