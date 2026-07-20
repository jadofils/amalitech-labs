package calculators;

import interfaces.Calculable;
import manager.GradeManager;
import manager.StudentManager;
import model.grade.Grade;
import model.student.Student;

import java.util.List;

/**
 * Converts percentage grades to a 4.0 GPA scale per the table in
 * {@code ReadMe-v2.md} ("Calculate Student GPA"), and derives a student's
 * cumulative GPA and class rank from that scale.
 *
 * <p>{@link #percentageToGPA} and {@link #gpaToLetter} are both table-driven
 * over the same parallel arrays, so a percentage and its corresponding
 * letter always come from the same row - the two can never disagree the way
 * two independently-written if-chains could (and, before this fix, did:
 * see CHANGELOG.md KI-1).
 */
public class GPACalculator implements Calculable {

    // Row i of these three arrays is one row of the ReadMe-v2.md grading
    // table: a grade >= PERCENTAGE_THRESHOLDS[i] earns GPA_POINTS[i] and
    // letter LETTER_GRADES[i]. Ordered highest to lowest; the first match
    // wins.
    private static final double[] PERCENTAGE_THRESHOLDS = {93, 90, 87, 83, 80, 77, 73, 70, 67, 60};
    private static final double[] GPA_POINTS            = {4.0, 3.7, 3.3, 3.0, 2.7, 2.3, 2.0, 1.7, 1.3, 1.0};
    private static final String[] LETTER_GRADES         = {"A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D"};
    private static final String BELOW_MINIMUM_LETTER = "F";
    private static final double BELOW_MINIMUM_GPA = 0.0;

    private final GradeManager gradeManager;
    private final StudentManager studentManager;

    public GPACalculator(GradeManager gradeManager, StudentManager studentManager) {
        this.gradeManager = gradeManager;
        this.studentManager = studentManager;
    }

    @Override
    public double percentageToGPA(double percentage) {
        for (int i = 0; i < PERCENTAGE_THRESHOLDS.length; i++) {
            if (percentage >= PERCENTAGE_THRESHOLDS[i]) {
                return GPA_POINTS[i];
            }
        }
        return BELOW_MINIMUM_GPA;
    }

    @Override
    public String gpaToLetter(double gpa) {
        for (int i = 0; i < GPA_POINTS.length; i++) {
            if (gpa >= GPA_POINTS[i]) {
                return LETTER_GRADES[i];
            }
        }
        return BELOW_MINIMUM_LETTER;
    }

    @Override
    public double cumulativeGPA(String studentId) {
        List<Grade> grades = gradeManager.getGradesForStudent(studentId);
        if (grades.isEmpty()) {
            return 0.0;
        }
        double total = 0;
        for (Grade grade : grades) {
            total += percentageToGPA(grade.getGrade());
        }
        return total / grades.size();
    }

    @Override
    public int classRank(String studentId) {
        Student student = studentManager.findStudent(studentId);
        double studentAverage = student == null ? 0.0 : student.calculateAverageGrade();

        int rank = 1;
        for (Student other : studentManager.getAllStudents()) {
            if (other.calculateAverageGrade() > studentAverage) {
                rank++;
            }
        }
        return rank;
    }
}
