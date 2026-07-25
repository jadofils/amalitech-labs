package tests.validation;

import main.utils.validators.ValidationPatterns;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ValidationPatternsTest {

    @ParameterizedTest
    @ValueSource(strings = {"STU001", "STU999", "STU1000", "STU12345"})
    @DisplayName("STUDENT_ID accepts STU + 3 or more digits, unbounded above 999")
    void studentIdAcceptsValidTest(String id) {
        assertTrue(ValidationPatterns.STUDENT_ID.matcher(id).matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "STU", "STU01", "stu001", "STUD001", "001STU", "STU-001"})
    @DisplayName("STUDENT_ID rejects anything not exactly STU + 3-or-more digits")
    void studentIdRejectsInvalidTest(String id) {
        assertFalse(ValidationPatterns.STUDENT_ID.matcher(id).matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {"GRD001", "GRD999", "GRD1000"})
    @DisplayName("GRADE_ID accepts GRD + 3 or more digits, unbounded above 999")
    void gradeIdAcceptsValidTest(String id) {
        assertTrue(ValidationPatterns.GRADE_ID.matcher(id).matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "GRD", "GRD01", "grd001", "GRAD001"})
    @DisplayName("GRADE_ID rejects anything not exactly GRD + 3-or-more digits")
    void gradeIdRejectsInvalidTest(String id) {
        assertFalse(ValidationPatterns.GRADE_ID.matcher(id).matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567890", "0987654321"})
    @DisplayName("PHONE_LOCAL accepts exactly 10 digits")
    void phoneLocalAcceptsValidTest(String phone) {
        assertTrue(ValidationPatterns.PHONE_LOCAL.matcher(phone).matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {"+1-555-0101", "+44-207-1234"})
    @DisplayName("PHONE_INTERNATIONAL accepts +<country>-###-####")
    void phoneInternationalAcceptsValidTest(String phone) {
        assertTrue(ValidationPatterns.PHONE_INTERNATIONAL.matcher(phone).matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {"123-456-7890", "1-555-0101", "+1 555 0101"})
    @DisplayName("PHONE_INTERNATIONAL rejects a dashed number missing the leading +")
    void phoneInternationalRejectsInvalidTest(String phone) {
        assertFalse(ValidationPatterns.PHONE_INTERNATIONAL.matcher(phone).matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-01-01", "1999-12-31"})
    @DisplayName("DATE_ISO accepts YYYY-MM-DD")
    void dateIsoAcceptsValidTest(String date) {
        assertTrue(ValidationPatterns.DATE_ISO.matcher(date).matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {"01-01-2026", "2026/01/01", "26-01-01"})
    @DisplayName("DATE_ISO rejects non-ISO shapes, including the app's own dd-MM-yyyy display format")
    void dateIsoRejectsInvalidTest(String date) {
        assertFalse(ValidationPatterns.DATE_ISO.matcher(date).matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {"MATH01", "ENG101", "AB12"})
    @DisplayName("COURSE_CODE accepts 2+ uppercase letters followed by 2+ digits")
    void courseCodeAcceptsValidTest(String code) {
        assertTrue(ValidationPatterns.COURSE_CODE.matcher(code).matches());
    }

    @ParameterizedTest
    @CsvSource({"'', empty", "math01, lowercase", "'M1', too few digits"})
    @DisplayName("COURSE_CODE rejects blank, lowercase, or under-length input")
    void courseCodeRejectsInvalidTest(String code, String reason) {
        assertFalse(ValidationPatterns.COURSE_CODE.matcher(code).matches(), reason);
    }
}
