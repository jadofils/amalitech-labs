package model.enums;

/**
 * The 4.0-scale GPA grading table from {@code ReadMe-v2.md}'s "Calculate
 * Student GPA" feature. Each constant carries its percentage threshold, GPA
 * points, and display label together, as fields of the same object - unlike
 * the three separate parallel arrays this replaced, a percentage and its
 * letter can no longer drift apart from each other (see CHANGELOG.md KI-1:
 * that's exactly how the original bug happened - two independently
 * hand-written if-chains that quietly disagreed).
 */
public enum GpaLetterGrade {
    A(93, 4.0, "A"),
    A_MINUS(90, 3.7, "A-"),
    B_PLUS(87, 3.3, "B+"),
    B(83, 3.0, "B"),
    B_MINUS(80, 2.7, "B-"),
    C_PLUS(77, 2.3, "C+"),
    C(73, 2.0, "C"),
    C_MINUS(70, 1.7, "C-"),
    D_PLUS(67, 1.3, "D+"),
    D(60, 1.0, "D"),
    F(0, 0.0, "F");

    private final double minPercentage;
    private final double gpaPoints;
    private final String label;

    GpaLetterGrade(double minPercentage, double gpaPoints, String label) {
        this.minPercentage = minPercentage;
        this.gpaPoints = gpaPoints;
        this.label = label;
    }

    public double getMinPercentage() { return minPercentage; }
    public double getGpaPoints() { return gpaPoints; }
    public String getLabel() { return label; }

    public static GpaLetterGrade fromPercentage(double percentage) {
        for (GpaLetterGrade grade : values()) {
            if (percentage >= grade.minPercentage) {
                return grade;
            }
        }
        return F;
    }

    /** Finds the constant matching a GPA points value exactly (or the highest tier at or below it). */
    public static GpaLetterGrade fromGpaPoints(double gpaPoints) {
        for (GpaLetterGrade grade : values()) {
            if (gpaPoints >= grade.gpaPoints) {
                return grade;
            }
        }
        return F;
    }
}
