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
        printDetailsDivider();
        printCommonDetails();
        System.out.println("Honors Eligible: " + (honorsEligible ? "Yes" : "No"));
        printDetailsDivider();
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
