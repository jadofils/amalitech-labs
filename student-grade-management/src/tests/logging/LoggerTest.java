package tests.logging;

import main.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

// Logger has no collaborators to mock (it's a static utility over System.err),
// so there's no LoggerMockitoTest - see tests/README.md.
class LoggerTest {

    private final Logger.Level originalLevel = Logger.getLevel();

    @AfterEach
    void restoreLevel() {
        Logger.setLevel(originalLevel);
    }

    @Test
    @DisplayName("setLevel()/getLevel() round-trip")
    void setLevelRoundTripTest() {
        Logger.setLevel(Logger.Level.WARN);
        assertEquals(Logger.Level.WARN, Logger.getLevel());
    }

    @Test
    @DisplayName("Messages below the threshold are suppressed")
    void belowThresholdSuppressedTest() {
        Logger.setLevel(Logger.Level.ERROR);
        String output = captureStdErr(() -> {
            Logger.debug("debug message");
            Logger.info("info message");
            Logger.warn("warn message");
        });
        assertTrue(output.isEmpty());
    }

    @Test
    @DisplayName("Messages at or above the threshold are printed")
    void atOrAboveThresholdPrintedTest() {
        Logger.setLevel(Logger.Level.WARN);
        String output = captureStdErr(() -> {
            Logger.warn("warn message");
            Logger.error("error message");
        });
        assertTrue(output.contains("WARN"));
        assertTrue(output.contains("warn message"));
        assertTrue(output.contains("ERROR"));
        assertTrue(output.contains("error message"));
    }

    @Test
    @DisplayName("DEBUG threshold lets debug messages through")
    void debugThresholdLetsDebugThroughTest() {
        Logger.setLevel(Logger.Level.DEBUG);
        String output = captureStdErr(() -> Logger.debug("tracing detail"));
        assertTrue(output.contains("DEBUG"));
        assertTrue(output.contains("tracing detail"));
    }

    @Test
    @DisplayName("error(message, Throwable) includes the exception type and message")
    void errorWithThrowableIncludesDetailsTest() {
        Logger.setLevel(Logger.Level.ERROR);
        RuntimeException cause = new IllegalStateException("boom");
        String output = captureStdErr(() -> Logger.error("operation failed", cause));
        assertTrue(output.contains("operation failed"));
        assertTrue(output.contains("IllegalStateException"));
        assertTrue(output.contains("boom"));
    }

    private String captureStdErr(Runnable action) {
        PrintStream original = System.err;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setErr(new PrintStream(buffer));
        try {
            action.run();
        } finally {
            System.setErr(original);
        }
        return buffer.toString();
    }
}
