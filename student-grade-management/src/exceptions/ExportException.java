package exceptions;

public class ExportException extends RuntimeException {
    private final String filePath;

    public ExportException(String message, String filePath, Throwable cause) {
        super(message, cause);
        this.filePath = filePath;
    }

    public ExportException(String message) {
        super(message);
        this.filePath = null;
    }

    public String getFilePath() { return filePath; }
}
