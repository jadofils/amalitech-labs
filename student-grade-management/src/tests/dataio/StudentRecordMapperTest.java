package tests.dataio;

import dataio.StudentRecord;
import dataio.StudentRecordMapper;
import model.enums.StudentStatus;
import model.student.HonorsStudent;
import model.student.RegularStudent;
import model.student.Student;
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

        StudentRecord record = StudentRecordMapper.toRecord(student);

        assertEquals("STU001", record.studentId());
        assertEquals("Musa Nkusi", record.name());
        assertEquals("REGULAR", record.studentType());
        assertEquals(17, record.age());
        assertEquals("musa@school.edu", record.email());
        assertEquals("1234567890", record.phone());
        assertEquals("ACTIVE", record.status());
    }

    @Test
    @DisplayName("toStudent() reconstructs a RegularStudent from its record")
    void toStudentReconstructsRegularStudentTest() {
        StudentRecord record = new StudentRecord("STU002", "Alice Johnson", "REGULAR", 16,
                "alice@school.edu", "1234567890", "ACTIVE");

        Student student = StudentRecordMapper.toStudent(record);

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
        StudentRecord record = new StudentRecord("STU003", "Bob Smith", "HONORS", 18,
                "bob@school.edu", "1234567890", "ACTIVE");

        Student student = StudentRecordMapper.toStudent(record);

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
