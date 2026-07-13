package interfaces;

import model.grade.Grade;

import java.util.List;

public interface Calculable {
    double percentageToGPA(double percentage);
    String gpaToLetter(double gpa);
    double cumulativeGPA(List<Grade> grades);
    int classRank(String studentId, double studentAverage, List<Double> classAverages);
}
