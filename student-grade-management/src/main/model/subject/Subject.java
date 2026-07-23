package main.model.subject;

import main.model.enums.SubjectType;

public abstract class Subject {
    private String subjectName;
    private String subjectCode;

    // constructor
    protected Subject(String subjectName, String subjectCode) {
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
    }

    // getters and setters
    public String getSubjectName() { return subjectName; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    // abstract methods
    public abstract void displaySubjectDetails();
    public abstract SubjectType getSubjectType();
}
