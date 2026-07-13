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
import service.serviceimpl.GradeServiceImpl;
import service.serviceimpl.StudentServiceImpl;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    private static final StudentRepository studentRepository = new StudentRepositoryImpl();
    private static final SubjectRepository subjectRepository = new SubjectRepositoryImpl();
    private static final GradeService gradeService = new GradeServiceImpl(studentRepository, subjectRepository);
    private static final GradeManager gradeManager = new GradeManager(gradeService, subjectRepository);
    private static final StudentService studentService = new StudentServiceImpl(studentRepository);
    private static final StudentManager studentManager = new StudentManager(studentService, gradeManager);

    private static boolean useRoleBased = false;
    private static boolean isTeacher = true;

    public static void main(String[] args) {
        askRoleBased();

        while (true) {
            printMenu();

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            if (useRoleBased && !isAuthorized(choice)) {
                System.out.println("Access denied. This action is not available for your role.");
                continue;
            }

            try {
                switch (choice) {
                    case 1 -> addStudent();
                    case 2 -> viewAllStudents();
                    case 3 -> recordGrade();
                    case 4 -> viewGradeReport();
                    case 5 -> {
                        System.out.println("Thank you for using Student Grade Management System!");
                        System.out.println("Goodbye!");
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

    private static void askRoleBased() {
        System.out.print("Enable role-based access control? (Y/N, default N): ");
        String input = scanner.nextLine().trim();
        if (!input.equalsIgnoreCase("Y")) {
            return;
        }
        useRoleBased = true;

        while (true) {
            System.out.println("\nSelect your role:");
            System.out.println("1. Teacher");
            System.out.println("2. Student");
            System.out.print("Choose (1-2): ");
            String roleInput = scanner.nextLine().trim();
            if (roleInput.equals("1")) {
                isTeacher = true;
                return;
            } else if (roleInput.equals("2")) {
                isTeacher = false;
                return;
            }
            System.out.println("Invalid choice. Please select 1 or 2.");
        }
    }

    private static void printMenu() {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║   STUDENT GRADE MANAGEMENT - MAIN MENU     ║");
        System.out.println("╚════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("1. Add Student");
        System.out.println("2. View Students");
        System.out.println("3. Record Grade");
        System.out.println("4. View Grade Report");
        System.out.println("5. Exit");
        if (useRoleBased) {
            System.out.println("Role: " + (isTeacher ? "Teacher" : "Student"));
        }
        System.out.println();
        System.out.print("Enter choice: ");
    }

    private static boolean isAuthorized(int choice) {
        if (isTeacher) return true;
        return switch (choice) {
            case 2, 4, 5 -> true;
            default -> false;
        };
    }

    private static void addStudent() {
        if (useRoleBased && !isTeacher) {
            System.out.println("Access denied. This action is not available for your role.");
            return;
        }

        System.out.println("\nADD STUDENT");
        System.out.println("─────────────────────────────────────────────");
        System.out.println();

        System.out.print("Enter student name: ");
        String name = scanner.nextLine();

        System.out.print("Enter student age: ");
        int age;
        try {
            age = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid age. Please enter a number.");
            return;
        }

        System.out.print("Enter student email: ");
        String email = scanner.nextLine();

        System.out.print("Enter student phone: ");
        String phone = scanner.nextLine();

        System.out.println();
        System.out.println("Student type:");
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
            System.out.println("Invalid selection. Please choose 1 or 2.");
            return;
        }

        studentManager.addStudent(student);
        System.out.println("\n\u2713 Student added successfully!");
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

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void viewAllStudents() {
        System.out.println("\nSTUDENT LISTING");
        System.out.println("──────────────────────────────────────────────────────────────────────────");
        System.out.printf("%-8s | %-18s | %-12s | %-9s | %s%n", "STU ID", "NAME", "TYPE", "AVG GRADE", "STATUS");
        System.out.println("──────────────────────────────────────────────────────────────────────────");

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
            System.out.println("──────────────────────────────────────────────────────────────────────────");
            classTotal += student.calculateAverageGrade();
        }

        System.out.printf("%nTotal Students: %d%n", students.size());
        if (!students.isEmpty()) {
            System.out.printf("Average Class Grade: %.1f%%%n", classTotal / students.size());
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void recordGrade() {
        if (useRoleBased && !isTeacher) {
            System.out.println("Access denied. This action is not available for your role.");
            return;
        }

        System.out.println("\nRECORD GRADE");
        System.out.println("─────────────────────────────────────────────");
        System.out.println();

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
        System.out.printf("Current Average: %.1f%%%n", student.calculateAverageGrade());

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
            System.out.println("Invalid grade. Please enter a number.");
            return;
        }

        Grade grade = new Grade(studentId, subject, gradeValue);

        System.out.println("\nGRADE CONFIRMATION");
        System.out.println("─────────────────────────────────────────────");
        System.out.println("Grade ID: " + grade.getGradeId());
        System.out.println("Student: " + studentId + " - " + student.getName());
        System.out.println("Subject: " + subject.getSubjectName() + " (" + subject.getSubjectType() + ")");
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
        System.out.println("\n\u2713 Grade recorded successfully!");

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void viewGradeReport() {
        System.out.println("\nVIEW GRADE REPORT");
        System.out.println("─────────────────────────────────────────────");
        System.out.println();

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

        gradeManager.viewGradesByStudent(studentId);

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}
