package tests.repository.subject;

import exceptions.subjects.SubjectNotFoundException;
import exceptions.subjects.SubjectValidationException;
import model.enums.SubjectType;
import model.subject.CoreSubject;
import model.subject.ElectiveSubject;
import model.subject.Subject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.subject.impl.SubjectRepositoryImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SubjectRepositoryImplTest {

    @Test
    @DisplayName("A new repository is pre-seeded with 6 subjects: 3 core, 3 elective")
    void seedsSixSubjectsTest() {
        SubjectRepositoryImpl repository = new SubjectRepositoryImpl();
        List<Subject> subjects = repository.getAllSubjects();

        assertEquals(6, subjects.size());
        long coreCount = subjects.stream().filter(s -> s.getSubjectType() == SubjectType.CORE).count();
        long electiveCount = subjects.stream().filter(s -> s.getSubjectType() == SubjectType.ELECTIVE).count();
        assertEquals(3, coreCount);
        assertEquals(3, electiveCount);
    }

    @Test
    @DisplayName("addSubject() makes the subject findable by code and increases the count")
    void addSubjectTest() {
        SubjectRepositoryImpl repository = new SubjectRepositoryImpl();
        int before = repository.getAllSubjects().size();

        Subject subject = new CoreSubject("History", "HIST01");
        repository.addSubject(subject);

        assertEquals(before + 1, repository.getAllSubjects().size());
        assertSame(subject, repository.findSubjectByCode("HIST01"));
    }

    // Unlike StudentRepositoryImpl.addStudent() and GradeRepositoryImpl.addGrade(),
    // this repository calls SubjectValidator directly - so a malformed subject is
    // rejected here, at the storage layer, not only at the service layer above it.
    @Test
    @DisplayName("addSubject() enforces SubjectValidator's rules directly")
    void addSubjectValidatesTest() {
        SubjectRepositoryImpl repository = new SubjectRepositoryImpl();
        Subject invalid = new ElectiveSubject("Drama", "drama"); // lowercase code fails the format regex
        assertThrows(SubjectValidationException.class, () -> repository.addSubject(invalid));
    }

    @Test
    @DisplayName("findSubjectByCode() throws for an unknown code")
    void findSubjectByCodeMissingThrowsTest() {
        SubjectRepositoryImpl repository = new SubjectRepositoryImpl();
        assertThrows(SubjectNotFoundException.class, () -> repository.findSubjectByCode("NOPE99"));
    }

    @Test
    @DisplayName("deleteSubject() removes the subject and decreases the count")
    void deleteSubjectTest() {
        SubjectRepositoryImpl repository = new SubjectRepositoryImpl();
        int before = repository.getAllSubjects().size();

        repository.deleteSubject("MATH01");

        assertEquals(before - 1, repository.getAllSubjects().size());
        assertThrows(SubjectNotFoundException.class, () -> repository.findSubjectByCode("MATH01"));
    }

    @Test
    @DisplayName("deleteSubject() throws for an unknown code")
    void deleteSubjectMissingThrowsTest() {
        SubjectRepositoryImpl repository = new SubjectRepositoryImpl();
        assertThrows(SubjectNotFoundException.class, () -> repository.deleteSubject("NOPE99"));
    }
}
