package exceptions;

/**
 * Thrown when a CSV file cannot be read at all - as opposed to an individual
 * row being invalid, which {@code imports.CSVParser} collects as a per-row
 * error message instead of throwing. This is the class
 * {@code docs/v2-backlog.md}'s PBI-4 names explicitly; the parser previously
 * reused {@link InvalidFileFormatException} for this instead.
 */
public class CSVImportException extends ApplicationException {

    public CSVImportException(String message) {
        super(message);
    }

    public CSVImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
