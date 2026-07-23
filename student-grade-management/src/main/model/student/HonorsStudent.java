package main.model.student;

import main.model.enums.StudentStatus;
import main.model.enums.StudentType;

public class HonorsStudent extends Student {
    private static final StudentType studentType = StudentType.HONORS;
    private boolean honorsEligible;

    // Constructor
    public HonorsStudent(String name, int age, String email, String phone) {
        super(name, age, email, phone);
        this.honorsEligible = checkHonorsEligibility();
    }

    public HonorsStudent(String studentId, String name, int age, String email, String phone, StudentStatus status) {
        super(studentId, name, age, email, phone, status);
        this.honorsEligible = checkHonorsEligibility();
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
        System.out.println("Honors Eligible: " + (honorsEligible ? "Yes" : "No"));
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

    // Method to check honors eligibility
    public boolean checkHonorsEligibility() {
        honorsEligible = calculateAverageGrade() >= getPassingGrade();
        return honorsEligible;
    }
}
