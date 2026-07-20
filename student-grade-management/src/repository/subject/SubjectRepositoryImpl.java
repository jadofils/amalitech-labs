package repository.subject;

import exceptions.SubjectNotFoundException;
import logging.Logger;
import model.subject.Subject;
import utils.validators.SubjectValidator;
import model.subject.CoreSubject;
import model.subject.ElectiveSubject;

import java.util.ArrayList;
import java.util.List;

public class SubjectRepositoryImpl implements SubjectRepository {

    private final Subject[] subjects = new Subject[50];
    private int subjectCount = 0;

    public SubjectRepositoryImpl() {
        // Seed Core Subjects (matching README specification)
        seedSubject(new CoreSubject("Mathematics", "MATH01"));
        seedSubject(new CoreSubject("English", "ENGL01"));
        seedSubject(new CoreSubject("Science", "SCIE01"));

        // Seed Elective Subjects (matching README specification)
        seedSubject(new ElectiveSubject("Music", "MUSC01"));
        seedSubject(new ElectiveSubject("Art", "ART01"));
        seedSubject(new ElectiveSubject("Physical Education", "PHED01"));
    }

    private void seedSubject(Subject subject) {
        subjects[subjectCount++] = subject;
    }

    @Override
    public void addSubject(Subject subject) {
        SubjectValidator.validateSubject(subject);
        if (subjectCount >= subjects.length) {
            Logger.error("Subject storage full at capacity " + subjects.length + "; rejected " + subject.getSubjectCode());
            throw new RuntimeException("Cannot add more subjects. Storage is full.");
        }
        subjects[subjectCount++] = subject;
    }

    @Override
    public Subject findSubjectByCode(String subjectCode) {
        for (int i = 0; i < subjectCount; i++) {
            if (subjects[i].getSubjectCode().equals(subjectCode)) {
                return subjects[i];
            }
        }
        throw new SubjectNotFoundException("Subject with code " + subjectCode + " not found");
    }

    @Override
    public List<Subject> getAllSubjects() {
        List<Subject> result = new ArrayList<>();
        for (int i = 0; i < subjectCount; i++) {
            result.add(subjects[i]);
        }
        return result;
    }

    @Override
    public void deleteSubject(String subjectCode) {
        for (int i = 0; i < subjectCount; i++) {
            if (subjects[i].getSubjectCode().equals(subjectCode)) {
                subjects[i] = subjects[subjectCount - 1];
                subjects[subjectCount - 1] = null;
                subjectCount--;
                return;
            }
        }
        throw new SubjectNotFoundException("Subject with code " + subjectCode + " not found.");
    }
}
