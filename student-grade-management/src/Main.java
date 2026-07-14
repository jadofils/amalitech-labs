import calculators.GPACalculator;
import calculators.StatisticsCalculator;
import exceptions.ExportException;
import exceptions.ImportException;
import exceptions.InvalidGradeException;
import exceptions.StudentNotFoundException;
import exceptions.StudentValidationException;
import exceptions.grades.GradeException;
import exceptions.subjects.SubjectNotFoundException;
import exceptions.subjects.SubjectValidationException;
import export.FileExporter;
import export.ReportGenerator;
import imports.BulkImportService;
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
    private static final ReportGenerator reportGenerator = new ReportGenerator(gradeManager);
    private static final FileExporter fileExporter = new FileExporter();
    private static final GPACalculator gpaCalculator = new GPACalculator();
    private static final BulkImportService bulkImportService = new BulkImportService(subjectRepository, studentManager, gradeManager);
    private static final StatisticsCalculator statisticsCalculator = new StatisticsCalculator();

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
            } catch (ImportException e) {
                System.out.println("\n\u2717 ERROR: ImportException");
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

        boolean summary = option.equals("1") || option.equals("3");
        boolean detailed = option.equals("2") || option.equals("3");

        int filesCreated = 0;
        long totalSize = 0;

        if (summary) {
            String content = reportGenerator.exportSummary(studentId);
            FileExporter.FileExportResult result = fileExporter.exportToFile(filename + "_summary.txt", content);
            filesCreated++;
            totalSize += result.getSize();
        }

        if (detailed) {
            String content = reportGenerator.exportDetailed(studentId);
            FileExporter.FileExportResult result = fileExporter.exportToFile(filename + "_detailed.txt", content);
            filesCreated++;
            totalSize += result.getSize();
        }

        System.out.println("\n\u2713 Report exported successfully!");
        for (int i = 0; i < filesCreated; i++) {
            String suffix = summary && detailed ? (i == 0 ? "_summary" : "_detailed") : "";
            System.out.println("  File: " + filename + suffix + ".txt");
        }
        System.out.println("  Location: ./reports/");
        System.out.println("  Size: " + (totalSize / 1024.0) + " KB");
        System.out.println("  Contains: " + gradeManager.getGradesForStudent(studentId).size() + " grades");

        promptEnter();
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
        double cumulativeGPA = gpaCalculator.cumulativeGPA(grades);

        for (Grade g : grades) {
            double gpa = gpaCalculator.percentageToGPA(g.getGrade());
            System.out.printf("%-16s | %.0f%%    | %.1f (%s)%n",
                    g.getSubject().getSubjectName(), g.getGrade(), gpa, gpaCalculator.gpaToLetter(gpa));
        }

        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        System.out.printf("Cumulative GPA: %.2f / 4.0%n", cumulativeGPA);
        System.out.println("Letter Grade: " + gpaCalculator.gpaToLetter(cumulativeGPA));

        List<Student> allStudents = studentManager.getAllStudents();
        List<Double> classAverages = allStudents.stream()
                .map(Student::calculateAverageGrade)
                .collect(java.util.stream.Collectors.toList());
        int rank = gpaCalculator.classRank(studentId, student.calculateAverageGrade(), classAverages);
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
        double classAvgGPA = gpaCalculator.percentageToGPA(classAvg);
        if (cumulativeGPA > classAvgGPA) {
            System.out.printf("\u2713 Above class average (%.2f GPA)%n", classAvgGPA);
        } else {
            System.out.printf("\u2717 Below class average (%.2f GPA)%n", classAvgGPA);
        }

        promptEnter();
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

        System.out.println("Validating file... \u2713");
        System.out.println("Processing grades...");

        try {
            BulkImportService.ImportResult result = bulkImportService.importFromFile(filename);

            System.out.println("\nIMPORT SUMMARY");
            System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
            System.out.println("Total Rows: " + (result.getSuccessCount() + result.getFailedCount()));
            System.out.println("Successfully Imported: " + result.getSuccessCount());
            System.out.println("Failed: " + result.getFailedCount());

            if (result.getFailedCount() > 0) {
                System.out.println("\nFailed Records:");
                for (String reason : result.getFailReasons()) {
                    System.out.println("  " + reason);
                }
            }

            System.out.println("\n\u2713 Import completed!");
            System.out.println("  " + result.getSuccessCount() + " grades added to system");
            System.out.println("  See " + result.getLogFilename() + " for details");
        } catch (ImportException e) {
            System.out.println("\n\u2717 ERROR: " + e.getMessage());
            if (e.getFilePath() != null) {
                System.out.println("  File: " + e.getFilePath());
            }
        }

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

        if (allGrades.isEmpty()) {
            System.out.println("No grades recorded yet.");
            promptEnter();
            return;
        }

        StatisticsCalculator.GradeDistribution dist = statisticsCalculator.calculateDistribution(allGrades);
        StatisticsCalculator.StatsResult stats = statisticsCalculator.calculateStats(allGrades, students);
        List<StatisticsCalculator.SubjectAverage> subjAverages = statisticsCalculator.calculateSubjectAverages(allGrades, subjectRepository.getAllSubjects());
        StatisticsCalculator.StudentTypeComparison typeComp = statisticsCalculator.compareStudentTypes(students);

        System.out.println("\nGRADE DISTRIBUTION");
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        String[] labels = dist.getLabels();
        int[] counts = dist.getCounts();
        for (int i = 0; i < 5; i++) {
            double pct = dist.getPercentages()[i];
            int barLen = (int) (pct / 2);
            String bar = "\u2588".repeat(Math.max(0, barLen)) + "\u2591".repeat(Math.max(0, 50 - barLen));
            System.out.printf("%s: %s %.1f%% (%d grades)%n", labels[i], bar, pct, counts[i]);
        }

        System.out.println("\nSTATISTICAL ANALYSIS");
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        System.out.printf("Mean (Average):      %.1f%%%n", stats.getMean());
        System.out.printf("Median:              %.1f%%%n", stats.getMedian());
        System.out.printf("Standard Deviation:  %.1f%%%n", stats.getStdDev());
        System.out.printf("Range:               %.1f%% (%.0f%% - %.0f%%)%n", stats.getRange(), stats.getMin(), stats.getMax());

        System.out.printf("%nHighest Grade:  %.0f%% (%s - %s)%n", stats.getMax(), stats.getMaxStudentName(), stats.getMaxSubjectName());
        System.out.printf("Lowest Grade:    %.0f%% (%s - %s)%n", stats.getMin(), stats.getMinStudentName(), stats.getMinSubjectName());

        System.out.println("\nSUBJECT PERFORMANCE");
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        for (StatisticsCalculator.SubjectAverage sa : subjAverages) {
            System.out.printf("  %s: %.1f%%%n", sa.getSubjectName(), sa.getAverage());
        }

        System.out.println("\nSTUDENT TYPE COMPARISON");
        System.out.println("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        if (typeComp.getRegularCount() > 0)
            System.out.printf("  Regular Students:  %.1f%% average (%d students)%n", typeComp.getRegularAverage(), typeComp.getRegularCount());
        if (typeComp.getHonorsCount() > 0)
            System.out.printf("  Honors Students:   %.1f%% average (%d students)%n", typeComp.getHonorsAverage(), typeComp.getHonorsCount());

        promptEnter();
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
