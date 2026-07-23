package tests.student;

import main.model.enums.StudentStatus;
import main.model.student.RegularStudent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class RegularStudentTest {

    @Test
    @DisplayName("Student type is REGULAR")
    void getStudentTypeTest() {
        RegularStudent student = new RegularStudent("Test Student", 10, "", "1234567890");
        assertEquals("REGULAR", student.getStudentType());
    }

    @Test
    @DisplayName("Default student status is ACTIVE")
    void defaultStudentStatusTest() {
        RegularStudent student = new RegularStudent("Test Student", 10, "", "1234567890");
        assertEquals(StudentStatus.ACTIVE, student.getStatus());
    }

    @Test
    @DisplayName("Student status can be changed")
    void changeStudentStatusTest() {
        RegularStudent student = new RegularStudent("eric", 12, "eric@gmail.com", "1234567890");
        student.setStatus(StudentStatus.INACTIVE);
        assertEquals(StudentStatus.INACTIVE, student.getStatus());
    }

    @Test
    @DisplayName("Passing grade for a regular student is 50")
    void getPassingGradeRegularTest() {
        RegularStudent student = new RegularStudent("Test Student", 10, "", "1234567890");
        assertEquals(50.0, student.getPassingGrade());
    }

    @Test
    @DisplayName("Average grade is 0 when no grades recorded")
    void averageGradeWithNoGradesTest() {
        RegularStudent student = new RegularStudent("Test Student", 10, "", "1234567890");
        assertEquals(0.0, student.calculateAverageGrade());
    }

    @Test
    @DisplayName("Average grade is the arithmetic mean of recorded grades")
    void averageGradeTest() {
        RegularStudent student = new RegularStudent("Test Student", 10, "", "1234567890");
        student.addGrade(80.0);
        student.addGrade(90.0);
        student.addGrade(70.0);
        assertEquals(80.0, student.calculateAverageGrade(), 0.0001);
    }

    @Test
    @DisplayName("Regular student passes at exactly 50%")
    void isPassingAtThresholdTest() {
        RegularStudent student = new RegularStudent("Test Student", 10, "", "1234567890");
        student.addGrade(50.0);
        assertTrue(student.isPassing());
    }

    @Test
    @DisplayName("Regular student fails below 50%")
    void isFailingBelowThresholdTest() {
        RegularStudent student = new RegularStudent("Test Student", 10, "", "1234567890");
        student.addGrade(49.9);
        assertFalse(student.isPassing());
    }

    @Test
    @DisplayName("Displaying student details does not throw")
    void displayStudentDetailsTest() {
        RegularStudent student = new RegularStudent("Test Student", 10, "", "1234567890");
        assertDoesNotThrow(student::displayStudentDetails);
    }

    @Test
    @DisplayName("Each new student receives a distinct, format-correct ID")
    void studentIdIsUniqueAndFormattedTest() {
        // studentCounter is static on Student, so it is shared with every other
        // test in this run - assert format and uniqueness, never an exact value.
        RegularStudent first = new RegularStudent("Test Student", 10, "", "1234567890");
        RegularStudent second = new RegularStudent("Test Student", 10, "", "1234567890");
        assertTrue(first.getStudentId().matches("^STU\\d{3,}$"));
        assertTrue(second.getStudentId().matches("^STU\\d{3,}$"));
        assertNotEquals(first.getStudentId(), second.getStudentId());
    }
}
