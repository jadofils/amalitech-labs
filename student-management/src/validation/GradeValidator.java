package validation;


import exceptions.grades.GradeException;

public class GradeValidator {

    // Validate grade range
    public static void validateGrade(double grade) {
        if (grade < 0 || grade > 100) {
            throw new GradeException("Grade must be between 0 and 100.");
        }
    }
}
