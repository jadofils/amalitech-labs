package exceptions;

public class ImportException extends RuntimeException {
    private final int successfulRows;
    private final int failedRows;

    public ImportException(String message, int successfulRows, int failedRows, Throwable cause) {
        super(message, cause);
        this.successfulRows = successfulRows;
        this.failedRows = failedRows;
    }

    public ImportException(String message) {
        super(message);
        this.successfulRows = 0;
        this.failedRows = 0;
    }

    public int getSuccessfulRows() { return successfulRows; }
    public int getFailedRows() { return failedRows; }
}
