package logging;

import java.text.SimpleDateFormat;
import java.util.Date;

// Minimal dependency-free logger - this project has no Maven/Gradle to pull in
// a real logging framework (see docs/PROJECT_GUIDE.md), so this is a small
// static utility instead. Writes to System.err, separate from the menu's
// System.out UI output, so redirecting/piping one doesn't capture the other.
public final class Logger {

    public enum Level { DEBUG, INFO, WARN, ERROR }

    private static volatile Level threshold = resolveThreshold();

    private Logger() {
    }

    // Reads -Dlog.level=DEBUG|INFO|WARN|ERROR at startup; defaults to INFO
    // (so DEBUG-level tracing is opt-in, not on by default).
    private static Level resolveThreshold() {
        String configured = System.getProperty("log.level");
        if (configured == null) {
            return Level.INFO;
        }
        try {
            return Level.valueOf(configured.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Level.INFO;
        }
    }

    // Lets callers (or tests) change the threshold at runtime without
    // restarting the JVM with a different system property.
    public static void setLevel(Level level) {
        threshold = level;
    }

    public static Level getLevel() {
        return threshold;
    }

    public static void debug(String message) {
        log(Level.DEBUG, message);
    }

    public static void info(String message) {
        log(Level.INFO, message);
    }

    public static void warn(String message) {
        log(Level.WARN, message);
    }

    public static void error(String message) {
        log(Level.ERROR, message);
    }

    public static void error(String message, Throwable cause) {
        log(Level.ERROR, message + " - " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
    }

    private static void log(Level level, String message) {
        if (level.ordinal() < threshold.ordinal()) {
            return;
        }
        System.err.printf("[%s] %-5s %s%n", timestamp(), level, message);
    }

    private static String timestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
