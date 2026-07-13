package export;

import interfaces.Exportable;
import manager.GradeManager;
import model.grade.Grade;
import model.student.Student;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ReportGenerator implements Exportable {
    private final GradeManager gradeManager;

    public ReportGenerator(GradeManager gradeManager) {
        this.gradeManager = gradeManager;
    }

    @Override
    public String exportSummary(String studentId) {
        StringBuilder sb = new StringBuilder();
        sb.append("STUDENT GRADE REPORT - SUMMARY\n");
        sb.append("================================\n\n");
        sb.append("Student ID: ").append(studentId).append("\n");
        sb.append("Name: [name]\n");
        sb.append("Overall Average: ").append(String.format("%.1f%%", gradeManager.calculateOverallAverage(studentId))).append("\n\n");
        sb.append("================================\n");
        sb.append("Generated on: ").append(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date())).append("\n");
        return sb.toString();
    }

    @Override
    public String exportDetailed(String studentId) {
        StringBuilder sb = new StringBuilder();
        sb.append("STUDENT GRADE REPORT - DETAILED\n");
        sb.append("================================\n\n");
        sb.append("Student ID: ").append(studentId).append("\n\n");

        sb.append("GRADE HISTORY\n");
        sb.append("------------------------------------------------\n");
        sb.append(String.format("%-8s | %-10s | %-16s | %-9s | %s%n", "GRD ID", "DATE", "SUBJECT", "TYPE", "GRADE"));
        sb.append("------------------------------------------------\n");

        List<Grade> grades = gradeManager.getGradesForStudent(studentId);
        for (Grade g : grades) {
            sb.append(String.format("%-8s | %-10s | %-16s | %-9s | %.1f%%%n",
                    g.getGradeId(), g.getDate(), g.getSubject().getSubjectName(),
                    g.getSubjectType(), g.getGrade()));
        }

        sb.append("------------------------------------------------\n");
        sb.append("Total Grades: ").append(grades.size()).append("\n");
        sb.append("Core Average: ").append(String.format("%.1f%%", gradeManager.calculateCoreAverage(studentId))).append("\n");
        sb.append("Elective Average: ").append(String.format("%.1f%%", gradeManager.calculateElectiveAverage(studentId))).append("\n");
        sb.append("Overall Average: ").append(String.format("%.1f%%", gradeManager.calculateOverallAverage(studentId))).append("\n\n");
        sb.append("================================\n");
        sb.append("Generated on: ").append(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date())).append("\n");
        return sb.toString();
    }

    public String generateFullReport(Student student, String studentId) {
        StringBuilder sb = new StringBuilder();
        sb.append("STUDENT GRADE REPORT - DETAILED\n");
        sb.append("================================\n\n");
        sb.append("Student ID: ").append(studentId).append("\n");
        sb.append("Name: ").append(student.getName()).append("\n");
        sb.append("Type: ").append(student.getStudentType()).append(" Student\n\n");

        sb.append(exportDetailed(studentId));

        sb.append("PERFORMANCE ANALYSIS\n");
        sb.append("------------------------------------------------\n");
        double avg = gradeManager.calculateOverallAverage(studentId);
        if (avg >= 80) {
            sb.append("Excellent performance\n");
        } else if (avg >= 60) {
            sb.append("Good performance\n");
        } else {
            sb.append("Needs improvement\n");
        }
        sb.append("================================\n");

        return sb.toString();
    }
}
