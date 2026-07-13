import exceptions.ExportException;
import exceptions.InvalidGradeException;
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
                    case 5 -> exportGradeReport();
                    case 6 -> calculateGPA();
                    case 7 -> bulkImportGrades();
                    case 8 -> viewClassStatistics();
                    case 9 -> searchStudents();
                    case 10 -> {
                        System.out.println("Thank you for using Student Grade Management System!");
                        System.out.println("Goodbye!");
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (InvalidGradeException e) {
                System.out.println("\n\u2717 ERROR: InvalidGradeException");
                System.out.println("  " + e.getMessage());
                System.out.println("  Try again? (Y/N): ");
                if (scanner.nextLine().trim().equalsIgnoreCase("Y")) {
                    recordGrade();
                }
            } catch (StudentNotFoundException e) {
                System.out.println("\n\u2717 ERROR: StudentNotFoundException");
                System.out.println("  " + e.getMessage());
                if (e.getAvailableIds() != null && !e.getAvailableIds().isEmpty()) {
                    System.out.println("  Available student IDs: " + String.join(", ", e.getAvailableIds()));
                }
            } catch (ExportException e) {
                System.out.println("\n\u2717 ERROR: ExportException");
                System.out.println("  " + e.getMessage());
                if (e.getFilePath() != null) {
                    System.out.println("  File: " + e.getFilePath());
                }
            } catch (StudentValidationException | GradeException
                     | SubjectNotFoundException | SubjectValidationException e) {
                System.out.println("\n\u2717 ERROR: " + e.getClass().getSimpleName());
                System.out.println("  " + e.getMessage());
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
        System.out.println("\n\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557");
        System.out.println("\u2551   STUDENT GRADE MANAGEMENT - MAIN MENU    \u2551");
        System.out.println("\u255a\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255d");
        System.out.println();
        System.out.println("1. Add Student");
        System.out.println("2. View Students");
        System.out.println("3. Record Grade");
        System.out.println("4. View Grade Report");
        System.out.println("5. Export Grade Report");
        System.out.println("6. Calculate Student GPA");
        System.out.println("7. Bulk Import Grades");
        System.out.println("8. View Class Statistics");
        System.out.println("9. Search Students");
        System.out.println("10. Exit");
        if (useRoleBased) {
            System.out.println("Role: " + (isTeacher ? "Teacher" : "Student"));
        }
        System.out.println();
        System.out.print("Enter choice: ");
    }

    private static boolean isAuthorized(int choice) {
        if (isTeacher) return true;
        return choice >= 4 && choice <= 10;
    }

    private static void addStudent() {
        if (useRoleBased && !isTeacher) {
            System.out.println("Access denied.");
            return;
        }

        System.out.println("\nADD STUDENT");
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");

        System.out.print("Enter student name: ");
        String name = scanner.nextLine();

        System.out.print("Enter student age: ");
        int age;
        try {
            age = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid age.");
            return;
        }

        System.out.print("Enter student email: ");
        String email = scanner.nextLine();

        System.out.print("Enter student phone: ");
        String phone = scanner.nextLine();

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

        promptEnter();
    }

    private static void viewAllStudents() {
        System.out.println("\nSTUDENT LISTING");
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        System.out.printf("%-8s | %-18s | %-12s | %-9s | %s%n", "STU ID", "NAME", "TYPE", "AVG GRADE", "STATUS");
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");

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
            System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
            classTotal += student.calculateAverageGrade();
        }

        System.out.printf("%nTotal Students: %d%n", students.size());
        if (!students.isEmpty()) {
            System.out.printf("Average Class Grade: %.1f%%%n", classTotal / students.size());
        }

        promptEnter();
    }

    private static void recordGrade() {
        if (useRoleBased && !isTeacher) {
            System.out.println("Access denied.");
            return;
        }

        System.out.println("\nRECORD GRADE");
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");

        System.out.print("Enter Student ID: ");
        String studentId = scanner.nextLine();

        Student student = studentManager.findStudent(studentId);
        if (student == null) {
            System.out.println("\n\u2717 ERROR: StudentNotFoundException");
            System.out.println("  Student with ID '" + studentId + "' not found in the system.");
            System.out.println("  Available student IDs: " + String.join(", ", getAvailableStudentIds()));
            return;
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
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        System.out.println("Grade ID: " + grade.getGradeId());
        System.out.println("Student: " + studentId + " - " + student.getName());
        System.out.println("Subject: " + subject.getSubjectName() + " (" + subject.getSubjectType() + ")");
        System.out.printf("Grade: %.1f%%%n", gradeValue);
        System.out.println("Date: " + grade.getDate());
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");

        System.out.print("Confirm grade? (Y/N): ");
        if (!scanner.nextLine().equalsIgnoreCase("Y")) {
            System.out.println("Grade recording cancelled.");
            return;
        }

        gradeManager.addGrade(grade);
        System.out.println("\n\u2713 Grade recorded successfully!");

        promptEnter();
    }

    private static void viewGradeReport() {
        System.out.println("\nVIEW GRADE REPORT");
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");

        System.out.print("Enter Student ID: ");
        String studentId = scanner.nextLine();

        Student student = studentManager.findStudent(studentId);
        if (student == null) {
            System.out.println("\n\u2717 ERROR: StudentNotFoundException");
            System.out.println("  Student with ID '" + studentId + "' not found.");
            return;
        }

        System.out.println("\nStudent: " + studentId + " - " + student.getName());
        System.out.println("Type: " + student.getStudentType() + " Student");
        System.out.printf("Passing Grade: %.0f%%%n", student.getPassingGrade());

        gradeManager.viewGradesByStudent(studentId);

        promptEnter();
    }

    private static void exportGradeReport() {
        System.out.println("\nEXPORT GRADE REPORT");
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");

        System.out.print("Enter Student ID: ");
        String studentId = scanner.nextLine();

        Student student = studentManager.findStudent(studentId);
        if (student == null) {
            System.out.println("Student not found.");
            return;
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

        try {
            java.io.File reportsDir = new java.io.File("reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdirs();
            }

            boolean summary = option.equals("1") || option.equals("3");
            boolean detailed = option.equals("2") || option.equals("3");

            int filesCreated = 0;
            long totalSize = 0;

            if (summary) {
                String content = generateSummaryReport(student, studentId);
                String path = "reports/" + filename + "_summary.txt";
                java.io.File file = new java.io.File(path);
                java.io.FileWriter writer = new java.io.FileWriter(file);
                writer.write(content);
                writer.close();
                filesCreated++;
                totalSize += file.length();
            }

            if (detailed) {
                String content = generateDetailedReport(student, studentId);
                String path = "reports/" + filename + "_detailed.txt";
                java.io.File file = new java.io.File(path);
                java.io.FileWriter writer = new java.io.FileWriter(file);
                writer.write(content);
                writer.close();
                filesCreated++;
                totalSize += file.length();
            }

            System.out.println("\n\u2713 Report exported successfully!");
            for (int i = 0; i < filesCreated; i++) {
                String suffix = summary && detailed ? (i == 0 ? "_summary" : "_detailed") : "";
                System.out.println("  File: " + filename + suffix + ".txt");
            }
            System.out.println("  Location: ./reports/");
            System.out.println("  Size: " + (totalSize / 1024.0) + " KB");
            System.out.println("  Contains: " + gradeManager.getGradesForStudent(studentId).size() + " grades");
        } catch (java.io.IOException e) {
            throw new ExportException("Failed to export report: " + e.getMessage(), "reports/" + filename, e);
        }

        promptEnter();
    }

    private static String generateSummaryReport(Student student, String studentId) {
        StringBuilder sb = new StringBuilder();
        sb.append("STUDENT GRADE REPORT - SUMMARY\n");
        sb.append("================================\n\n");
        sb.append("Student ID: ").append(studentId).append("\n");
        sb.append("Name: ").append(student.getName()).append("\n");
        sb.append("Type: ").append(student.getStudentType()).append(" Student\n");
        sb.append("Passing Grade: ").append((int) student.getPassingGrade()).append("%\n");
        sb.append("Status: ").append(student.isPassing() ? "Passing" : "Failing").append("\n");
        sb.append("Overall Average: ").append(String.format("%.1f%%", student.calculateAverageGrade())).append("\n\n");
        sb.append("================================\n");
        sb.append("Generated on: ").append(new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm").format(new java.util.Date())).append("\n");
        return sb.toString();
    }

    private static String generateDetailedReport(Student student, String studentId) {
        StringBuilder sb = new StringBuilder();
        sb.append("STUDENT GRADE REPORT - DETAILED\n");
        sb.append("================================\n\n");
        sb.append("Student ID: ").append(studentId).append("\n");
        sb.append("Name: ").append(student.getName()).append("\n");
        sb.append("Type: ").append(student.getStudentType()).append(" Student\n\n");

        sb.append("GRADE HISTORY\n");
        sb.append("------------------------------------------------\n");
        sb.append(String.format("%-8s | %-10s | %-16s | %-9s | %s%n", "GRD ID", "DATE", "SUBJECT", "TYPE", "GRADE"));
        sb.append("------------------------------------------------\n");

        List<Grade> grades = gradeManager.getGradesForStudent(studentId);
        for (Grade g : grades) {
            sb.append(String.format("%-8s | %-10s | %-16s | %-9s | %.1f%%%n",
                    g.getGradeId(), g.getDate(), g.getSubject().getSubjectName(),
                    g.getSubjectType(), g.getGrade()));
        }

        sb.append("------------------------------------------------\n");
        sb.append("Total Grades: ").append(grades.size()).append("\n");
        sb.append("Core Average: ").append(String.format("%.1f%%", gradeManager.calculateCoreAverage(studentId))).append("\n");
        sb.append("Elective Average: ").append(String.format("%.1f%%", gradeManager.calculateElectiveAverage(studentId))).append("\n");
        sb.append("Overall Average: ").append(String.format("%.1f%%", gradeManager.calculateOverallAverage(studentId))).append("\n\n");
        sb.append("================================\n");
        sb.append("Generated on: ").append(new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm").format(new java.util.Date())).append("\n");
        return sb.toString();
    }

    private static void calculateGPA() {
        System.out.println("\nCALCULATE STUDENT GPA");
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");

        System.out.print("Enter Student ID: ");
        String studentId = scanner.nextLine();

        Student student = studentManager.findStudent(studentId);
        if (student == null) {
            System.out.println("Student not found.");
            return;
        }

        System.out.println("\nStudent: " + studentId + " - " + student.getName());
        System.out.println("Type: " + student.getStudentType() + " Student");
        System.out.printf("Overall Average: %.1f%%%n", student.calculateAverageGrade());

        System.out.println("\nGPA CALCULATION (4.0 Scale)");
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        System.out.printf("%-16s | %-7s | %s%n", "Subject", "Grade", "GPA Points");
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");

        List<Grade> grades = gradeManager.getGradesForStudent(studentId);
        double totalGPA = 0;
        int count = 0;

        for (Grade g : grades) {
            double gpa = percentageToGPA(g.getGrade());
            totalGPA += gpa;
            count++;
            System.out.printf("%-16s | %.0f%%    | %.1f (%s)%n",
                    g.getSubject().getSubjectName(), g.getGrade(), gpa, gpaToLetter(gpa));
        }

        double cumulativeGPA = count > 0 ? totalGPA / count : 0;
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        System.out.printf("Cumulative GPA: %.2f / 4.0%n", cumulativeGPA);
        System.out.println("Letter Grade: " + gpaToLetter(cumulativeGPA));

        int rank = calculateClassRank(studentId, student);
        List<Student> allStudents = studentManager.getAllStudents();
        System.out.println("Class Rank: " + rank + " of " + allStudents.size());

        System.out.println("\nPerformance Analysis:");
        if (cumulativeGPA >= 3.5) {
            System.out.println("\u2713 Excellent performance (3.5+ GPA)");
        } else if (cumulativeGPA >= 3.0) {
            System.out.println("\u2713 Good performance (3.0+ GPA)");
        } else if (cumulativeGPA >= 2.0) {
            System.out.println("\u2713 Satisfactory performance (2.0+ GPA)");
        } else {
            System.out.println("\u2717 Needs improvement (below 2.0 GPA)");
        }
        if (student instanceof HonorsStudent hs && hs.checkHonorsEligibility()) {
            System.out.println("\u2713 Honors eligibility maintained");
        }
        double classAvg = allStudents.isEmpty() ? 0 : allStudents.stream().mapToDouble(Student::calculateAverageGrade).average().orElse(0);
        double classAvgGPA = percentageToGPA(classAvg);
        if (cumulativeGPA > classAvgGPA) {
            System.out.printf("\u2713 Above class average (%.2f GPA)%n", classAvgGPA);
        } else {
            System.out.printf("\u2717 Below class average (%.2f GPA)%n", classAvgGPA);
        }

        promptEnter();
    }

    private static double percentageToGPA(double percentage) {
        if (percentage >= 93) return 4.0;
        if (percentage >= 90) return 3.7;
        if (percentage >= 87) return 3.3;
        if (percentage >= 83) return 3.0;
        if (percentage >= 80) return 2.7;
        if (percentage >= 77) return 2.3;
        if (percentage >= 73) return 2.0;
        if (percentage >= 70) return 1.7;
        if (percentage >= 67) return 1.3;
        if (percentage >= 60) return 1.0;
        return 0.0;
    }

    private static String gpaToLetter(double gpa) {
        if (gpa >= 3.7) return "A";
        if (gpa >= 3.3) return "A-";
        if (gpa >= 3.0) return "B+";
        if (gpa >= 2.7) return "B";
        if (gpa >= 2.3) return "B-";
        if (gpa >= 2.0) return "C+";
        if (gpa >= 1.7) return "C";
        if (gpa >= 1.3) return "C-";
        if (gpa >= 1.0) return "D+";
        return "F";
    }

    private static int calculateClassRank(String studentId, Student target) {
        List<Student> students = studentManager.getAllStudents();
        double targetAvg = target.calculateAverageGrade();
        int rank = 1;
        for (Student s : students) {
            if (!s.getStudentId().equals(studentId) && s.calculateAverageGrade() > targetAvg) {
                rank++;
            }
        }
        return rank;
    }

    private static void bulkImportGrades() {
        System.out.println("\nBULK IMPORT GRADES");
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");

        System.out.println("\nPlace your CSV file in: ./imports/");
        System.out.println("\nCSV Format Required:");
        System.out.println("StudentID,SubjectName,SubjectType,Grade");
        System.out.println("Example: STU001,Mathematics,Core,85");

        System.out.print("\nEnter filename (without extension): ");
        String filename = scanner.nextLine().trim();

        if (filename.isEmpty()) {
            System.out.println("Filename cannot be empty.");
            return;
        }

        String path = "imports/" + filename + ".csv";
        java.io.File file = new java.io.File(path);

        if (!file.exists()) {
            System.out.println("\n\u2717 ERROR: File not found: " + path);
            return;
        }

        System.out.println("Validating file... \u2713");
        System.out.println("Processing grades...");

        int total = 0;
        int success = 0;
        int failed = 0;
        StringBuilder failedLog = new StringBuilder();

        try {
            java.util.Scanner fileScanner = new java.util.Scanner(file);
            int lineNum = 0;

            if (fileScanner.hasNextLine()) {
                fileScanner.nextLine();
                lineNum++;
            }

            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                lineNum++;
                total++;

                if (line.isEmpty()) {
                    success++;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length != 4) {
                    failed++;
                    failedLog.append("  Row ").append(lineNum).append(": Invalid format\n");
                    continue;
                }

                String sid = parts[0].trim();
                String subjName = parts[1].trim();
                String subjType = parts[2].trim();
                double gVal;

                try {
                    gVal = Double.parseDouble(parts[3].trim());
                } catch (NumberFormatException e) {
                    failed++;
                    failedLog.append("  Row ").append(lineNum).append(": Invalid grade number (").append(parts[3].trim()).append(")\n");
                    continue;
                }

                Student s = studentManager.findStudent(sid);
                if (s == null) {
                    failed++;
                    failedLog.append("  Row ").append(lineNum).append(": Invalid student ID (").append(sid).append(")\n");
                    continue;
                }

                if (gVal < 0 || gVal > 100) {
                    failed++;
                    failedLog.append("  Row ").append(lineNum).append(": Grade out of range (").append((int) gVal).append(")\n");
                    continue;
                }

                Subject matchedSubject = null;
                for (Subject subj : subjectRepository.getAllSubjects()) {
                    if (subj.getSubjectName().equalsIgnoreCase(subjName)) {
                        matchedSubject = subj;
                        break;
                    }
                }

                if (matchedSubject == null) {
                    failed++;
                    failedLog.append("  Row ").append(lineNum).append(": Unknown subject (").append(subjName).append(")\n");
                    continue;
                }

                Grade grade = new Grade(sid, matchedSubject, gVal);
                gradeManager.addGrade(grade);
                success++;
            }

            fileScanner.close();
        } catch (java.io.IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return;
        }

        System.out.println("\nIMPORT SUMMARY");
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        System.out.println("Total Rows: " + total);
        System.out.println("Successfully Imported: " + success);
        System.out.println("Failed: " + failed);

        if (failed > 0) {
            System.out.println("\nFailed Records:");
            System.out.print(failedLog.toString());
        }

        System.out.println("\n\u2713 Import completed!");
        System.out.println("  " + success + " grades added to system");

        promptEnter();
    }

    private static void viewClassStatistics() {
        System.out.println("\nCLASS STATISTICS");
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");

        List<Student> students = studentManager.getAllStudents();
        List<Grade> allGrades = new java.util.ArrayList<>();
        for (Student s : students) {
            allGrades.addAll(gradeManager.getGradesForStudent(s.getStudentId()));
        }

        System.out.println("\nTotal Students: " + students.size());
        System.out.println("Total Grades Recorded: " + allGrades.size());

        int[] distribution = new int[5];
        double sum = 0;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        String maxStudent = "", maxSubject = "", minStudent = "", minSubject = "";

        for (Grade g : allGrades) {
            double val = g.getGrade();
            sum += val;
            if (val >= 90) distribution[0]++;
            else if (val >= 80) distribution[1]++;
            else if (val >= 70) distribution[2]++;
            else if (val >= 60) distribution[3]++;
            else distribution[4]++;

            if (val > max) {
                max = val;
                maxStudent = findStudentName(g.getStudentId());
                maxSubject = g.getSubject().getSubjectName();
            }
            if (val < min) {
                min = val;
                minStudent = findStudentName(g.getStudentId());
                minSubject = g.getSubject().getSubjectName();
            }
        }

        if (allGrades.isEmpty()) {
            System.out.println("No grades recorded yet.");
            promptEnter();
            return;
        }

        double mean = sum / allGrades.size();

        java.util.List<Double> sortedVals = new java.util.ArrayList<>();
        for (Grade g : allGrades) sortedVals.add(g.getGrade());
        java.util.Collections.sort(sortedVals);

        double median;
        int n = sortedVals.size();
        if (n % 2 == 0) {
            median = (sortedVals.get(n / 2 - 1) + sortedVals.get(n / 2)) / 2;
        } else {
            median = sortedVals.get(n / 2);
        }

        double varianceSum = 0;
        for (double v : sortedVals) varianceSum += Math.pow(v - mean, 2);
        double stdDev = Math.sqrt(varianceSum / n);
        double range = max - min;

        System.out.println("\nGRADE DISTRIBUTION");
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        String[] labels = {"90-100% (A)", "80-89%  (B)", "70-79%  (C)", "60-69%  (D)", "0-59%   (F)"};
        for (int i = 0; i < 5; i++) {
            double pct = (distribution[i] * 100.0 / allGrades.size());
            int barLen = (int) (pct / 2);
            String bar = "\u2588".repeat(Math.max(0, barLen)) + "\u2591".repeat(Math.max(0, 50 - barLen));
            System.out.printf("%s: %s %.1f%% (%d grades)%n", labels[i], bar, pct, distribution[i]);
        }

        System.out.println("\nSTATISTICAL ANALYSIS");
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        System.out.printf("Mean (Average):      %.1f%%%n", mean);
        System.out.printf("Median:              %.1f%%%n", median);
        System.out.printf("Standard Deviation:  %.1f%%%n", stdDev);
        System.out.printf("Range:               %.1f%% (%.0f%% - %.0f%%)%n", range, min, max);

        System.out.printf("%nHighest Grade:  %.0f%% (%s - %s)%n", max, maxStudent, maxSubject);
        System.out.printf("Lowest Grade:    %.0f%% (%s - %s)%n", min, minStudent, minSubject);

        System.out.println("\nSUBJECT PERFORMANCE");
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        List<Subject> allSubjects = subjectRepository.getAllSubjects();
        for (Subject subj : allSubjects) {
            double sSum = 0;
            int sCount = 0;
            for (Grade g : allGrades) {
                if (g.getSubject().getSubjectCode().equals(subj.getSubjectCode())) {
                    sSum += g.getGrade();
                    sCount++;
                }
            }
            if (sCount > 0) {
                System.out.printf("  %s: %.1f%%%n", subj.getSubjectName(), sSum / sCount);
            }
        }

        System.out.println("\nSTUDENT TYPE COMPARISON");
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        double regSum = 0, honSum = 0;
        int regCount = 0, honCount = 0;
        for (Student s : students) {
            if (s instanceof HonorsStudent) {
                honSum += s.calculateAverageGrade();
                honCount++;
            } else {
                regSum += s.calculateAverageGrade();
                regCount++;
            }
        }
        if (regCount > 0) System.out.printf("  Regular Students:  %.1f%% average (%d students)%n", regSum / regCount, regCount);
        if (honCount > 0) System.out.printf("  Honors Students:   %.1f%% average (%d students)%n", honSum / honCount, honCount);

        promptEnter();
    }

    private static String findStudentName(String studentId) {
        Student s = studentManager.findStudent(studentId);
        return s != null ? s.getName() : "Unknown";
    }

    private static void searchStudents() {
        System.out.println("\nSEARCH STUDENTS");
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");

        while (true) {
            System.out.println("\nSearch options:");
            System.out.println("1. By Student ID");
            System.out.println("2. By Name (partial match)");
            System.out.println("3. By Grade Range");
            System.out.println("4. By Student Type");
            System.out.print("Select option (1-4): ");
            String option = scanner.nextLine().trim();

            List<Student> results = new java.util.ArrayList<>();
            String searchDesc = "";

            switch (option) {
                case "1" -> {
                    System.out.print("Enter Student ID: ");
                    String id = scanner.nextLine().trim();
                    searchDesc = "ID: " + id;
                    Student s = studentManager.findStudent(id);
                    if (s != null) results.add(s);
                }
                case "2" -> {
                    System.out.print("Enter name (partial or full): ");
                    String name = scanner.nextLine().trim().toLowerCase();
                    searchDesc = "Name: \"" + name + "\"";
                    for (Student s : studentManager.getAllStudents()) {
                        if (s.getName().toLowerCase().contains(name)) {
                            results.add(s);
                        }
                    }
                }
                case "3" -> {
                    System.out.print("Enter minimum grade: ");
                    double min;
                    try { min = Double.parseDouble(scanner.nextLine()); } catch (NumberFormatException e) { System.out.println("Invalid input."); continue; }
                    System.out.print("Enter maximum grade: ");
                    double max;
                    try { max = Double.parseDouble(scanner.nextLine()); } catch (NumberFormatException e) { System.out.println("Invalid input."); continue; }
                    searchDesc = "Grade range: " + (int) min + "-" + (int) max + "%";
                    for (Student s : studentManager.getAllStudents()) {
                        double avg = s.calculateAverageGrade();
                        if (avg >= min && avg <= max) {
                            results.add(s);
                        }
                    }
                }
                case "4" -> {
                    System.out.println("Student type:");
                    System.out.println("1. Regular");
                    System.out.println("2. Honors");
                    System.out.print("Select (1-2): ");
                    String t = scanner.nextLine().trim();
                    searchDesc = "Type: " + (t.equals("2") ? "Honors" : "Regular");
                    for (Student s : studentManager.getAllStudents()) {
                        if (t.equals("2") && s instanceof HonorsStudent) results.add(s);
                        else if (t.equals("1") && !(s instanceof HonorsStudent)) results.add(s);
                    }
                }
                default -> {
                    System.out.println("Invalid option.");
                    continue;
                }
            }

            System.out.println("\nSEARCH RESULTS (" + results.size() + " found)");
            System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
            System.out.printf("%-8s | %-18s | %-9s | %s%n", "STU ID", "NAME", "TYPE", "AVG");
            System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
            for (Student s : results) {
                System.out.printf("%-8s | %-18s | %-9s | %.1f%%%n",
                        s.getStudentId(), s.getName(), s.getStudentType(), s.calculateAverageGrade());
            }

            System.out.println("\nActions:");
            System.out.println("1. View full details for a student");
            System.out.println("2. Export search results");
            System.out.println("3. New search");
            System.out.println("4. Return to main menu");
            System.out.print("Enter choice: ");
            String action = scanner.nextLine().trim();

            if (action.equals("1")) {
                System.out.print("Enter Student ID to view: ");
                String viewId = scanner.nextLine().trim();
                Student viewS = studentManager.findStudent(viewId);
                if (viewS != null) {
                    viewS.displayStudentDetails();
                } else {
                    System.out.println("Student not found.");
                }
            } else if (action.equals("2")) {
                System.out.print("Enter filename: ");
                String expName = scanner.nextLine().trim();
                if (!expName.isEmpty()) {
                    try {
                        java.io.File expFile = new java.io.File("reports/search_" + expName + ".txt");
                        expFile.getParentFile().mkdirs();
                        java.io.FileWriter fw = new java.io.FileWriter(expFile);
                        fw.write("Search Results: " + searchDesc + "\n");
                        fw.write("Found: " + results.size() + " students\n\n");
                        for (Student s : results) {
                            fw.write(s.getStudentId() + " | " + s.getName() + " | " + s.getStudentType() + " | " + String.format("%.1f%%", s.calculateAverageGrade()) + "\n");
                        }
                        fw.close();
                        System.out.println("Results exported to reports/search_" + expName + ".txt");
                    } catch (java.io.IOException e) {
                        System.out.println("Export failed: " + e.getMessage());
                    }
                }
            } else if (action.equals("3")) {
                continue;
            } else {
                break;
            }

            if (!action.equals("3")) break;
        }

        promptEnter();
    }

    private static List<String> getAvailableStudentIds() {
        List<String> ids = new java.util.ArrayList<>();
        for (Student s : studentManager.getAllStudents()) {
            ids.add(s.getStudentId());
        }
        return ids;
    }

    private static void promptEnter() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}
