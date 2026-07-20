package exceptions;

public class ImportException extends ApplicationException {
    private final String filePath;
    private final int successfulRows;
    private final int failedRows;

    public ImportException(String message, String filePath, Throwable cause) {
        super(message, cause);
        this.filePath = filePath;
        this.successfulRows = 0;
        this.failedRows = 0;
    }

    public ImportException(String message, int successfulRows, int failedRows, Throwable cause) {
        super(message, cause);
        this.filePath = null;
        this.successfulRows = successfulRows;
        this.failedRows = failedRows;
    }

    public ImportException(String message) {
        super(message);
        this.filePath = null;
        this.successfulRows = 0;
        this.failedRows = 0;
    }

    public String getFilePath() { return filePath; }
    public int getSuccessfulRows() { return successfulRows; }
    public int getFailedRows() { return failedRows; }
}
