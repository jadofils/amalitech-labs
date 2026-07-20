package tests.mapper;

import dto.GradeDTO;
import mapper.GradeMapper;
import model.enums.SubjectType;
import model.grade.Grade;
import model.subject.CoreSubject;
import model.subject.Subject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Pure mapping function with a real domain object, no collaborators to mock -
// no GradeMapperMockitoTest (see tests/README.md).
class GradeMapperTest {

    @Test
    void mapsAllFiveGradeHistoryFieldsTest() {
        Subject subject = new CoreSubject("Mathematics", "MATH01");
        Grade grade = new Grade("STU001", subject, 85.0);

        GradeDTO dto = GradeMapper.toDto(grade);

        assertEquals(grade.getGradeId(), dto.getGradeId());
        assertEquals(grade.getDate(), dto.getDate());
        assertEquals("Mathematics", dto.getSubjectName());
        assertEquals(SubjectType.CORE, dto.getSubjectType());
        assertEquals(85.0, dto.getGrade(), 0.001);
    }
}
