package repository.subject.impl;

import exceptions.subjects.SubjectNotFoundException;
import model.subject.Subject;
import model.subject.CoreSubject;
import model.subject.ElectiveSubject;
import repository.subject.SubjectRepository;
import validation.SubjectValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubjectRepositoryImpl implements SubjectRepository {

    private final Map<String, Subject> subjectsMap = new HashMap<>(50);

    public SubjectRepositoryImpl() {
        // Seed Core Subjects
        seedSubject(new CoreSubject("Mathematics", "MATH01"));
        seedSubject(new CoreSubject("English", "ENGL01"));
        seedSubject(new CoreSubject("Science", "SCIE01"));
        seedSubject(new CoreSubject("History", "HIST01"));
        seedSubject(new CoreSubject("Physics", "PHYS01"));

        // Seed Elective Subjects
        seedSubject(new ElectiveSubject("Music", "MUSC01"));
        seedSubject(new ElectiveSubject("Art", "ART01"));
        seedSubject(new ElectiveSubject("Physical Education", "PHED01"));
        seedSubject(new ElectiveSubject("Drama", "DRAM01"));
        seedSubject(new ElectiveSubject("Computer Science", "COMP01"));
    }

    private void seedSubject(Subject subject) {
        subjectsMap.put(subject.getSubjectCode(), subject);
    }

    @Override
    public void addSubject(Subject subject) {
        // Validate subject before saving
        SubjectValidator.validateSubject(subject);
        subjectsMap.put(subject.getSubjectCode(), subject);
    }

    @Override
    public Subject findSubjectByCode(String subjectCode) {
        Subject subject = subjectsMap.get(subjectCode);
        if (subject == null) {
            throw new SubjectNotFoundException("Subject with code " + subjectCode + " not found");
        }
        return subject;
    }

    @Override
    public List<Subject> getAllSubjects() {
        return new ArrayList<>(subjectsMap.values());
    }

    @Override
    public void deleteSubject(String subjectCode) {
        if (!subjectsMap.containsKey(subjectCode)) {
            throw new SubjectNotFoundException("Subject with code " + subjectCode + " not found.");
        }
        subjectsMap.remove(subjectCode);
    }
}