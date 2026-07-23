package tests.student;

import main.model.student.RegularStudent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Uses a Mockito spy on a real RegularStudent to verify *interactions* -
 * i.e. that isPassing() is actually implemented in terms of
 * calculateAverageGrade()/getPassingGrade() rather than duplicating the logic.
 * The plain RegularStudentTest checks outcomes; this checks how they're produced.
 */
class RegularStudentMockitoTest {

    @Test
    @DisplayName("isPassing() delegates to calculateAverageGrade() and getPassingGrade()")
    void isPassingDelegatesToCollaboratingMethods() {
        RegularStudent real = new RegularStudent("Test Student", 10, "", "1234567890");
        real.addGrade(60.0);
        RegularStudent spy = spy(real);

        boolean result = spy.isPassing();

        assertTrue(result);
        verify(spy, times(1)).calculateAverageGrade();
        verify(spy, times(1)).getPassingGrade();
    }

    @Test
    @DisplayName("isPassing() does not recompute the average more than once")
    void isPassingCallsAverageExactlyOnce() {
        RegularStudent real = new RegularStudent("Test Student", 10, "", "1234567890");
        real.addGrade(10.0);
        RegularStudent spy = spy(real);

        spy.isPassing();

        // A future refactor that accidentally recalculates the average twice
        // (e.g. once for a log statement) would be caught by this test.
        verify(spy, times(1)).calculateAverageGrade();
    }
}
