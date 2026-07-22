package exceptions;

/**
 * General-purpose exception for a Subject-related failure that isn't a
 * lookup miss ({@link SubjectNotFoundException}) or a field-validation
 * failure ({@link SubjectValidationException}) - e.g. the repository's
 * fixed-size backing array being full. Mirrors {@link GradeException}'s
 * role as the catch-all for its own entity.
 */
public class SubjectException extends ApplicationException {
    private final String subjectCode;

    public SubjectException(String message, String subjectCode) {
        super(message);
        this.subjectCode = subjectCode;
    }

    public SubjectException(String message) {
        super(message);
        this.subjectCode = null;
    }

    public String getSubjectCode() { return subjectCode; }
}
