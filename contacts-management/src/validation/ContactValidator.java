package validation;

import exceptions.ContactValidationException;

import java.util.regex.Pattern;

public class ContactValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    // Name must be at least 2 characters
    public static void validateName(String name) {
        if (name == null || name.trim().length() < 2) {
            throw new ContactValidationException("Name must be at least 2 characters.");
        }
    }

    // Email must match a basic user@domain pattern
    public static void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ContactValidationException("Invalid email format.");
        }
    }
}
