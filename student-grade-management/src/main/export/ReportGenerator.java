package main.export;

import main.dto.GradeDTO;
import main.logging.Logger;
import main.manager.GradeManager;
import main.manager.StudentManager;
import main.mapper.GradeMapper;
import main.model.grade.Grade;
import main.model.student.Student;
import main.model.subject.Subject;
import main.utils.DateFormats;

import java.util.Comparator;
import java.util.List;

/**
 * Builds the text content for a student's exported grade report. Resolves
 * the student's own name via {@link StudentManager} internally, so callers
 * only ever need a student ID (matching the {@link Exportable} contract) -
 * see CHANGELOG.md KI-2 for why that matters: the previous version never
 * had a {@code Student} to read a name from at all, and hardcoded the
 * literal string "[name]" instead.
 */
public class ReportGenerator implements Exportable {

    private static final double EXCELLENT_THRESHOLD = 80.0;
    private static final double GOOD_THRESHOLD = 60.0;
    private static final String PERCENT_ONE_DECIMAL = "%.1f%%";
    private static final String SECTION_DIVIDER = "------------------------------------------------\n";
    private static final String HEADER_DIVIDER = "================================\n\n";
    private static final String FOOTER_DIVIDER = "================================\n";
    private static final String GENERATED_ON_LABEL = "Generated on: ";

    private final GradeManager gradeManager;
    private final StudentManager studentManager;

    public ReportGenerator(GradeManager gradeManager, StudentManager studentManager) {
        this.gradeManager = gradeManager;
        this.studentManager = studentManager;
    }

    @Override
    public String exportSummary(String studentId) {
        Logger.debug("Generating summary report for student " + studentId);
        Student student = studentManager.findStudent(studentId);
        double overallAverage = gradeManager.calculateOverallAverage(studentId);

        StringBuilder sb = new StringBuilder();
        sb.append("STUDENT GRADE REPORT - SUMMARY\n");
        sb.append(HEADER_DIVIDER);
        sb.append("Student ID: ").append(studentId).append("\n");
        sb.append("Name: ").append(studentName(student)).append("\n");
        sb.append("Overall Average: ").append(String.format(PERCENT_ONE_DECIMAL, overallAverage)).append("\n\n");
        sb.append(FOOTER_DIVIDER);
        sb.append(GENERATED_ON_LABEL).append(timestamp()).append("\n");
        return sb.toString();
    }

    @Override
    public String exportDetailed(String studentId) {
        Logger.debug("Generating detailed report for student " + studentId);
        Student student = studentManager.findStudent(studentId);

        StringBuilder sb = new StringBuilder();
        sb.append("STUDENT GRADE REPORT - DETAILED\n");
        sb.append(HEADER_DIVIDER);
        sb.append("Student ID: ").append(studentId).append("\n");
        sb.append("Name: ").append(studentName(student)).append("\n\n");

        sb.append("GRADE HISTORY\n");
        sb.append(SECTION_DIVIDER);
        sb.append(String.format("%-8s | %-10s | %-16s | %-9s | %s%n", "GRD ID", "DATE", "SUBJECT", "TYPE", "GRADE"));
        sb.append(SECTION_DIVIDER);

        List<Grade> grades = gradeManager.getGradesForStudent(studentId);
        List<GradeDTO> gradeDtos = grades.stream().map(GradeMapper::toDto).toList();
        for (GradeDTO g : gradeDtos) {
            sb.append(String.format("%-8s | %-10s | %-16s | %-9s | %.1f%%%n",
                    g.getGradeId(), g.getDate(), g.getSubjectName(),
                    g.getSubjectType(), g.getGrade()));
        }

        double overallAverage = gradeManager.calculateOverallAverage(studentId);
        sb.append(SECTION_DIVIDER);
        sb.append("Total Grades: ").append(grades.size()).append("\n");
        sb.append("Core Average: ").append(String.format(PERCENT_ONE_DECIMAL, gradeManager.calculateCoreAverage(studentId))).append("\n");
        sb.append("Elective Average: ").append(String.format(PERCENT_ONE_DECIMAL, gradeManager.calculateElectiveAverage(studentId))).append("\n");
        sb.append("Overall Average: ").append(String.format(PERCENT_ONE_DECIMAL, overallAverage)).append("\n\n");

        // Performance analysis: part of US-2's acceptance criteria
        // ("...performance analysis") but previously only implemented in a
        // generateFullReport() method Main never called - see CHANGELOG.md
        // KI-2. Folded directly into the detailed report instead of keeping
        // a second, unreachable method around.
        sb.append("PERFORMANCE ANALYSIS\n");
        sb.append(SECTION_DIVIDER);
        sb.append(performanceSummary(overallAverage)).append("\n");

        sb.append(FOOTER_DIVIDER);
        sb.append(GENERATED_ON_LABEL).append(timestamp()).append("\n");
        return sb.toString();
    }

