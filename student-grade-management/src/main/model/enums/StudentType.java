package main.model.enums;

public enum StudentType {
    REGULAR(50.0),
    HONORS(60.0);

    private final double passingGrade;

    // Constructor for enum constants
    StudentType(double passingGrade) {
        this.passingGrade = passingGrade;
    }

    public double getPassingGrade() {
        return passingGrade;
    }
}
