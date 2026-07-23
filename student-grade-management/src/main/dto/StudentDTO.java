package main.dto;

/**
 * Read-only projection of a {@link main.model.student.Student} for display
 * paths (Search Students results, exported reports) that only ever need
 * these four fields - callers here don't need the full domain object
 * (grade history, status, contact details) just to print a results row.
 */
public final class StudentDTO {
    private final String studentId;
    private final String name;
    private final String studentType;
    private final double averageGrade;

    public StudentDTO(String studentId, String name, String studentType, double averageGrade) {
        this.studentId = studentId;
        this.name = name;
        this.studentType = studentType;
        this.averageGrade = averageGrade;
    }

    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getStudentType() { return studentType; }
    public double getAverageGrade() { return averageGrade; }
}
