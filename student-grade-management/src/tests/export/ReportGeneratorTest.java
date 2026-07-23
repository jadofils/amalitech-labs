package tests.export;

import main.export.ReportGenerator;
import main.manager.GradeManager;
import main.manager.StudentManager;
import main.model.grade.Grade;
import main.model.student.Student;
import main.model.subject.CoreSubject;
import main.model.subject.Subject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import main.repository.student.StudentRepositoryImpl;
import main.repository.subject.SubjectRepositoryImpl;
import main.service.GradeService;
import main.service.StudentService;
import main.service.GradeServiceImpl;
import main.service.StudentServiceImpl;

import static org.junit.jupiter.api.Assertions.*;

class ReportGeneratorTest {

    private final Subject subject = new CoreSubject("Mathematics", "MATH01");

    private ReportGenerator newGenerator(StudentRepositoryImpl students, GradeManager gradeManager) {
        StudentService studentService = new StudentServiceImpl(students);
        StudentManager studentManager = new StudentManager(studentService, gradeManager);
        return new ReportGenerator(gradeManager, studentManager);
    }

    // Regression test for CHANGELOG.md KI-2: exportSummary() used to
    // hardcode the literal string "[name]" instead of the real student name
    // (reports/Emma_Wilson_reports_summary.txt in this repo is a real file
    // that bug produced).
    @Test
    @DisplayName("exportSummary() includes the student's real name, not a placeholder")
    void exportSummaryIncludesRealNameTest() {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(students, subjects);
        GradeManager gradeManager = new GradeManager(gradeService, subjects);
        ReportGenerator generator = newGenerator(students, gradeManager);
        Student student = students.getAllStudents().get(0);

        String summary = generator.exportSummary(student.getStudentId());

        assertFalse(summary.contains("[name]"));
        assertTrue(summary.contains("Name: " + student.getName()));
    }

    @Test
    @DisplayName("exportSummary() reports the correct overall average")
    void exportSummaryIncludesOverallAverageTest() {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(students, subjects);
        GradeManager gradeManager = new GradeManager(gradeService, subjects);
        ReportGenerator generator = newGenerator(students, gradeManager);
        Student student = students.getAllStudents().get(0);
        gradeManager.addGrade(new Grade(student.getStudentId(), subject, 80.0));

        String summary = generator.exportSummary(student.getStudentId());

        assertTrue(summary.contains("Overall Average: 80.0%"));
    }

    @Test
    @DisplayName("exportDetailed() lists every grade and includes a performance analysis section")
    void exportDetailedIncludesGradesAndPerformanceTest() {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(students, subjects);
        GradeManager gradeManager = new GradeManager(gradeService, subjects);
        ReportGenerator generator = newGenerator(students, gradeManager);
        Student student = students.getAllStudents().get(0);
        Grade grade = new Grade(student.getStudentId(), subject, 90.0);
        gradeManager.addGrade(grade);

        String detailed = generator.exportDetailed(student.getStudentId());

        assertTrue(detailed.contains(grade.getGradeId()));
        assertTrue(detailed.contains("Name: " + student.getName()));
        assertTrue(detailed.contains("PERFORMANCE ANALYSIS"));
        assertTrue(detailed.contains("Excellent performance"));
    }

    @Test
    @DisplayName("exportDetailed() classifies a below-60 average as needing improvement")
    void exportDetailedLowAverageNeedsImprovementTest() {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(students, subjects);
        GradeManager gradeManager = new GradeManager(gradeService, subjects);
        ReportGenerator generator = newGenerator(students, gradeManager);
        Student student = students.getAllStudents().get(0);
        gradeManager.addGrade(new Grade(student.getStudentId(), subject, 40.0));

        String detailed = generator.exportDetailed(student.getStudentId());

        assertTrue(detailed.contains("Needs improvement"));
    }

    @Test
    @DisplayName("An unknown student ID reports 'Unknown' instead of throwing")
    void unknownStudentReportsUnknownNameTest() {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(students, subjects);
        GradeManager gradeManager = new GradeManager(gradeService, subjects);
        ReportGenerator generator = newGenerator(students, gradeManager);

        String summary = generator.exportSummary("NOPE");

        assertTrue(summary.contains("Name: Unknown"));
    }
}
