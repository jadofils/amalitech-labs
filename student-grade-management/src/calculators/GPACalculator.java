package calculators;

import manager.GradeManager;
import manager.StudentManager;
import model.enums.GpaLetterGrade;
import model.grade.Grade;
import model.student.Student;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Converts percentage grades to a 4.0 GPA scale per the table in
 * {@code ReadMe-v2.md} ("Calculate Student GPA"), and derives a student's
 * cumulative GPA and class rank from that scale.
 *
 * <p>Both conversions delegate to {@link GpaLetterGrade}, which is the one
 * source of truth for the grading table - see CHANGELOG.md KI-1 for the bug
 * that motivated this (two independently hand-written if-chains for
 * percentage-to-GPA and GPA-to-letter quietly disagreed with each other).
 */
public class GPACalculator implements Calculable {

    private final GradeManager gradeManager;
    private final StudentManager studentManager;

    public GPACalculator(GradeManager gradeManager, StudentManager studentManager) {
        this.gradeManager = gradeManager;
        this.studentManager = studentManager;
    }

    @Override
    public double percentageToGPA(double percentage) {
        return GpaLetterGrade.fromPercentage(percentage).getGpaPoints();
    }

    @Override
    public String gpaToLetter(double gpa) {
        return GpaLetterGrade.fromGpaPoints(gpa).getLabel();
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

    /**
     * v3: every enrolled student grouped by cumulative GPA, highest first - built with a
     * {@link TreeMap} keyed on GPA (descending, via {@link Comparator#reverseOrder()}) so each
     * student is an O(log n) insert, and students sharing an identical GPA land in the same
     * bucket instead of an arbitrary tie-break order. Distinct from {@link #classRank(String)},
     * which answers "where does this one student rank" in O(n); this answers "show me the whole
     * class ranked" in one pass, for a future ranking report/dashboard view.
     */
    public NavigableMap<Double, List<Student>> classRankings() {
        NavigableMap<Double, List<Student>> ranked = new TreeMap<>(Comparator.reverseOrder());
        for (Student student : studentManager.getAllStudents()) {
            double gpa = cumulativeGPA(student.getStudentId());
            ranked.computeIfAbsent(gpa, key -> new ArrayList<>()).add(student);
        }
        return ranked;
    }
}
