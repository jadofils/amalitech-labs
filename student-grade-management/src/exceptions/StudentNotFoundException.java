package exceptions;

import java.util.List;

public class StudentNotFoundException extends RuntimeException {
    private final String studentId;
    private final List<String> availableIds;

    public StudentNotFoundException(String message, String studentId, List<String> availableIds) {
        super(message);
        this.studentId = studentId;
        this.availableIds = availableIds;
    }

    public StudentNotFoundException(String message) {
        super(message);
        this.studentId = null;
        this.availableIds = null;
    }

    public String getStudentId() { return studentId; }
    public List<String> getAvailableIds() { return availableIds; }
}
