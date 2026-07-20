package tests.calculators;

import calculators.StatisticsCalculator;
import model.grade.Grade;
import model.student.HonorsStudent;
import model.student.RegularStudent;
import model.student.Student;
import model.subject.CoreSubject;
import model.subject.ElectiveSubject;
import model.subject.Subject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StatisticsCalculatorTest {

    private final StatisticsCalculator calculator = new StatisticsCalculator();
    private final Subject math = new CoreSubject("Mathematics", "MATH01");
    private final Subject music = new ElectiveSubject("Music", "MUSC01");

    // Regression test for CHANGELOG.md KI-5: the distribution used to bucket
    // by its own hardcoded 90/80/70/60 scale, disagreeing with
    // model.enums.LetterGrade's 85/70/55/40 scale used everywhere else in
    // the app. These boundaries must match LetterGrade.fromNumeric() exactly.
    @ParameterizedTest
    @CsvSource({"85, 0", "100, 0", "84, 1", "70, 1", "69, 2", "55, 2", "54, 3", "40, 3", "39, 4", "0, 4"})
    @DisplayName("calculateDistribution() buckets grades using LetterGrade's own thresholds")
    void distributionMatchesLetterGradeThresholdsTest(double value, int expectedBucket) {
        Grade grade = new Grade("STU001", math, value);

        StatisticsCalculator.GradeDistribution dist = calculator.calculateDistribution(List.of(grade));

        for (int i = 0; i < 5; i++) {
            assertEquals(i == expectedBucket ? 1 : 0, dist.getCounts()[i]);
        }
    }

    @Test
    @DisplayName("calculateStats() computes mean, median (even count), and range correctly")
    void calculateStatsBasicMeasuresTest() {
        List<Grade> grades = List.of(
                new Grade("STU001", math, 60.0),
                new Grade("STU001", math, 70.0),
                new Grade("STU001", math, 80.0),
                new Grade("STU001", math, 90.0)
        );

        StatisticsCalculator.StatsResult stats = calculator.calculateStats(grades, List.of());

        assertEquals(75.0, stats.getMean(), 0.0001);
        assertEquals(75.0, stats.getMedian(), 0.0001); // (70+80)/2
        assertEquals(30.0, stats.getRange(), 0.0001);
        assertEquals(60.0, stats.getMin());
        assertEquals(90.0, stats.getMax());
    }

    @Test
    @DisplayName("calculateStats() computes median correctly for an odd count")
    void calculateStatsOddCountMedianTest() {
        List<Grade> grades = List.of(
                new Grade("STU001", math, 60.0),
                new Grade("STU001", math, 70.0),
                new Grade("STU001", math, 100.0)
        );

        StatisticsCalculator.StatsResult stats = calculator.calculateStats(grades, List.of());

        assertEquals(70.0, stats.getMedian(), 0.0001);
    }

    @Test
    @DisplayName("calculateStats() reports the mode as the most frequent grade value")
    void calculateStatsModeTest() {
        List<Grade> grades = List.of(
                new Grade("STU001", math, 80.0),
                new Grade("STU001", math, 80.0),
                new Grade("STU001", math, 90.0)
        );

        StatisticsCalculator.StatsResult stats = calculator.calculateStats(grades, List.of());

        assertEquals(80.0, stats.getMode(), 0.0001);
    }

    @Test
    @DisplayName("calculateStats() breaks a mode tie by choosing the lower value")
    void calculateStatsModeTieBreaksLowTest() {
        List<Grade> grades = List.of(
                new Grade("STU001", math, 60.0),
                new Grade("STU001", math, 90.0)
        );

        StatisticsCalculator.StatsResult stats = calculator.calculateStats(grades, List.of());

        assertEquals(60.0, stats.getMode(), 0.0001);
    }

    @Test
    @DisplayName("calculateStats() identifies which student/subject earned the highest and lowest grade")
    void calculateStatsIdentifiesMaxMinOwnerTest() {
        Student alice = new RegularStudent("STU001", "Alice Johnson", 16, "alice@school.edu",
                "1234567890", model.enums.StudentStatus.ACTIVE);
        Student bob = new RegularStudent("STU002", "Bob Smith", 16, "bob@school.edu",
                "1234567890", model.enums.StudentStatus.ACTIVE);
        List<Grade> grades = List.of(
                new Grade(alice.getStudentId(), math, 95.0),
                new Grade(bob.getStudentId(), music, 40.0)
        );

        StatisticsCalculator.StatsResult stats = calculator.calculateStats(grades, List.of(alice, bob));

        assertEquals("Alice Johnson", stats.getMaxStudentName());
        assertEquals("Mathematics", stats.getMaxSubjectName());
        assertEquals("Bob Smith", stats.getMinStudentName());
        assertEquals("Music", stats.getMinSubjectName());
    }

    @Test
    @DisplayName("calculateStats() returns all zeros for an empty grade list, not an exception")
    void calculateStatsEmptyTest() {
        StatisticsCalculator.StatsResult stats = calculator.calculateStats(List.of(), List.of());

        assertEquals(0.0, stats.getMean());
        assertEquals(0.0, stats.getMedian());
        assertEquals(0.0, stats.getMode());
        assertEquals(0.0, stats.getStdDev());
    }

    @Test
    @DisplayName("calculateSubjectAverages() averages only the grades recorded for each subject")
    void calculateSubjectAveragesTest() {
        List<Grade> grades = List.of(
                new Grade("STU001", math, 80.0),
                new Grade("STU001", math, 90.0),
                new Grade("STU001", music, 70.0)
        );

        List<StatisticsCalculator.SubjectAverage> averages =
                calculator.calculateSubjectAverages(grades, List.of(math, music));

        assertEquals(2, averages.size());
        assertEquals(85.0, averages.get(0).getAverage(), 0.0001);
        assertEquals(70.0, averages.get(1).getAverage(), 0.0001);
    }

    @Test
    @DisplayName("calculateSubjectAverages() omits a subject with no grades recorded")
    void calculateSubjectAveragesOmitsUnusedSubjectTest() {
        List<Grade> grades = List.of(new Grade("STU001", math, 80.0));

        List<StatisticsCalculator.SubjectAverage> averages =
                calculator.calculateSubjectAverages(grades, List.of(math, music));

        assertEquals(1, averages.size());
        assertEquals("Mathematics", averages.get(0).getSubjectName());
    }

    @Test
    @DisplayName("compareStudentTypes() averages Regular and Honors students separately")
    void compareStudentTypesTest() {
        Student regular = new RegularStudent("Regular Student", 16, "r@school.edu", "1234567890");
        regular.addGrade(60.0);
        Student honors = new HonorsStudent("Honors Student", 16, "h@school.edu", "1234567890");
        honors.addGrade(90.0);

        StatisticsCalculator.StudentTypeComparison comparison =
                calculator.compareStudentTypes(List.of(regular, honors));

        assertEquals(60.0, comparison.getRegularAverage(), 0.0001);
        assertEquals(1, comparison.getRegularCount());
        assertEquals(90.0, comparison.getHonorsAverage(), 0.0001);
        assertEquals(1, comparison.getHonorsCount());
    }
}
