package exceptions;

/**
 * Common parent for every exception this application throws deliberately,
 * as opposed to an unanticipated runtime failure (NullPointerException and
 * the like). Lets {@code Main} catch one type - {@code ApplicationException}
 * - as its final, catch-all handler for expected error conditions instead of
 * a generic {@code catch (Exception e)}, while each subtype still carries
 * whatever specific recovery data (a student ID, a file path, ...) its
 * scenario needs.
 */
public abstract class ApplicationException extends RuntimeException {

    protected ApplicationException(String message) {
        super(message);
    }

    protected ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
