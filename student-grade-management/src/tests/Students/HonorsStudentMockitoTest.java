package tests.Students;

import model.student.HonorsStudent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Spy-based interaction tests: verifies checkHonorsEligibility() is actually
 * built on top of calculateAverageGrade() rather than some separate,
 * independently-maintained calculation.
 */
class HonorsStudentMockitoTest {

    @Test
    @DisplayName("checkHonorsEligibility() delegates to calculateAverageGrade()")
    void eligibilityDelegatesToAverage() {
        HonorsStudent real = new HonorsStudent("Test Student", 10, "", "1234567890");
        real.setGrade(90);
        HonorsStudent spy = spy(real);

        boolean eligible = spy.checkHonorsEligibility();

        assertTrue(eligible);
        verify(spy, times(1)).calculateAverageGrade();
    }

    @Test
    @DisplayName("checkHonorsEligibility() recomputes on every call rather than caching")
    void eligibilityRecomputesEveryCall() {
        HonorsStudent real = new HonorsStudent("Test Student", 10, "", "1234567890");
        real.setGrade(30);
        HonorsStudent spy = spy(real);

        spy.checkHonorsEligibility();
        spy.checkHonorsEligibility();

        verify(spy, times(2)).calculateAverageGrade();
    }
}
