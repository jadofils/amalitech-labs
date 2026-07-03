package model;

public class HonorsStudent extends Student {
    private final double passingGrade = 60.0;
    private boolean honorsEligible;

    // Constructor
    public HonorsStudent(String name, int age, String email, String phone) {
        super(name, age, email, phone);
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
        System.out.println("Passing Grade: " + passingGrade);
        System.out.println("Status: " + getStatus());
        System.out.println("Average Grade: " + calculateAverageGrade());
        System.out.println("Is Passing: " + (isPassing() ? "Yes" : "No"));
        System.out.println("Honors Eligible: " + (honorsEligible ? "Yes" : "No"));
        System.out.println("─────────────────────────────────────────────");
    }

    @Override
    public String getStudentType() {
        return "Honors";
    }

    @Override
    public double getPassingGrade() {
        return passingGrade;
    }

    // Method to check honors eligibility
    public boolean checkHonorsEligibility() {
        honorsEligible = calculateAverageGrade() >= 85.0;
        return honorsEligible;
    }
}
