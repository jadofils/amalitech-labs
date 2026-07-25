package tests.service;

import main.exceptions.StudentNotFoundException;
import main.exceptions.GradeException;
import main.exceptions.SubjectNotFoundException;
import main.model.grade.Grade;
import main.model.subject.CoreSubject;
import main.model.subject.Subject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import main.repository.grade.GradeRepository;
import main.repository.student.StudentRepository;
import main.repository.subject.SubjectRepository;
import main.service.GradeServiceImpl;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Mocks all three repositories to verify GradeServiceImpl's own orchestration
 * logic: the ORDER existence checks happen in, and that a failure at any
 * step stops the ones after it from running.
 */
class GradeServiceImplMockitoTest {

    private final Subject subject = new CoreSubject("Mathematics", "MATH01");

    @Test
    @DisplayName("recordGrade() checks the student, then the subject, then persists - in that order")
    void recordGradeChecksInOrderTest() {
        StudentRepository students = mock(StudentRepository.class);
        SubjectRepository subjects = mock(SubjectRepository.class);
        GradeRepository grades = mock(GradeRepository.class);
        GradeServiceImpl service = new GradeServiceImpl(grades, students, subjects);
        Grade grade = new Grade("STU001", subject, 85.0);
        when(subjects.findSubjectByCode("MATH01")).thenReturn(subject);

        service.recordGrade(grade);

        InOrder inOrder = inOrder(students, subjects, grades);
        inOrder.verify(students).findStudentById("STU001");
        inOrder.verify(subjects).findSubjectByCode("MATH01");
        inOrder.verify(grades).addGrade(grade);
    }

    @Test
    @DisplayName("recordGrade() never checks the subject or persists if the student check fails")
    void recordGradeStudentFailureShortCircuitsTest() {
        StudentRepository students = mock(StudentRepository.class);
        SubjectRepository subjects = mock(SubjectRepository.class);
        GradeRepository grades = mock(GradeRepository.class);
        GradeServiceImpl service = new GradeServiceImpl(grades, students, subjects);
        Grade grade = new Grade("STU001", subject, 85.0);
        when(students.findStudentById("STU001")).thenThrow(new StudentNotFoundException("not found"));

        assertThrows(StudentNotFoundException.class, () -> service.recordGrade(grade));

        verify(subjects, never()).findSubjectByCode(any());
        verify(grades, never()).addGrade(any());
    }

    @Test
    @DisplayName("recordGrade() never persists if the subject check fails")
    void recordGradeSubjectFailureShortCircuitsTest() {
        StudentRepository students = mock(StudentRepository.class);
        SubjectRepository subjects = mock(SubjectRepository.class);
        GradeRepository grades = mock(GradeRepository.class);
        GradeServiceImpl service = new GradeServiceImpl(grades, students, subjects);
        Grade grade = new Grade("STU001", subject, 85.0);
        when(subjects.findSubjectByCode("MATH01")).thenThrow(new SubjectNotFoundException("not found"));

        assertThrows(SubjectNotFoundException.class, () -> service.recordGrade(grade));

        verify(grades, never()).addGrade(any());
    }

    @Test
    @DisplayName("getGradeById() wraps a null main.repository result in a GradeException")
    void getGradeByIdWrapsNullTest() {
        // The real GradeRepositoryImpl can never actually return null here (it
        // throws first) - this defensive branch is only reachable with a mock,
        // which is exactly why it's worth testing this way. See Lab 2's
        // technical documentation for the same observation.
        GradeRepository grades = mock(GradeRepository.class);
        when(grades.findGradeById("GRD001")).thenReturn(null);
        GradeServiceImpl service = new GradeServiceImpl(grades, mock(StudentRepository.class), mock(SubjectRepository.class));

        GradeException ex = assertThrows(GradeException.class, () -> service.getGradeById("GRD001"));
        assertEquals("Grade with ID GRD001 not found.", ex.getMessage());
    }

    @Test
    @DisplayName("getGradesByStudentId() delegates to the grade main.repository")
    void getGradesByStudentIdDelegatesTest() {
        GradeRepository grades = mock(GradeRepository.class);
        GradeServiceImpl service = new GradeServiceImpl(grades, mock(StudentRepository.class), mock(SubjectRepository.class));

        service.getGradesByStudentId("STU001");

        verify(grades, times(1)).findGradesByStudentId("STU001");
    }

    @Test
    @DisplayName("deleteGrade() delegates to the grade main.repository with the given ID")
    void deleteGradeDelegatesTest() {
        GradeRepository grades = mock(GradeRepository.class);
        GradeServiceImpl service = new GradeServiceImpl(grades, mock(StudentRepository.class), mock(SubjectRepository.class));

        service.deleteGrade("GRD001");

        verify(grades, times(1)).deleteGrade("GRD001");
    }
}
