package main.model.grade;

import main.exceptions.InvalidGradeException;
import main.model.enums.SubjectType;
import main.model.enums.LetterGrade;
import main.model.subject.Subject;
import main.utils.DateFormats;

public class Grade implements Gradable {
    private static int gradeCounter = 1;

    private String gradeId;
    private String studentId;
    private Subject subject;
    private double grade;
    private String date;

    public Grade(String studentId, Subject subject, double gradeValue) {
        this.studentId = studentId;
        this.subject = subject;

        // Goes through the Gradable contract so the 0-100 range check lives in
        // exactly one place; an invalid value never gets an ID/date assigned.
        if (!recordGrade(gradeValue)) {
            throw new InvalidGradeException("Grade must be between 0 and 100. You entered: " + (int) gradeValue, gradeValue);
        }

        // Auto-generate grade ID
        this.gradeId = String.format("GRD%03d", gradeCounter++);
        // Auto-generate date
        this.date = DateFormats.now(DateFormats.DISPLAY_DATE);
    }

    private Grade(String gradeId, String studentId, Subject subject, double gradeValue, String date) {
        this.gradeId = gradeId;
        this.studentId = studentId;
        this.subject = subject;
        this.grade = gradeValue;
        this.date = date;
    }

    // Rebuilds a Grade already persisted in the database, keeping its real ID and
    // date instead of running them through the auto-generation in the main
    // constructor (which would mint a new ID and stamp today's date on every read).
    public static Grade reconstruct(String gradeId, String studentId, Subject subject, double gradeValue, String date) {
        return new Grade(gradeId, studentId, subject, gradeValue, date);
    }

    public String getGradeId() { return gradeId; }
    public String getStudentId() { return studentId; }
    public Subject getSubject() { return subject; }
    public double getGrade() { return grade; }
    public String getDate() { return date; }

    public void displayGradeDetails() {
        System.out.println("Grade ID: " + gradeId);
        System.out.println("Student ID: " + studentId);
        subject.displaySubjectDetails();
        System.out.println("Grade: " + grade + " (" + getLetterGrade() + ")");
        System.out.println("Date: " + date);
    }

    public LetterGrade getLetterGrade() {
        return LetterGrade.fromNumeric(grade);
    }

    public SubjectType getSubjectType() {
        return subject.getSubjectType();
    }

    @Override
    public boolean validateGrade(double gradeValue) {
        return gradeValue >= 0 && gradeValue <= 100;
    }

    @Override
    public boolean recordGrade(double gradeValue) {
        if (!validateGrade(gradeValue)) {
            return false;
        }
        this.grade = gradeValue;
        return true;
    }

    // Keeps the static ID counter ahead of whatever is already persisted,
    // since it resets to 1 on every JVM restart but the database does not.
    public static void initializeCounter(int highestExistingSequence) {
        gradeCounter = Math.max(gradeCounter, highestExistingSequence + 1);
    }
}
