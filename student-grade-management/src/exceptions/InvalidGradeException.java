package exceptions;

public class InvalidGradeException extends RuntimeException {
    private final double attemptedGrade;

    public InvalidGradeException(String message, double attemptedGrade) {
        super(message);
        this.attemptedGrade = attemptedGrade;
    }

    public InvalidGradeException(String message) {
        super(message);
        this.attemptedGrade = -1;
    }

    public double getAttemptedGrade() {
        return attemptedGrade;
    }
}
