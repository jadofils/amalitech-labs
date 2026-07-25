package tests.manager;

import main.manager.GradeManager;
import main.model.enums.SubjectType;
import main.model.grade.Grade;
import main.model.subject.Subject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import main.repository.subject.SubjectRepository;
import main.service.GradeService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Mocks GradeService and SubjectRepository so GradeManager's own filtering and
 * arithmetic can be verified independent of the real Grade/Subject/main.repository
 * implementations.
 */
class GradeManagerMockitoTest {

    private Subject mockSubject(SubjectType type) {
        Subject subject = mock(Subject.class);
        when(subject.getSubjectType()).thenReturn(type);
        return subject;
    }

    private Grade mockGrade(double value, SubjectType type) {
        Grade grade = mock(Grade.class);
        when(grade.getGrade()).thenReturn(value);
        when(grade.getSubjectType()).thenReturn(type);
        return grade;
    }

    @Test
    @DisplayName("getSubjectsByType() filters the main.repository's full list by type")
    void getSubjectsByTypeFiltersTest() {
        GradeService gradeService = mock(GradeService.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        GradeManager manager = new GradeManager(gradeService, subjectRepository);
        Subject core = mockSubject(SubjectType.CORE);
        Subject elective = mockSubject(SubjectType.ELECTIVE);
        when(subjectRepository.getAllSubjects()).thenReturn(List.of(core, elective));

        List<Subject> result = manager.getSubjectsByType(SubjectType.CORE);

        assertEquals(List.of(core), result);
        verify(subjectRepository, times(1)).getAllSubjects();
    }

    @Test
    @DisplayName("calculateCoreAverage() only averages grades whose subject type is CORE")
    void calculateCoreAverageFiltersByTypeTest() {
        GradeService gradeService = mock(GradeService.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        GradeManager manager = new GradeManager(gradeService, subjectRepository);
        // Build the mocked grades as a separate statement first - stubbing a
        // new mock (mockGrade) *inside* the arguments of .thenReturn() would
        // interleave with the outer when(...) before it completes and trip
        // Mockito's UnfinishedStubbingException.
        List<Grade> grades = List.of(
                mockGrade(80.0, SubjectType.CORE),
                mockGrade(100.0, SubjectType.CORE),
                mockGrade(0.0, SubjectType.ELECTIVE));
        when(gradeService.getGradesByStudentId("STU001")).thenReturn(grades);

        double coreAverage = manager.calculateCoreAverage("STU001");

        assertEquals(90.0, coreAverage, 0.0001);
    }

    @Test
    @DisplayName("calculateOverallAverage() averages every grade regardless of subject type")
    void calculateOverallAverageIncludesAllTest() {
        GradeService gradeService = mock(GradeService.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        GradeManager manager = new GradeManager(gradeService, subjectRepository);
        List<Grade> grades = List.of(
                mockGrade(80.0, SubjectType.CORE),
                mockGrade(60.0, SubjectType.ELECTIVE));
        when(gradeService.getGradesByStudentId("STU001")).thenReturn(grades);

        assertEquals(70.0, manager.calculateOverallAverage("STU001"), 0.0001);
    }

    @Test
    @DisplayName("addGrade() delegates to GradeService.recordGrade() with the exact same object")
    void addGradeDelegatesTest() {
        GradeService gradeService = mock(GradeService.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        GradeManager manager = new GradeManager(gradeService, subjectRepository);
        Grade grade = mockGrade(80.0, SubjectType.CORE);

        manager.addGrade(grade);

        verify(gradeService, times(1)).recordGrade(grade);
    }

    @Test
    @DisplayName("getGradeCount() delegates to GradeService.getAllGrades().size()")
    void getGradeCountDelegatesTest() {
        GradeService gradeService = mock(GradeService.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        GradeManager manager = new GradeManager(gradeService, subjectRepository);
        List<Grade> grades = List.of(mockGrade(80.0, SubjectType.CORE), mockGrade(90.0, SubjectType.CORE));
        when(gradeService.getAllGrades()).thenReturn(grades);

        assertEquals(2, manager.getGradeCount());
    }
}
