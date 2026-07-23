package tests.dataio;

import dataio.GradeRecord;
import dataio.GradeRecordMapper;
import model.grade.Grade;
import model.subject.Subject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.subject.SubjectRepositoryImpl;

import static org.junit.jupiter.api.Assertions.*;

// Pure mapping over real domain objects - GradeRecordMapper.toGrade() needs a
// SubjectRepository to resolve subjectCode, so a real SubjectRepositoryImpl is
// used here rather than a mock (matching the *Test.java / *MockitoTest.java
// split's "real collaborators" half). See GradeRecordMapperMockitoTest for the
// mocked-repository half (subject-not-found path).
class GradeRecordMapperTest {

    private final SubjectRepositoryImpl subjectRepository = new SubjectRepositoryImpl();
    private final Subject subject = subjectRepository.findSubjectByCode("MATH01");

    @Test
    @DisplayName("toRecord() captures every field needed to reconstruct a Grade")
    void toRecordCapturesGradeFieldsTest() {
        Grade grade = new Grade("STU001", subject, 85.0);

        GradeRecord record = GradeRecordMapper.toRecord(grade);

        assertEquals(grade.getGradeId(), record.gradeId());
        assertEquals("STU001", record.studentId());
        assertEquals("MATH01", record.subjectCode());
        assertEquals(85.0, record.grade());
        assertEquals(grade.getDate(), record.date());
    }

    @Test
    @DisplayName("toGrade() resolves subjectCode via the repository and rebuilds the Grade")
    void toGradeResolvesSubjectAndRebuildsTest() {
        GradeRecord record = new GradeRecord("GRD099", "STU002", "MATH01", 92.0, "01-01-2026");

        Grade rebuilt = GradeRecordMapper.toGrade(record, subjectRepository);

        assertEquals("GRD099", rebuilt.getGradeId());
        assertEquals("STU002", rebuilt.getStudentId());
        assertEquals("MATH01", rebuilt.getSubject().getSubjectCode());
        assertEquals(92.0, rebuilt.getGrade());
        assertEquals("01-01-2026", rebuilt.getDate());
    }

    @Test
    @DisplayName("Grade -> record -> Grade round-trips every field unchanged")
    void roundTripPreservesAllFieldsTest() {
        Grade original = new Grade("STU003", subject, 77.0);

        Grade rebuilt = GradeRecordMapper.toGrade(GradeRecordMapper.toRecord(original), subjectRepository);

        assertEquals(original.getGradeId(), rebuilt.getGradeId());
        assertEquals(original.getStudentId(), rebuilt.getStudentId());
        assertEquals(original.getSubject().getSubjectCode(), rebuilt.getSubject().getSubjectCode());
        assertEquals(original.getGrade(), rebuilt.getGrade());
        assertEquals(original.getDate(), rebuilt.getDate());
    }
}
