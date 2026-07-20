package calculators;

import model.enums.LetterGrade;
import model.grade.Grade;
import model.student.HonorsStudent;
import model.student.Student;
import model.subject.Subject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class-wide grade statistics: distribution by letter grade, descriptive
 * statistics (mean/median/mode/standard deviation), per-subject averages,
 * and a Regular-vs-Honors comparison.
 */
public class StatisticsCalculator {

    public static class GradeDistribution {
        private final int[] counts;
        private final double[] percentages;
        // Bucket order must match LetterGrade's own declaration order
        // (A, B, C, D, F / ordinal 0-4) - see calculateDistribution(), which
        // buckets by LetterGrade.ordinal() specifically. The labels below are
        // computed directly from LetterGrade.getMinPercentage() rather than
        // a second, independently hardcoded set of numbers, so the two can
        // never disagree again the way they used to (CHANGELOG.md KI-5).
        private static final String[] LABELS = buildLabels();

        private static String[] buildLabels() {
            LetterGrade[] letters = LetterGrade.values();
            String[] labels = new String[letters.length];
            for (int i = 0; i < letters.length; i++) {
                int min = (int) letters[i].getMinPercentage();
                int max = (i == 0) ? 100 : (int) letters[i - 1].getMinPercentage() - 1;
                labels[i] = String.format("%d-%d%% (%s)", min, max, letters[i].name());
            }
            return labels;
        }

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
        private final double mode;
        private final double stdDev;
        private final double range;
        private final double min;
        private final double max;
        private final String maxStudentName;
        private final String maxSubjectName;
        private final String minStudentName;
        private final String minSubjectName;

        public StatsResult(double mean, double median, double mode, double stdDev, double range,
                           double min, double max, String maxStudentName, String maxSubjectName,
                           String minStudentName, String minSubjectName) {
            this.mean = mean;
            this.median = median;
            this.mode = mode;
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
        public double getMode() { return mode; }
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

    /** Buckets every grade by its {@link LetterGrade}, so this can never disagree with LetterGrade elsewhere. */
    public GradeDistribution calculateDistribution(List<Grade> grades) {
        int[] counts = new int[5];
        for (Grade g : grades) {
            counts[LetterGrade.fromNumeric(g.getGrade()).ordinal()]++;
        }
        return new GradeDistribution(counts, grades.size());
    }

    /** Mean, median, mode, standard deviation, range, and the highest/lowest single grade (with who/what earned it). */
    public StatsResult calculateStats(List<Grade> grades, List<Student> students) {
        if (grades.isEmpty()) {
            return new StatsResult(0, 0, 0, 0, 0, 0, 0, "", "", "", "");
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

        double mode = calculateMode(sortedVals);

        double varianceSum = 0;
        for (double v : sortedVals) varianceSum += Math.pow(v - mean, 2);
        double stdDev = Math.sqrt(varianceSum / n);
        double range = max - min;

        return new StatsResult(mean, median, mode, stdDev, range, min, max,
                maxStudent, maxSubject, minStudent, minSubject);
    }

    // Ties broken by lowest value, via TreeMap's ascending iteration order
    // and only replacing the running mode on a strictly higher frequency.
    private double calculateMode(List<Double> values) {
        Map<Double, Integer> frequency = new TreeMap<>();
        for (double v : values) {
            frequency.merge(v, 1, Integer::sum);
        }
        double mode = 0;
        int highestFrequency = 0;
        for (Map.Entry<Double, Integer> entry : frequency.entrySet()) {
            if (entry.getValue() > highestFrequency) {
                highestFrequency = entry.getValue();
                mode = entry.getKey();
            }
        }
        return mode;
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