    /**
     * A class-wide (per-subject) summary: how many grades were recorded for
     * this subject across every student, and the class average - the
     * subject-level counterpart to {@link #exportSummary(String)}.
     */
    public String exportClassSummary(Subject subject) {
        Logger.debug("Generating class summary report for subject " + subject.getSubjectCode());
        List<Grade> grades = gradesForSubject(subject);

        StringBuilder sb = new StringBuilder();
        sb.append("CLASS GRADE REPORT - SUMMARY\n");
        sb.append(HEADER_DIVIDER);
        sb.append("Class: ").append(subject.getSubjectName()).append(" (").append(subject.getSubjectCode()).append(")\n");
        sb.append("Total Grades Recorded: ").append(grades.size()).append("\n");
        sb.append("Class Average: ").append(String.format(PERCENT_ONE_DECIMAL, average(grades))).append("\n\n");
        sb.append(FOOTER_DIVIDER);
        sb.append(GENERATED_ON_LABEL).append(timestamp()).append("\n");
        return sb.toString();
    }

    /**
     * Every grade recorded for this subject, across every student - the
     * subject-level counterpart to {@link #exportDetailed(String)}.
     */
    public String exportClassDetailed(Subject subject) {
        Logger.debug("Generating class detailed report for subject " + subject.getSubjectCode());
        List<Grade> grades = gradesForSubject(subject);

        StringBuilder sb = new StringBuilder();
        sb.append("CLASS GRADE REPORT - DETAILED\n");
        sb.append(HEADER_DIVIDER);
        sb.append("Class: ").append(subject.getSubjectName()).append(" (").append(subject.getSubjectCode()).append(")\n\n");

        sb.append("GRADE HISTORY\n");
        sb.append(SECTION_DIVIDER);
        sb.append(String.format("%-8s | %-10s | %-14s | %-16s | %s%n", "GRD ID", "STU ID", "STUDENT", "DATE", "GRADE"));
        sb.append(SECTION_DIVIDER);
        for (Grade g : grades) {
            sb.append(String.format("%-8s | %-10s | %-14s | %-16s | %.1f%%%n",
                    g.getGradeId(), g.getStudentId(), studentName(studentManager.findStudent(g.getStudentId())),
                    g.getDate(), g.getGrade()));
        }
        sb.append(SECTION_DIVIDER);
        sb.append("Total Grades: ").append(grades.size()).append("\n");
        sb.append("Class Average: ").append(String.format(PERCENT_ONE_DECIMAL, average(grades))).append("\n");

        if (!grades.isEmpty()) {
            Grade highest = grades.stream().max(Comparator.comparingDouble(Grade::getGrade)).orElseThrow();
            Grade lowest = grades.stream().min(Comparator.comparingDouble(Grade::getGrade)).orElseThrow();
            sb.append(String.format("Highest Grade: %.1f%% (%s - %s)%n", highest.getGrade(), highest.getStudentId(),
                    studentName(studentManager.findStudent(highest.getStudentId()))));
            sb.append(String.format("Lowest Grade: %.1f%% (%s - %s)%n", lowest.getGrade(), lowest.getStudentId(),
                    studentName(studentManager.findStudent(lowest.getStudentId()))));
        }

        sb.append("\n").append(FOOTER_DIVIDER);
        sb.append(GENERATED_ON_LABEL).append(timestamp()).append("\n");
        return sb.toString();
    }

    private List<Grade> gradesForSubject(Subject subject) {
        return gradeManager.getAllGrades().stream()
                .filter(g -> g.getSubject().getSubjectCode().equals(subject.getSubjectCode()))
                .toList();
    }

    private double average(List<Grade> grades) {
        return grades.stream().mapToDouble(Grade::getGrade).average().orElse(0.0);
    }

    private String performanceSummary(double overallAverage) {
        if (overallAverage >= EXCELLENT_THRESHOLD) {
            return "Excellent performance";
        } else if (overallAverage >= GOOD_THRESHOLD) {
            return "Good performance";
        } else {
            return "Needs improvement";
        }
    }

    private String studentName(Student student) {
        return student == null ? "Unknown" : student.getName();
    }

    private String timestamp() {
        return DateFormats.now(DateFormats.DISPLAY_DATE_SHORT_TIME);
    }
}
