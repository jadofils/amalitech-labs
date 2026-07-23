package main.model.subject;

import main.model.enums.SubjectType;

public class ElectiveSubject extends Subject {
    private static final boolean MANDATORY = false;

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
        return MANDATORY;
    }
}
