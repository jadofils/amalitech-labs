package model.enums;

/**
 * Each constant carries its own minimum percentage, so nothing else in the
 * app needs (or should keep) an independent copy of these thresholds -
 * see {@code calculators.StatisticsCalculator}'s distribution labels, which
 * used to hardcode a second, disagreeing scale (CHANGELOG.md KI-5).
 */
public enum LetterGrade {
    A(85),
    B(70),
    C(55),
    D(40),
    F(0);

    private final double minPercentage;

    LetterGrade(double minPercentage) {
        this.minPercentage = minPercentage;
    }

    public double getMinPercentage() {
        return minPercentage;
    }

    public static LetterGrade fromNumeric(double grade) {
        for (LetterGrade letter : values()) {
            if (grade >= letter.minPercentage) {
                return letter;
            }
        }
        return F;
    }
}
