package main.model.grade;

public interface Gradable {
    boolean recordGrade(double grade);
    boolean validateGrade(double grade);
}
