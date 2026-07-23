package tests.utils;

import org.junit.jupiter.api.Test;
import main.utils.DateFormats;

import static org.junit.jupiter.api.Assertions.assertTrue;

// Pure static utility, no collaborators - no DateFormatsMockitoTest (see tests/README.md).
class DateFormatsTest {

    @Test
    void displayDateMatchesDdMmYyyyTest() {
        assertTrue(DateFormats.now(DateFormats.DISPLAY_DATE).matches("\\d{2}-\\d{2}-\\d{4}"));
    }

    @Test
    void displayDateShortTimeMatchesPatternTest() {
        assertTrue(DateFormats.now(DateFormats.DISPLAY_DATE_SHORT_TIME).matches("\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}"));
    }

    @Test
    void displayDateTimeMatchesPatternTest() {
        assertTrue(DateFormats.now(DateFormats.DISPLAY_DATE_TIME).matches("\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void logTimestampMatchesPatternTest() {
        assertTrue(DateFormats.now(DateFormats.LOG_TIMESTAMP).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void fileSafeTimestampMatchesPatternTest() {
        assertTrue(DateFormats.now(DateFormats.FILE_SAFE_TIMESTAMP).matches("\\d{8}_\\d{6}"));
    }
}
