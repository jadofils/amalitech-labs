package tests.dataio;

import main.dataio.StudentRecord;
import main.dataio.StudentRecordMapper;
import main.model.enums.StudentStatus;
import main.model.student.HonorsStudent;
import main.model.student.RegularStudent;
import main.model.student.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Pure mapping, no injected collaborator to mock - same reasoning as StudentMapperTest
// (see tests/README.md), so there is no StudentRecordMapperMockitoTest.
class StudentRecordMapperTest {

    @Test
    @DisplayName("toRecord() captures every field needed to reconstruct a RegularStudent")
    void toRecordCapturesRegularStudentFieldsTest() {
        Student student = new RegularStudent("STU001", "Musa Nkusi", 17, "musa@school.edu",
                "1234567890", StudentStatus.ACTIVE);

        StudentRecord studentRecord = StudentRecordMapper.toRecord(student);

        assertEquals("STU001", studentRecord.studentId());
        assertEquals("Musa Nkusi", studentRecord.name());
        assertEquals("REGULAR", studentRecord.studentType());
        assertEquals(17, studentRecord.age());
        assertEquals("musa@school.edu", studentRecord.email());
        assertEquals("1234567890", studentRecord.phone());
        assertEquals("ACTIVE", studentRecord.status());
    }

    @Test
    @DisplayName("toStudent() reconstructs a RegularStudent from its record")
    void toStudentReconstructsRegularStudentTest() {
        StudentRecord studentRecord = new StudentRecord("STU002", "Alice Johnson", "REGULAR", 16,
                "alice@school.edu", "1234567890", "ACTIVE");

        Student student = StudentRecordMapper.toStudent(studentRecord);

        assertInstanceOf(RegularStudent.class, student);
        assertEquals("STU002", student.getStudentId());
        assertEquals("Alice Johnson", student.getName());
        assertEquals(16, student.getAge());
        assertEquals("alice@school.edu", student.getEmail());
        assertEquals("1234567890", student.getPhone());
        assertEquals(StudentStatus.ACTIVE, student.getStatus());
    }

    @Test
    @DisplayName("toStudent() reconstructs an HonorsStudent (not a RegularStudent) when studentType is HONORS")
    void toStudentReconstructsHonorsStudentTest() {
        StudentRecord studentRecord = new StudentRecord("STU003", "Bob Smith", "HONORS", 18,
                "bob@school.edu", "1234567890", "ACTIVE");

        Student student = StudentRecordMapper.toStudent(studentRecord);

        assertInstanceOf(HonorsStudent.class, student);
        assertEquals(60.0, student.getPassingGrade());
    }

    @Test
    @DisplayName("Student -> record -> Student round-trips every field unchanged")
    void roundTripPreservesAllFieldsTest() {
        Student original = new HonorsStudent("STU004", "David Chen", 17, "david@school.edu",
                "1234567890", StudentStatus.INACTIVE);

        Student rebuilt = StudentRecordMapper.toStudent(StudentRecordMapper.toRecord(original));

        assertEquals(original.getStudentId(), rebuilt.getStudentId());
        assertEquals(original.getName(), rebuilt.getName());
        assertEquals(original.getType(), rebuilt.getType());
        assertEquals(original.getAge(), rebuilt.getAge());
        assertEquals(original.getEmail(), rebuilt.getEmail());
        assertEquals(original.getPhone(), rebuilt.getPhone());
        assertEquals(original.getStatus(), rebuilt.getStatus());
    }
}
