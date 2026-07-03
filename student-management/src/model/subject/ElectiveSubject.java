package model.subject;

import model.enums.SubjectType;

public class ElectiveSubject extends Subject {
    private final boolean mandatory = false;

    public ElectiveSubject(String subjectName, String subjectCode) {
        super(subjectName, subjectCode);
    }

    @Override
    public void displaySubjectDetails() {
        System.out.println("Elective Subject: " + getSubjectName() + " (" + getSubjectCode() + ")");
    }

    @Override
    public SubjectType getSubjectType() {
        return SubjectType.ELECTIVE;
    }

    public boolean isMandatory() {
        return mandatory;
    }
}
