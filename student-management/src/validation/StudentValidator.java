package validation;

import exceptions.StudentValidationException;
import model.student.Student;

import java.util.regex.Pattern;

public class StudentValidator {

    // Email regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    // Validate a student object
    public static void validateStudent(Student student) {
        validateName(student.getName());
        validateAge(student.getAge());
        validateEmail(student.getEmail());
        validateGrades(student.calculateAverageGrade());
    }

    // Name must be 4–100 characters
    public static void validateName(String name) {
        if (name == null || name.length() < 4 || name.length() > 100) {
            throw new StudentValidationException("Name must be between 4 and 100 characters.");
        }
    }

    // Age must be within a plausible school-age range (README examples use ages
    // 16-17)
    public static void validateAge(int age) {
        if (age < 5 || age > 100) {
            throw new StudentValidationException("Age must be between 5 and 100.");
        }
    }

    // Email must match regex
    public static void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new StudentValidationException("Invalid email format.");
        }
    }

    // Grades must be between 0–100
    public static void validateGrades(double averageGrade) {
        if (averageGrade < 0 || averageGrade > 100) {
            throw new StudentValidationException("Grades must be between 0 and 100.");
        }
    }
}
