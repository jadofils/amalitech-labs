package interfaces;

/**
 * GPA-related calculations. Resolves everything by student ID - the caller
 * (e.g. {@code Main}) never has to gather grade lists or class averages
 * itself; the implementation encapsulates whatever data access it needs
 * (see {@code docs/v2-sprint-1-plan.md}'s original PBI-3 breakdown).
 */
public interface Calculable {

    /** Converts a single 0-100 percentage grade to a 4.0-scale GPA point value. */
    double percentageToGPA(double percentage);

    /** Converts a 4.0-scale GPA value to its letter grade (A, A-, B+, ...). */
    String gpaToLetter(double gpa);

    /** The average GPA (4.0 scale) across every grade recorded for this student. */
    double cumulativeGPA(String studentId);

    /** This student's rank (1 = highest) by overall average among all students. */
    int classRank(String studentId);
}
