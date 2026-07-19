package tests.repository.student;

import exceptions.StudentNotFoundException;
import model.enums.StudentStatus;
import model.student.RegularStudent;
import model.student.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.student.StudentRepositoryImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StudentRepositoryImplTest {

    @Test
    @DisplayName("A new repository is pre-seeded with 5 students: 3 Regular, 2 Honors")
    void seedsFiveStudentsTest() {
        StudentRepositoryImpl repository = new StudentRepositoryImpl();
        List<Student> students = repository.getAllStudents();

        assertEquals(5, students.size());
        long regularCount = students.stream().filter(s -> s.getStudentType().equals("REGULAR")).count();
        long honorsCount = students.stream().filter(s -> s.getStudentType().equals("HONORS")).count();
        assertEquals(3, regularCount);
        assertEquals(2, honorsCount);
    }

    @Test
    @DisplayName("addStudent() makes the student findable and increases the count")
    void addStudentTest() {
        StudentRepositoryImpl repository = new StudentRepositoryImpl();
        int before = repository.getAllStudents().size();

        Student student = new RegularStudent("New Student", 16, "new@school.edu", "1234567890");
        repository.addStudent(student);

        assertEquals(before + 1, repository.getAllStudents().size());
        assertSame(student, repository.findStudentById(student.getStudentId()));
    }

    @Test
    @DisplayName("findStudentById() throws for an unknown ID")
    void findStudentByIdMissingThrowsTest() {
        StudentRepositoryImpl repository = new StudentRepositoryImpl();
        assertThrows(StudentNotFoundException.class, () -> repository.findStudentById("NOPE"));
    }

    @Test
    @DisplayName("updateStudent() replaces the stored student with the same ID")
    void updateStudentTest() {
        StudentRepositoryImpl repository = new StudentRepositoryImpl();
        String existingId = repository.getAllStudents().get(0).getStudentId();

        Student updated = new RegularStudent(existingId, "Renamed Student", 20, "renamed@school.edu",
                "1234567890", StudentStatus.ACTIVE);
        repository.updateStudent(updated);

        assertEquals("Renamed Student", repository.findStudentById(existingId).getName());
    }

    @Test
    @DisplayName("updateStudent() throws for an unknown ID")
    void updateStudentMissingThrowsTest() {
        StudentRepositoryImpl repository = new StudentRepositoryImpl();
        Student ghost = new RegularStudent("NOPE", "Ghost", 20, "ghost@school.edu", "1234567890", StudentStatus.ACTIVE);
        assertThrows(StudentNotFoundException.class, () -> repository.updateStudent(ghost));
    }

    @Test
    @DisplayName("deleteStudent() removes the student and decreases the count")
    void deleteStudentTest() {
        StudentRepositoryImpl repository = new StudentRepositoryImpl();
        int before = repository.getAllStudents().size();
        String existingId = repository.getAllStudents().get(0).getStudentId();

        repository.deleteStudent(existingId);

        assertEquals(before - 1, repository.getAllStudents().size());
        assertThrows(StudentNotFoundException.class, () -> repository.findStudentById(existingId));
    }

    @Test
    @DisplayName("deleteStudent() throws for an unknown ID")
    void deleteStudentMissingThrowsTest() {
        StudentRepositoryImpl repository = new StudentRepositoryImpl();
        assertThrows(StudentNotFoundException.class, () -> repository.deleteStudent("NOPE"));
    }
}
