package validation;

import exceptions.ContactValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContactValidatorTest {

    @Test
    void validateNameAcceptsAReasonableName() {
        assertDoesNotThrow(() -> ContactValidator.validateName("Ada Lovelace"));
    }

    @Test
    void validateNameRejectsNull() {
        assertThrows(ContactValidationException.class, () -> ContactValidator.validateName(null));
    }

    @Test
    void validateNameRejectsBlank() {
        assertThrows(ContactValidationException.class, () -> ContactValidator.validateName("  "));
    }

    @Test
    void validateEmailAcceptsAWellFormedAddress() {
        assertDoesNotThrow(() -> ContactValidator.validateEmail("ada@example.com"));
    }

    @Test
    void validateEmailRejectsMissingAtSign() {
        assertThrows(ContactValidationException.class, () -> ContactValidator.validateEmail("ada.example.com"));
    }

    @Test
    void validateEmailRejectsNull() {
        assertThrows(ContactValidationException.class, () -> ContactValidator.validateEmail(null));
    }
}
