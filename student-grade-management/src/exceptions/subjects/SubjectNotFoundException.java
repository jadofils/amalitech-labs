package exceptions.subjects;

import exceptions.ApplicationException;

public class SubjectNotFoundException extends ApplicationException {
    public SubjectNotFoundException(String message) {
        super(message);
    }
}
