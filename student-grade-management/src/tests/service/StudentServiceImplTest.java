package tests.service;

import main.exceptions.StudentNotFoundException;
import main.exceptions.StudentValidationException;
import main.model.enums.StudentStatus;
import main.model.student.RegularStudent;
import main.model.student.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import main.repository.student.StudentRepositoryImpl;
import main.service.StudentServiceImpl;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Uses a real StudentRepositoryImpl (not a mock) - these tests verify the
 * end-to-end outcome of validate-then-persist. StudentServiceImplMockitoTest
 * verifies the same behaviour purely through interactions with a mocked
 * main.repository.
 */
class StudentServiceImplTest {

    @Test
    @DisplayName("A valid student is validated and persisted")
    void addValidStudentTest() {
        StudentRepositoryImpl repository = new StudentRepositoryImpl();
        StudentServiceImpl service = new StudentServiceImpl(repository);
        int before = repository.getAllStudents().size();

        Student student = new RegularStudent("Valid Student", 17, "valid@school.edu", "1234567890");
        service.addStudent(student);

        assertEquals(before + 1, repository.getAllStudents().size());
        assertSame(student, service.getStudentById(student.getStudentId()));
    }

    @Test
    @DisplayName("An invalid student is rejected before it reaches the main.repository")
    void addInvalidStudentNeverPersistedTest() {
        StudentRepositoryImpl repository = new StudentRepositoryImpl();
        StudentServiceImpl service = new StudentServiceImpl(repository);
        int before = repository.getAllStudents().size();

        Student invalid = new RegularStudent("Bad Phone", 17, "valid@school.edu", "123"); // not 10 digits

        assertThrows(StudentValidationException.class, () -> service.addStudent(invalid));
        assertEquals(before, repository.getAllStudents().size());
    }

    @Test
    @DisplayName("getStudentById() throws for an unknown ID")
    void getStudentByIdMissingThrowsTest() {
        StudentServiceImpl service = new StudentServiceImpl(new StudentRepositoryImpl());
        assertThrows(StudentNotFoundException.class, () -> service.getStudentById("NOPE"));
    }

    @Test
    @DisplayName("getAllStudents() reflects the seeded students plus any added")
    void getAllStudentsTest() {
        StudentRepositoryImpl repository = new StudentRepositoryImpl();
        StudentServiceImpl service = new StudentServiceImpl(repository);
        int before = service.getAllStudents().size();

        service.addStudent(new RegularStudent("Another Student", 17, "another@school.edu", "1234567890"));

        assertEquals(before + 1, service.getAllStudents().size());
    }

    @Test
    @DisplayName("A valid update is persisted")
    void updateValidStudentTest() {
        StudentRepositoryImpl repository = new StudentRepositoryImpl();
        StudentServiceImpl service = new StudentServiceImpl(repository);
        String existingId = service.getAllStudents().get(0).getStudentId();

        Student updated = new RegularStudent(existingId, "Updated Name", 20, "updated@school.edu",
                "1234567890", StudentStatus.ACTIVE);
        service.updateStudent(updated);

        assertEquals("Updated Name", service.getStudentById(existingId).getName());
    }

    @Test
    @DisplayName("An invalid update is rejected before it reaches the main.repository")
    void updateInvalidStudentNeverPersistedTest() {
        StudentRepositoryImpl repository = new StudentRepositoryImpl();
        StudentServiceImpl service = new StudentServiceImpl(repository);
        String existingId = service.getAllStudents().get(0).getStudentId();
        String originalName = service.getStudentById(existingId).getName();

        Student invalidUpdate = new RegularStudent(existingId, "Bad", 20, "updated@school.edu",
                "1234567890", StudentStatus.ACTIVE); // name too short (<4 chars)

        assertThrows(StudentValidationException.class, () -> service.updateStudent(invalidUpdate));
        assertEquals(originalName, service.getStudentById(existingId).getName());
    }

    @Test
    @DisplayName("deleteStudent() removes the student")
    void deleteStudentTest() {
        StudentRepositoryImpl repository = new StudentRepositoryImpl();
        StudentServiceImpl service = new StudentServiceImpl(repository);
        String existingId = service.getAllStudents().get(0).getStudentId();

        service.deleteStudent(existingId);

        assertThrows(StudentNotFoundException.class, () -> service.getStudentById(existingId));
    }
}
