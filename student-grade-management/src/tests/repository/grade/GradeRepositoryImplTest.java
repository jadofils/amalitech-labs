package tests.repository.grade;

import exceptions.GradeException;
import model.grade.Grade;
import model.subject.CoreSubject;
import model.subject.Subject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.grade.GradeRepositoryImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GradeRepositoryImplTest {

    private final Subject math = new CoreSubject("Mathematics", "MATH01");

    @Test
    @DisplayName("A new repository starts with no grades (nothing is seeded)")
    void startsEmptyTest() {
        GradeRepositoryImpl repository = new GradeRepositoryImpl();
        assertTrue(repository.getAllGrades().isEmpty());
        assertEquals(0, repository.getGradeCount());
    }

    @Test
    @DisplayName("addGrade() makes the grade findable by ID and increases the count")
    void addGradeTest() {
        GradeRepositoryImpl repository = new GradeRepositoryImpl();
        Grade grade = new Grade("STU001", math, 85.0);

        repository.addGrade(grade);

        assertEquals(1, repository.getGradeCount());
        assertSame(grade, repository.findGradeById(grade.getGradeId()));
    }

    @Test
    @DisplayName("findGradeById() throws for an unknown ID")
    void findGradeByIdMissingThrowsTest() {
        GradeRepositoryImpl repository = new GradeRepositoryImpl();
        assertThrows(GradeException.class, () -> repository.findGradeById("GRD999"));
    }

    @Test
    @DisplayName("findGradesByStudentId() returns only that student's grades")
    void findGradesByStudentIdFiltersTest() {
        GradeRepositoryImpl repository = new GradeRepositoryImpl();
        Grade g1 = new Grade("STU001", math, 85.0);
        Grade g2 = new Grade("STU001", math, 90.0);
        Grade g3 = new Grade("STU002", math, 70.0);
        repository.addGrade(g1);
        repository.addGrade(g2);
        repository.addGrade(g3);

        List<Grade> stu001Grades = repository.findGradesByStudentId("STU001");

        assertEquals(2, stu001Grades.size());
        assertTrue(stu001Grades.contains(g1));
        assertTrue(stu001Grades.contains(g2));
        assertFalse(stu001Grades.contains(g3));
    }

    @Test
    @DisplayName("findGradesByStudentId() returns an empty list (not an exception) for an unknown student")
    void findGradesByStudentIdUnknownReturnsEmptyTest() {
        GradeRepositoryImpl repository = new GradeRepositoryImpl();
        assertTrue(repository.findGradesByStudentId("NOPE").isEmpty());
    }

    @Test
    @DisplayName("deleteGrade() removes the grade and decreases the count")
    void deleteGradeTest() {
        GradeRepositoryImpl repository = new GradeRepositoryImpl();
        Grade grade = new Grade("STU001", math, 85.0);
        repository.addGrade(grade);

        repository.deleteGrade(grade.getGradeId());

        assertEquals(0, repository.getGradeCount());
        assertThrows(GradeException.class, () -> repository.findGradeById(grade.getGradeId()));
    }

    @Test
    @DisplayName("deleteGrade() throws for an unknown ID")
    void deleteGradeMissingThrowsTest() {
        GradeRepositoryImpl repository = new GradeRepositoryImpl();
        assertThrows(GradeException.class, () -> repository.deleteGrade("GRD999"));
    }
}
