package exceptions.grades;

public class GradeException extends RuntimeException {
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
