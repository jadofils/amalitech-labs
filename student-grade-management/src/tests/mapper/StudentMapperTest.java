package tests.mapper;

import main.dto.StudentDTO;
import main.mapper.StudentMapper;
import main.model.student.HonorsStudent;
import main.model.student.RegularStudent;
import main.model.student.Student;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Pure mapping function with a real domain object, no collaborators to mock -
// no StudentMapperMockitoTest (see tests/README.md).
class StudentMapperTest {

    @Test
    void mapsAllFourDisplayFieldsTest() {
        Student student = new RegularStudent("Jane Doe", 20, "jane@example.com", "1234567890");
        student.addGrade(80.0);
        student.addGrade(90.0);

        StudentDTO dto = StudentMapper.toDto(student);

        assertEquals(student.getStudentId(), dto.getStudentId());
        assertEquals("Jane Doe", dto.getName());
        assertEquals(student.getStudentType(), dto.getStudentType());
        assertEquals(85.0, dto.getAverageGrade(), 0.001);
    }

    @Test
    void reflectsTheStudentsOwnTypeLabelTest() {
        Student honors = new HonorsStudent("Amy Chen", 21, "amy@example.com", "1234567890");

        StudentDTO dto = StudentMapper.toDto(honors);

        assertEquals(honors.getStudentType(), dto.getStudentType());
    }
}
