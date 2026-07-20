package tests.manager;

import manager.GradeManager;
import manager.StudentManager;
import manager.StudentSearcher;
import model.grade.Grade;
import model.student.Student;
import model.subject.CoreSubject;
import model.subject.Subject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.student.StudentRepositoryImpl;
import repository.subject.impl.SubjectRepositoryImpl;
import service.GradeService;
import service.StudentService;
import service.serviceimpl.GradeServiceImpl;
import service.serviceimpl.StudentServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StudentSearcherTest {

    private final Subject subject = new CoreSubject("Mathematics", "MATH01");

    private StudentSearcher newSearcher(StudentRepositoryImpl students, GradeManager gradeManager) {
        StudentService studentService = new StudentServiceImpl(students);
        StudentManager studentManager = new StudentManager(studentService, gradeManager);
        return new StudentSearcher(studentManager);
    }

    @Test
    @DisplayName("searchById() returns exactly the matching student")
    void searchByIdFindsStudentTest() {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(students, subjects);
        GradeManager gradeManager = new GradeManager(gradeService, subjects);
        StudentSearcher searcher = newSearcher(students, gradeManager);
        Student student = students.getAllStudents().get(0);

        List<Student> results = searcher.searchById(student.getStudentId());

        assertEquals(1, results.size());
        assertEquals(student.getStudentId(), results.get(0).getStudentId());
    }

    @Test
    @DisplayName("searchById() returns an empty list (not null) for an unknown ID")
    void searchByIdUnknownReturnsEmptyTest() {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(students, subjects);
        GradeManager gradeManager = new GradeManager(gradeService, subjects);
        StudentSearcher searcher = newSearcher(students, gradeManager);

        assertTrue(searcher.searchById("NOPE").isEmpty());
    }

    @Test
    @DisplayName("searchByName() matches partially and case-insensitively")
    void searchByNamePartialCaseInsensitiveTest() {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(students, subjects);
        GradeManager gradeManager = new GradeManager(gradeService, subjects);
        StudentSearcher searcher = newSearcher(students, gradeManager);

        List<Student> results = searcher.searchByName("alice");

        assertEquals(1, results.size());
        assertEquals("Alice Johnson", results.get(0).getName());
    }

    @Test
    @DisplayName("searchByGradeRange() returns only students whose average falls within [min, max]")
    void searchByGradeRangeFiltersTest() {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(students, subjects);
        GradeManager gradeManager = new GradeManager(gradeService, subjects);
        StudentSearcher searcher = newSearcher(students, gradeManager);
        Student student = students.getAllStudents().get(0);
        gradeManager.addGrade(new Grade(student.getStudentId(), subject, 85.0));

        assertEquals(1, searcher.searchByGradeRange(80, 90).size());
        // Not 0-10: the other seeded students have no grades recorded, so
        // their average is 0.0 - which itself falls inside a 0-10 range and
        // would make this assertion pass for the wrong reason.
        assertTrue(searcher.searchByGradeRange(50, 60).isEmpty());
    }

    @Test
    @DisplayName("searchByType() separates Regular from Honors students")
    void searchByTypeSeparatesRegularAndHonorsTest() {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(students, subjects);
        GradeManager gradeManager = new GradeManager(gradeService, subjects);
        StudentSearcher searcher = newSearcher(students, gradeManager);

        // Seeded: 3 Regular, 2 Honors (StudentRepositoryImpl's constructor).
        assertEquals(3, searcher.searchByType(false).size());
        assertEquals(2, searcher.searchByType(true).size());
    }

    @Test
    @DisplayName("getSearchDescription() describes each search option")
    void getSearchDescriptionTest() {
        StudentSearcher searcher = newSearcher(new StudentRepositoryImpl(),
                new GradeManager(new GradeServiceImpl(), new SubjectRepositoryImpl()));

        assertEquals("ID: STU001", searcher.getSearchDescription("1", "STU001"));
        assertEquals("Name: \"John\"", searcher.getSearchDescription("2", "John"));
        assertEquals("Grade range: 80-90", searcher.getSearchDescription("3", "80-90"));
        assertEquals("Type: Honors", searcher.getSearchDescription("4", "2"));
        assertEquals("Type: Regular", searcher.getSearchDescription("4", "1"));
        assertEquals("", searcher.getSearchDescription("9", "anything"));
    }
}
