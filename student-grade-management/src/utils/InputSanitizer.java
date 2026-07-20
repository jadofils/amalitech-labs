package utils;

import java.util.regex.Pattern;

/**
 * Cleans raw console input before it reaches domain validation. Trims
 * surrounding whitespace and strips control characters a user could paste
 * in accidentally (e.g. from a copied CSV cell) - {@code validation}
 * package classes then validate the *content* of an already-clean string.
 */
public final class InputSanitizer {

    private static final Pattern CONTROL_CHARS = Pattern.compile("\\p{Cntrl}");

    private InputSanitizer() {
    }

    public static String sanitize(String raw) {
        if (raw == null) {
            return "";
        }
        return CONTROL_CHARS.matcher(raw.trim()).replaceAll("");
    }
}
