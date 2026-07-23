package main.dto;

/**
 * Read-only projection of a {@link main.model.student.Student} for display
 * paths (Search Students results, exported reports) that only ever need
 * these four fields - callers here don't need the full domain object
 * (grade history, status, contact details) just to print a results row.
 *
 * <p>A {@code record} (pure data, no behavior of its own) - the JavaBean-style
 * {@code getX()} accessors below sit alongside the record's own
 * {@code studentId()}/{@code name()}/etc. purely so every existing caller
 * (StudentMapper, console/*Action classes, tests) keeps compiling unchanged.
 */
public record StudentDTO(String studentId, String name, String studentType, double averageGrade) {

    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getStudentType() { return studentType; }
    public double getAverageGrade() { return averageGrade; }
}
