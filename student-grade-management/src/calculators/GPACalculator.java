package calculators;

import interfaces.Calculable;
import model.grade.Grade;

import java.util.List;

public class GPACalculator implements Calculable {

    @Override
    public double percentageToGPA(double percentage) {
        if (percentage >= 93) return 4.0;
        if (percentage >= 90) return 3.7;
        if (percentage >= 87) return 3.3;
        if (percentage >= 83) return 3.0;
        if (percentage >= 80) return 2.7;
        if (percentage >= 77) return 2.3;
        if (percentage >= 73) return 2.0;
        if (percentage >= 70) return 1.7;
        if (percentage >= 67) return 1.3;
        if (percentage >= 60) return 1.0;
        return 0.0;
    }

    @Override
    public String gpaToLetter(double gpa) {
        if (gpa >= 3.7) return "A";
        if (gpa >= 3.3) return "A-";
        if (gpa >= 3.0) return "B+";
        if (gpa >= 2.7) return "B";
        if (gpa >= 2.3) return "B-";
        if (gpa >= 2.0) return "C+";
        if (gpa >= 1.7) return "C";
        if (gpa >= 1.3) return "C-";
        if (gpa >= 1.0) return "D+";
        return "F";
    }

    @Override
    public double cumulativeGPA(List<Grade> grades) {
        if (grades.isEmpty()) return 0.0;
        double totalGPA = 0;
        for (Grade grade : grades) {
            totalGPA += percentageToGPA(grade.getGrade());
        }
        return totalGPA / grades.size();
    }

    @Override
    public int classRank(String studentId, double studentAverage, List<Double> classAverages) {
        int rank = 1;
        for (double avg : classAverages) {
            if (avg > studentAverage) {
                rank++;
            }
        }
        return rank;
    }

    public double calculateGPAFromPercentage(double percentage) {
        return percentageToGPA(percentage);
    }
}
