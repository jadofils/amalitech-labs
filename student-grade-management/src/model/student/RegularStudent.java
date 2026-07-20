package model.student;

import model.enums.StudentStatus;
import model.enums.StudentType;

public class RegularStudent extends Student {
    private final StudentType studentType = StudentType.REGULAR;

    public RegularStudent(String name, int age, String email, String phone) {
        super(name, age, email, phone);
    }

    public RegularStudent(String studentId, String name, int age, String email, String phone, StudentStatus status) {
        super(studentId, name, age, email, phone, status);
    }

    @Override
    public void displayStudentDetails() {
        System.out.println("─────────────────────────────────────────────");
        System.out.println("Student ID: " + getStudentId());
        System.out.println("Name: " + getName());
        System.out.println("Age: " + getAge());
        System.out.println("Email: " + getEmail());
        System.out.println("Phone: " + getPhone());
        System.out.println("Type: " + getStudentType());
        System.out.println("Passing Grade: " + studentType.getPassingGrade());
        System.out.println("Status: " + getStatus());
        System.out.println("Average Grade: " + calculateAverageGrade());
        System.out.println("Is Passing: " + (isPassing() ? "Yes" : "No"));
        System.out.println("─────────────────────────────────────────────");
    }

    @Override
    public StudentType getType() {
        return studentType;
    }

    @Override
    public double getPassingGrade() {
        return studentType.getPassingGrade();
    }
}
