package utils.validators;

import exceptions.SubjectValidationException;
import model.subject.Subject;

public class SubjectValidator {

    // Validate a subject object
    public static void validateSubject(Subject subject) {
        validateName(subject.getSubjectName());
        validateCode(subject.getSubjectCode());
    }

    // Subject name must be 3â€“100 characters
    public static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new SubjectValidationException("Subject name cannot be empty.");
        }
        if (name.length() < 3 || name.length() > 100) {
            throw new SubjectValidationException("Subject name must be between 3 and 100 characters.");
        }
    }

    // Subject code must follow a pattern (e.g., MATH101)
    public static void validateCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new SubjectValidationException("Subject code cannot be empty.");
        }
        if (!code.matches("^[A-Z]{2,}[0-9]{2,}$")) {
            throw new SubjectValidationException("Subject code must follow format like MATH101.");
        }
    }
}
