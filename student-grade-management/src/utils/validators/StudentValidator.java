package utils.validators;

import exceptions.StudentValidationException;
import model.student.Student;

import java.util.regex.Pattern;

public class StudentValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z ]+$");
    private static final Pattern ID_PATTERN = Pattern.compile("^[A-Za-z0-9]{4,}$");

    public static void validateStudent(Student student) {
        if (student == null) {
            throw new StudentValidationException("Student object cannot be null.");
        }
        validateId(student.getStudentId());
        validateName(student.getName());
        validateAge(student.getAge());
        validateEmail(student.getEmail());
        validatePhone(student.getPhone());
        validateStatus(student.getStatus());
        validateGrades(student.calculateAverageGrade());
    }

    public static void validateId(String studentId) {
        if (studentId == null || !ID_PATTERN.matcher(studentId).matches()) {
            throw new StudentValidationException("Student ID must be at least 4 characters and alphanumeric.");
        }
    }

    public static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new StudentValidationException("Student name cannot be empty or contain only spaces.");
        }
        if (name.length() < 4 || name.length() > 100) {
            throw new StudentValidationException("Name must be between 4 and 100 characters.");
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new StudentValidationException("Name must contain only letters and spaces.");
        }
    }

    public static void validateAge(int age) {
        if (age < 5 || age > 100) {
            throw new StudentValidationException("Age must be between 5 and 100.");
        }
    }

    public static void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new StudentValidationException("Invalid email format.");
        }
    }

    public static void validatePhone(String phone) {
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new StudentValidationException("Phone number must be exactly 10 digits.");
        }
    }

    public static void validateGrades(double averageGrade) {
        if (averageGrade < 0 || averageGrade > 100) {
            throw new StudentValidationException("Grades must be between 0 and 100.");
        }
    }

    public static void validateStatus(Object status) {
        if (status == null) {
            throw new StudentValidationException("Student status cannot be null.");
        }
    }
}
