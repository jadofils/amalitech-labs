package tests.dataio;

import main.dataio.GradeRecord;
import main.dataio.GradeRecordMapper;
import main.exceptions.SubjectNotFoundException;
import main.model.grade.Grade;
import main.model.subject.CoreSubject;
import main.model.subject.Subject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import main.repository.subject.SubjectRepository;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class GradeRecordMapperMockitoTest {

    @Test
    @DisplayName("toGrade() propagates SubjectNotFoundException when subjectCode no longer exists")
    void toGradeThrowsWhenSubjectMissingTest() {
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        when(subjectRepository.findSubjectByCode("GONE01")).thenThrow(new SubjectNotFoundException("not found"));
        GradeRecord record = new GradeRecord("GRD001", "STU001", "GONE01", 80.0, "01-01-2026");

        assertThrows(SubjectNotFoundException.class, () -> GradeRecordMapper.toGrade(record, subjectRepository));
    }

    @Test
    @DisplayName("toGrade() looks the subject code up exactly once")
    void toGradeQueriesSubjectRepositoryOnceTest() {
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        Subject subject = new CoreSubject("Mathematics", "MATH01");
        when(subjectRepository.findSubjectByCode("MATH01")).thenReturn(subject);
        GradeRecord record = new GradeRecord("GRD001", "STU001", "MATH01", 80.0, "01-01-2026");

        GradeRecordMapper.toGrade(record, subjectRepository);

        verify(subjectRepository, times(1)).findSubjectByCode("MATH01");
    }

    @Test
    @DisplayName("toRecord() reads subjectCode from the grade's own Subject, not a lookup")
    void toRecordReadsSubjectCodeDirectlyTest() {
        Subject subject = mock(Subject.class);
        when(subject.getSubjectCode()).thenReturn("ENGL01");
        when(subject.getSubjectType()).thenReturn(main.model.enums.SubjectType.CORE);
        Grade grade = new Grade("STU001", subject, 70.0);

        GradeRecord record = GradeRecordMapper.toRecord(grade);

        assertEquals("ENGL01", record.subjectCode());
        verify(subject, atLeastOnce()).getSubjectCode();
    }
}
