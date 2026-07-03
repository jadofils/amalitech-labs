package exceptions.subjects;

public class SubjectValidationException extends RuntimeException {
    public SubjectValidationException(String message) {
        super(message);
    }
}
