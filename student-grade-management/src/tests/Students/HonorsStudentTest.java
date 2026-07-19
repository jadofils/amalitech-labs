package tests.Students;

import model.enums.StudentStatus;
import model.student.HonorsStudent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class HonorsStudentTest {

    @Test
    @DisplayName("Student type is HONORS")
    void getStudentTypeTest() {
        HonorsStudent student = new HonorsStudent("Test Student", 10, "", "1234567890");
        assertEquals("HONORS", student.getStudentType());
    }

    @Test
    @DisplayName("Passing grade for an honors student is 60")
    void getPassingGradeHonorsTest() {
        HonorsStudent student = new HonorsStudent("Test Student", 10, "", "1234567890");
        assertEquals(60.0, student.getPassingGrade());
    }

    @Test
    @DisplayName("Default student status is ACTIVE")
    void defaultStudentStatusTest() {
        HonorsStudent student = new HonorsStudent("Test Student", 10, "", "1234567890");
        assertEquals(StudentStatus.ACTIVE, student.getStatus());
    }

    // NOTE: checkHonorsEligibility() as currently implemented compares against
    // StudentType.HONORS.getPassingGrade() (60.0) - the SAME threshold as
    // isPassing(). This is documented as a discrepancy against ReadMe.md's
    // stated 85% rule in ai-prompt-engineering-demo/lab-2-technical-documentation.
    // These tests assert what the code actually does, not what the docs claim.
    @ParameterizedTest
    @CsvSource({
            "59.9, false",
            "60.0, true",
            "60.1, true",
            "100.0, true"
    })
    @DisplayName("Honors eligibility follows the current 60% code threshold")
    void checkHonorsEligibilityThresholdTest(double grade, boolean expectedEligible) {
        HonorsStudent student = new HonorsStudent("Test Student", 10, "", "1234567890");
        student.setGrade((int) grade);
        assertEquals(expectedEligible, student.checkHonorsEligibility());
    }

    @Test
    @DisplayName("Honors eligibility currently matches passing status exactly")
    void honorsEligibilityMatchesPassingStatus() {
        HonorsStudent student = new HonorsStudent("Test Student", 10, "", "1234567890");
        student.setGrade(72);
        assertEquals(student.isPassing(), student.checkHonorsEligibility());
    }

    @Test
    @DisplayName("Honors eligibility is recomputed, not cached, when grades change")
    void honorsEligibilityRecomputesOnNewGrades() {
        HonorsStudent student = new HonorsStudent("Test Student", 10, "", "1234567890");
        student.setGrade(40);
        assertFalse(student.checkHonorsEligibility());

        student.setGrade(100); // average now (40+100)/2 = 70 >= 60
        assertTrue(student.checkHonorsEligibility());
    }

    @Test
    @DisplayName("Displaying student details does not throw")
    void displayStudentDetailsTest() {
        HonorsStudent student = new HonorsStudent("Test Student", 10, "", "1234567890");
        assertDoesNotThrow(student::displayStudentDetails);
    }
}
