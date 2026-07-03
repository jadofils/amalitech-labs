package model.student;

import model.enums.StudentStatus;

import java.util.ArrayList;
import java.util.List;

public abstract class Student {
    private String studentId;
    private String name;
    private int age;
    private String email;
    private String phone;
    private StudentStatus status;  // use enum instead of String

    private static int studentCounter = 0;
    private List<Double> grades = new ArrayList<>();

    // Constructor
    public Student(String name, int age, String email, String phone) {
        this.studentId = generateStudentId();
        this.name = name;
        this.age = age;
        this.email = email;
        this.phone = phone;
        this.status = StudentStatus.ACTIVE; // default status
    }

    // Generate unique student ID
    private String generateStudentId() {
        studentCounter++;
        return String.format("STU%03d", studentCounter);
    }

    // Getters and setters
    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public StudentStatus getStatus() { return status; }
    public void setStatus(StudentStatus status) { this.status = status; }

    public List<Double> getGrades() { return grades; }
    public void setGrades(List<Double> grades) { this.grades = grades; }

    // Add grade
    public void addGrade(double grade) {
        grades.add(grade);
    }

    // Calculate average grade
    public double calculateAverageGrade() {
        if (grades.isEmpty()) return 0.0;
        double sum = 0;
        for (double g : grades) {
            sum += g;
        }
        return sum / grades.size();
    }

    // Check passing status
    public boolean isPassing() {
        return calculateAverageGrade() >= getPassingGrade();
    }

    // Abstract methods
    public abstract void displayStudentDetails();
    public abstract String getStudentType();
    public abstract double getPassingGrade();

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    // Keeps the static ID counter ahead of whatever is already persisted,
    // since it resets to 0 on every JVM restart but the database does not.
    public static void initializeCounter(int highestExistingSequence) {
        studentCounter = Math.max(studentCounter, highestExistingSequence);
    }
}
