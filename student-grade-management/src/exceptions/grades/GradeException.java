package exceptions.grades;

import exceptions.ApplicationException;

public class GradeException extends ApplicationException {
    private final String gradeId;

    public GradeException(String message, String gradeId) {
        super(message);
        this.gradeId = gradeId;
    }

    public GradeException(String message) {
        super(message);
        this.gradeId = null;
    }

    public String getGradeId() { return gradeId; }
}
