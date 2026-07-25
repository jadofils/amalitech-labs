package main.utils.validators;

import main.exceptions.InvalidGradeException;

/**
 * Validates a grade's identifier fields and value before it is reconstructed from imported
 * (CSV/JSON/binary) data - {@code Grade.reconstruct(...)} itself intentionally skips this (see
 * {@code GradeTest.reconstructBypassesValidationTest}), since it also rehydrates already-validated
 * in-memory data. Imported data carries no such guarantee, so {@code GradeRecordMapper.toGrade()}
 * runs it first.
 *
 * <p>Date format is deliberately not checked here: {@code Grade.getDate()} is stamped in
 * {@code dd-MM-yyyy} ({@code DateFormats.DISPLAY_DATE}), not the ISO {@code yyyy-MM-dd} shape
 * {@link ValidationPatterns#DATE_ISO} validates - enforcing ISO here would reject every real
 * exported-then-reimported grade.
 */
public final class GradeValidator {

    private GradeValidator() {
    }

    public static void validateForImport(String gradeId, String studentId, double grade) {
        if (gradeId == null || !ValidationPatterns.GRADE_ID.matcher(gradeId).matches()) {
            throw new InvalidGradeException("Grade ID must be in the format GRD### (e.g. GRD001). Got: " + gradeId);
        }
        if (studentId == null || !ValidationPatterns.STUDENT_ID.matcher(studentId).matches()) {
            throw new InvalidGradeException("Student ID must be in the format STU### (e.g. STU001). Got: " + studentId);
        }
        if (grade < 0 || grade > 100) {
            throw new InvalidGradeException("Grade must be between 0 and 100. You entered: " + (int) grade, grade);
        }
    }
}
