package exceptions.subjects;

import exceptions.ApplicationException;

public class SubjectValidationException extends ApplicationException {
    public SubjectValidationException(String message) {
        super(message);
    }
}
