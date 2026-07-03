package model.grade;

import exceptions.grades.GradeException;
import model.enums.SubjectType;
import model.enums.LetterGrade;
import model.subject.Subject;

import java.text.SimpleDateFormat;
import java.util.Date;

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
            throw new GradeException("Grade must be between 0 and 100.");
        }

        // Auto-generate grade ID
        this.gradeId = String.format("GRD%03d", gradeCounter++);
        // Auto-generate date
        this.date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
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
