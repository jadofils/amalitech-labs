package calculators;

import model.grade.Grade;
import model.student.HonorsStudent;
import model.student.Student;
import model.subject.Subject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatisticsCalculator {

    public static class GradeDistribution {
        private final int[] counts;
        private final double[] percentages;
        private static final String[] LABELS = {"90-100% (A)", "80-89%  (B)", "70-79%  (C)", "60-69%  (D)", "0-59%   (F)"};

        public GradeDistribution(int[] counts, int total) {
            this.counts = counts;
            this.percentages = new double[5];
            for (int i = 0; i < 5; i++) {
                this.percentages[i] = total > 0 ? (counts[i] * 100.0 / total) : 0;
            }
        }

        public int[] getCounts() { return counts; }
        public double[] getPercentages() { return percentages; }
        public String[] getLabels() { return LABELS; }
    }

    public static class StatsResult {
        private final double mean;
        private final double median;
        private final double stdDev;
        private final double range;
        private final double min;
        private final double max;
        private final String maxStudentName;
        private final String maxSubjectName;
        private final String minStudentName;
        private final String minSubjectName;

        public StatsResult(double mean, double median, double stdDev, double range,
                           double min, double max, String maxStudentName, String maxSubjectName,
                           String minStudentName, String minSubjectName) {
            this.mean = mean;
            this.median = median;
            this.stdDev = stdDev;
            this.range = range;
            this.min = min;
            this.max = max;
            this.maxStudentName = maxStudentName;
            this.maxSubjectName = maxSubjectName;
            this.minStudentName = minStudentName;
            this.minSubjectName = minSubjectName;
        }

        public double getMean() { return mean; }
        public double getMedian() { return median; }
        public double getStdDev() { return stdDev; }
        public double getRange() { return range; }
        public double getMin() { return min; }
        public double getMax() { return max; }
        public String getMaxStudentName() { return maxStudentName; }
        public String getMaxSubjectName() { return maxSubjectName; }
        public String getMinStudentName() { return minStudentName; }
        public String getMinSubjectName() { return minSubjectName; }
    }

    public static class SubjectAverage {
        private final String subjectName;
        private final double average;

        public SubjectAverage(String subjectName, double average) {
            this.subjectName = subjectName;
            this.average = average;
        }

        public String getSubjectName() { return subjectName; }
        public double getAverage() { return average; }
    }

    public static class StudentTypeComparison {
        private final double regularAverage;
        private final int regularCount;
        private final double honorsAverage;
        private final int honorsCount;

        public StudentTypeComparison(double regularAverage, int regularCount,
                                      double honorsAverage, int honorsCount) {
            this.regularAverage = regularAverage;
            this.regularCount = regularCount;
            this.honorsAverage = honorsAverage;
            this.honorsCount = honorsCount;
        }

        public double getRegularAverage() { return regularAverage; }
        public int getRegularCount() { return regularCount; }
        public double getHonorsAverage() { return honorsAverage; }
        public int getHonorsCount() { return honorsCount; }
    }

    public GradeDistribution calculateDistribution(List<Grade> grades) {
        int[] counts = new int[5];
        for (Grade g : grades) {
            double val = g.getGrade();
            if (val >= 90) counts[0]++;
            else if (val >= 80) counts[1]++;
            else if (val >= 70) counts[2]++;
            else if (val >= 60) counts[3]++;
            else counts[4]++;
        }
        return new GradeDistribution(counts, grades.size());
    }

    public StatsResult calculateStats(List<Grade> grades, List<Student> students) {
        if (grades.isEmpty()) {
            return new StatsResult(0, 0, 0, 0, 0, 0, "", "", "", "");
        }

        double sum = 0;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        String maxStudent = "", maxSubject = "", minStudent = "", minSubject = "";

        for (Grade g : grades) {
            double val = g.getGrade();
            sum += val;
            if (val > max) {
                max = val;
                maxStudent = findStudentName(g.getStudentId(), students);
                maxSubject = g.getSubject().getSubjectName();
            }
            if (val < min) {
                min = val;
                minStudent = findStudentName(g.getStudentId(), students);
                minSubject = g.getSubject().getSubjectName();
            }
        }

        double mean = sum / grades.size();

        List<Double> sortedVals = new ArrayList<>();
        for (Grade g : grades) sortedVals.add(g.getGrade());
        Collections.sort(sortedVals);

        double median;
        int n = sortedVals.size();
        if (n % 2 == 0) {
            median = (sortedVals.get(n / 2 - 1) + sortedVals.get(n / 2)) / 2;
        } else {
            median = sortedVals.get(n / 2);
        }

        double varianceSum = 0;
        for (double v : sortedVals) varianceSum += Math.pow(v - mean, 2);
        double stdDev = Math.sqrt(varianceSum / n);
        double range = max - min;

        return new StatsResult(mean, median, stdDev, range, min, max,
                maxStudent, maxSubject, minStudent, minSubject);
    }

    public List<SubjectAverage> calculateSubjectAverages(List<Grade> allGrades, List<Subject> allSubjects) {
        List<SubjectAverage> result = new ArrayList<>();
        for (Subject subj : allSubjects) {
            double sSum = 0;
            int sCount = 0;
            for (Grade g : allGrades) {
                if (g.getSubject().getSubjectCode().equals(subj.getSubjectCode())) {
                    sSum += g.getGrade();
                    sCount++;
                }
            }
            if (sCount > 0) {
                result.add(new SubjectAverage(subj.getSubjectName(), sSum / sCount));
            }
        }
        return result;
    }

    public StudentTypeComparison compareStudentTypes(List<Student> students) {
        double regSum = 0, honSum = 0;
        int regCount = 0, honCount = 0;
        for (Student s : students) {
            if (s instanceof HonorsStudent) {
                honSum += s.calculateAverageGrade();
                honCount++;
            } else {
                regSum += s.calculateAverageGrade();
                regCount++;
            }
        }
        double regAvg = regCount > 0 ? regSum / regCount : 0;
        double honAvg = honCount > 0 ? honSum / honCount : 0;
        return new StudentTypeComparison(regAvg, regCount, honAvg, honCount);
    }

    private String findStudentName(String studentId, List<Student> students) {
        for (Student s : students) {
            if (s.getStudentId().equals(studentId)) {
                return s.getName();
            }
        }
        return "Unknown";
    }
}
