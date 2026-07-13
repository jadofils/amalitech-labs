package exceptions;

public class InvalidFileFormatException extends RuntimeException {
    private final String filename;
    private final int lineNumber;

    public InvalidFileFormatException(String message, String filename, int lineNumber) {
        super(message);
        this.filename = filename;
        this.lineNumber = lineNumber;
    }

    public InvalidFileFormatException(String message, String filename) {
        super(message);
        this.filename = filename;
        this.lineNumber = -1;
    }

    public InvalidFileFormatException(String message) {
        super(message);
        this.filename = null;
        this.lineNumber = -1;
    }

    public String getFilename() { return filename; }
    public int getLineNumber() { return lineNumber; }
}
