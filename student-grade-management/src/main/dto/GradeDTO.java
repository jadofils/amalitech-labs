package main.dto;

import main.model.enums.SubjectType;

/**
 * Read-only projection of a {@link main.model.grade.Grade} for the exported
 * detailed report's grade-history table.
 *
 * <p>A {@code record} (pure data, no behavior of its own) - the JavaBean-style
 * {@code getX()} accessors below sit alongside the record's own
 * {@code gradeId()}/{@code date()}/etc. purely so every existing caller
 * (GradeMapper, ReportGenerator, tests) keeps compiling unchanged.
 */
public record GradeDTO(String gradeId, String date, String subjectName, SubjectType subjectType, double grade) {

    public String getGradeId() { return gradeId; }
    public String getDate() { return date; }
    public String getSubjectName() { return subjectName; }
    public SubjectType getSubjectType() { return subjectType; }
    public double getGrade() { return grade; }
}
