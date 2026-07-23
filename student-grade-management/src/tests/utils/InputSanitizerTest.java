package tests.utils;

import org.junit.jupiter.api.Test;
import main.utils.InputSanitizer;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Pure static utility, no collaborators - no InputSanitizerMockitoTest (see tests/README.md).
class InputSanitizerTest {

    @Test
    void trimsSurroundingWhitespaceTest() {
        assertEquals("STU001", InputSanitizer.sanitize("  STU001  "));
    }

    @Test
    void stripsControlCharactersTest() {
        String withTab = "STU" + (char) 9 + "001";
        assertEquals("STU001", InputSanitizer.sanitize(withTab));
    }

    @Test
    void nullBecomesEmptyStringTest() {
        assertEquals("", InputSanitizer.sanitize(null));
    }

    @Test
    void leavesOrdinaryTextUnchangedTest() {
        assertEquals("Jane Doe", InputSanitizer.sanitize("Jane Doe"));
    }
}
