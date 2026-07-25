package tests.service;

import main.exceptions.StudentNotFoundException;
import main.exceptions.GradeException;
import main.exceptions.SubjectNotFoundException;
import main.model.grade.Grade;
import main.model.student.Student;
import main.model.subject.CoreSubject;
import main.model.subject.Subject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import main.repository.student.StudentRepositoryImpl;
import main.repository.subject.SubjectRepositoryImpl;
import main.service.GradeServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Wires GradeServiceImpl to real repositories (the same constructor Main.java
 * uses), so these tests exercise the actual existence checks end-to-end.
 * GradeServiceImplMockitoTest verifies the same logic, plus the call
 * ordering, with fully mocked repositories.
 */
class GradeServiceImplTest {

    private GradeServiceImpl newService(StudentRepositoryImpl students, SubjectRepositoryImpl subjects) {
        return new GradeServiceImpl(students, subjects);
    }

    @Test
    @DisplayName("recordGrade() succeeds for a known student and a known subject")
    void recordGradeSuccessTest() {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeServiceImpl service = newService(students, subjects);
        String studentId = students.getAllStudents().get(0).getStudentId();
        Subject subject = subjects.getAllSubjects().get(0); // a real, registered subject

        Grade grade = new Grade(studentId, subject, 85.0);
        service.recordGrade(grade);

        assertTrue(service.getAllGrades().contains(grade));
        assertEquals(1, service.getGradesByStudentId(studentId).size());
    }

    @Test
    @DisplayName("recordGrade() rejects an unknown student and persists nothing")
    void recordGradeUnknownStudentTest() {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeServiceImpl service = newService(students, subjects);
        Subject subject = subjects.getAllSubjects().get(0);

        Grade grade = new Grade("NOPE", subject, 85.0);

        assertThrows(StudentNotFoundException.class, () -> service.recordGrade(grade));
        assertTrue(service.getAllGrades().isEmpty());
    }

    @Test
    @DisplayName("recordGrade() rejects a subject that was never registered in the main.repository")
    void recordGradeUnknownSubjectTest() {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeServiceImpl service = newService(students, subjects);
        String studentId = students.getAllStudents().get(0).getStudentId();

        // A real Subject instance, but one that was constructed directly and
        // never added to subjects - GradeService still checks the main.repository,
        // not the reference on the Grade object itself.
        Subject unregisteredSubject = new CoreSubject("Philosophy", "PHIL01");
        Grade grade = new Grade(studentId, unregisteredSubject, 85.0);

        assertThrows(SubjectNotFoundException.class, () -> service.recordGrade(grade));
        assertTrue(service.getAllGrades().isEmpty());
    }

    @Test
    @DisplayName("getGradeById() throws for an unknown ID")
    void getGradeByIdMissingThrowsTest() {
        GradeServiceImpl service = newService(new StudentRepositoryImpl(), new SubjectRepositoryImpl());
        assertThrows(GradeException.class, () -> service.getGradeById("GRD999"));
    }

    @Test
    @DisplayName("getGradesByStudentId() returns only that student's grades")
    void getGradesByStudentIdFiltersTest() {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeServiceImpl service = newService(students, subjects);
        List<Student> allStudents = students.getAllStudents();
        Subject subject = subjects.getAllSubjects().get(0);

        service.recordGrade(new Grade(allStudents.get(0).getStudentId(), subject, 80.0));
        service.recordGrade(new Grade(allStudents.get(1).getStudentId(), subject, 90.0));

        assertEquals(1, service.getGradesByStudentId(allStudents.get(0).getStudentId()).size());
    }

    @Test
    @DisplayName("deleteGrade() removes the grade")
    void deleteGradeTest() {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeServiceImpl service = newService(students, subjects);
        String studentId = students.getAllStudents().get(0).getStudentId();
        Subject subject = subjects.getAllSubjects().get(0);
        Grade grade = new Grade(studentId, subject, 85.0);
        service.recordGrade(grade);

        service.deleteGrade(grade.getGradeId());

        assertTrue(service.getAllGrades().isEmpty());
    }

    @Test
    @DisplayName("deleteGrade() throws for an unknown ID")
    void deleteGradeMissingThrowsTest() {
        GradeServiceImpl service = newService(new StudentRepositoryImpl(), new SubjectRepositoryImpl());
        assertThrows(GradeException.class, () -> service.deleteGrade("GRD999"));
    }
}
