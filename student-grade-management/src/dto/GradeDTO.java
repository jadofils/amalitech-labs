package dto;

import model.enums.SubjectType;

/**
 * Read-only projection of a {@link model.grade.Grade} for the exported
 * detailed report's grade-history table.
 */
public final class GradeDTO {
    private final String gradeId;
    private final String date;
    private final String subjectName;
    private final SubjectType subjectType;
    private final double grade;

    public GradeDTO(String gradeId, String date, String subjectName, SubjectType subjectType, double grade) {
        this.gradeId = gradeId;
        this.date = date;
        this.subjectName = subjectName;
        this.subjectType = subjectType;
        this.grade = grade;
    }

    public String getGradeId() { return gradeId; }
    public String getDate() { return date; }
    public String getSubjectName() { return subjectName; }
    public SubjectType getSubjectType() { return subjectType; }
    public double getGrade() { return grade; }
}
