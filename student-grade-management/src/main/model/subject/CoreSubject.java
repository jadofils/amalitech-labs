package main.model.subject;

import main.model.enums.SubjectType;

public class CoreSubject extends Subject {
    private static final boolean MANDATORY = true;

    public CoreSubject(String subjectName, String subjectCode) {
        super(subjectName, subjectCode);
    }

    @Override
    public void displaySubjectDetails() {
        System.out.println("Core Subject: " + getSubjectName() + " (" + getSubjectCode() + ")");
    }

    @Override
    public SubjectType getSubjectType() {
        return SubjectType.CORE;
    }

    public boolean isMandatory() {
        return MANDATORY;
    }
}
