package manager;

import exceptions.StudentNotFoundException;
import model.grade.Grade;
import model.student.HonorsStudent;
import model.student.Student;
import service.StudentService;

import java.util.List;

// Backed by the database (StudentService/StudentRepository) instead of an in-memory array for now.
public class StudentManager {
    private final StudentService studentService;
    private final GradeManager gradeManager;

    public StudentManager(StudentService studentService, GradeManager gradeManager) {
        this.studentService = studentService;
        this.gradeManager = gradeManager;
        syncStudentCounter();
    }

    private void syncStudentCounter() {
        try {
            int highest = 0;
            for (Student student : studentService.getAllStudents()) {
                highest = Math.max(highest, extractSequence(student.getStudentId()));
            }
            Student.initializeCounter(highest);
        } catch (RuntimeException e) {
            // Database not reachable yet; counter will sync next time students are read successfully.
        }
    }

    private int extractSequence(String id) {
        String digits = id.replaceAll("\\D", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    public void addStudent(Student student) {
        studentService.addStudent(student);
    }

    public Student findStudent(String studentId) {
        try {
            Student student = studentService.getStudentById(studentId);
            hydrateGrades(student);
            return student;
        } catch (StudentNotFoundException e) {
            return null;
        }
    }

    public void viewAllStudents() {
        List<Student> students = studentService.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("No students found.");
            return;
        }
        double total = 0;
        for (Student student : students) {
            hydrateGrades(student);
            student.displayStudentDetails();
            total += student.calculateAverageGrade();
        }
        System.out.println("Total Students: " + students.size());
        System.out.printf("Average Class Grade: %.1f%%%n", total / students.size());
    }

    public double getAverageClassGrade() {
        List<Student> students = studentService.getAllStudents();
        if (students.isEmpty()) return 0.0;
        double total = 0;
        for (Student student : students) {
            hydrateGrades(student);
            total += student.calculateAverageGrade();
        }
        return total / students.size();
    }

    public int getStudentCount() {
        return studentService.getAllStudents().size();
    }

    public void updateStudent(Student student) {
        studentService.updateStudent(student);
    }

    public void deleteStudent(String studentId) {
        studentService.deleteStudent(studentId);
    }

    // Grades live in their own table, so a freshly loaded Student needs its
    // transient grade list (and honors eligibility) refreshed from GradeManager.
    private void hydrateGrades(Student student) {
        student.getGrades().clear();
        for (Grade grade : gradeManager.getGradesForStudent(student.getStudentId())) {
            student.addGrade(grade.getGrade());
        }
        if (student instanceof HonorsStudent honorsStudent) {
            honorsStudent.checkHonorsEligibility();
        }
    }
}
