package console;

import calculators.StatisticsCalculator;
import manager.GradeManager;
import manager.StudentManager;
import model.grade.Grade;
import model.student.Student;
import repository.subject.SubjectRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Menu option 8: View Class Statistics. */
public class ClassStatisticsAction implements MenuAction {

    private final Scanner scanner;
    private final StudentManager studentManager;
    private final GradeManager gradeManager;
    private final StatisticsCalculator statisticsCalculator;
    private final SubjectRepository subjectRepository;

    public ClassStatisticsAction(Scanner scanner, StudentManager studentManager, GradeManager gradeManager,
                                  StatisticsCalculator statisticsCalculator, SubjectRepository subjectRepository) {
        this.scanner = scanner;
        this.studentManager = studentManager;
        this.gradeManager = gradeManager;
        this.statisticsCalculator = statisticsCalculator;
        this.subjectRepository = subjectRepository;
    }

    @Override
    public int getOptionNumber() {
        return 8;
    }

    @Override
    public String getLabel() {
        return "View Class Statistics";
    }

    @Override
    public void execute() {
        System.out.println("\nCLASS STATISTICS");
        System.out.println("─────────────────────────");

        List<Student> students = studentManager.getAllStudents();
        List<Grade> allGrades = new ArrayList<>();
        for (Student s : students) {
            allGrades.addAll(gradeManager.getGradesForStudent(s.getStudentId()));
        }

        System.out.println("\nTotal Students: " + students.size());
        System.out.println("Total Grades Recorded: " + allGrades.size());

        if (allGrades.isEmpty()) {
            System.out.println("No grades recorded yet.");
            ConsoleUtils.promptEnter(scanner);
            return;
        }

        StatisticsCalculator.GradeDistribution dist = statisticsCalculator.calculateDistribution(allGrades);
        StatisticsCalculator.StatsResult stats = statisticsCalculator.calculateStats(allGrades, students);
        List<StatisticsCalculator.SubjectAverage> subjAverages = statisticsCalculator.calculateSubjectAverages(allGrades, subjectRepository.getAllSubjects());
        StatisticsCalculator.StudentTypeComparison typeComp = statisticsCalculator.compareStudentTypes(students);

        System.out.println("\nGRADE DISTRIBUTION");
        System.out.println("─────────────────────────");
        String[] labels = dist.getLabels();
        int[] counts = dist.getCounts();
        for (int i = 0; i < 5; i++) {
            double pct = dist.getPercentages()[i];
            int barLen = (int) (pct / 2);
            String bar = "█".repeat(Math.max(0, barLen)) + "░".repeat(Math.max(0, 50 - barLen));
            System.out.printf("%s: %s %.1f%% (%d grades)%n", labels[i], bar, pct, counts[i]);
        }

        System.out.println("\nSTATISTICAL ANALYSIS");
        System.out.println("─────────────────────────");
        System.out.printf("Mean (Average):      %.1f%%%n", stats.getMean());
        System.out.printf("Median:              %.1f%%%n", stats.getMedian());
        System.out.printf("Mode:                %.1f%%%n", stats.getMode());
        System.out.printf("Standard Deviation:  %.1f%%%n", stats.getStdDev());
        System.out.printf("Range:               %.1f%% (%.0f%% - %.0f%%)%n", stats.getRange(), stats.getMin(), stats.getMax());

        System.out.printf("%nHighest Grade:  %.0f%% (%s - %s)%n", stats.getMax(), stats.getMaxStudentName(), stats.getMaxSubjectName());
        System.out.printf("Lowest Grade:    %.0f%% (%s - %s)%n", stats.getMin(), stats.getMinStudentName(), stats.getMinSubjectName());

        System.out.println("\nSUBJECT PERFORMANCE");
        System.out.println("─────────────────────────");
        for (StatisticsCalculator.SubjectAverage sa : subjAverages) {
            System.out.printf("  %s: %.1f%%%n", sa.getSubjectName(), sa.getAverage());
        }

        System.out.println("\nSTUDENT TYPE COMPARISON");
        System.out.println("─────────────────────────");
        if (typeComp.getRegularCount() > 0)
            System.out.printf("  Regular Students:  %.1f%% average (%d students)%n", typeComp.getRegularAverage(), typeComp.getRegularCount());
        if (typeComp.getHonorsCount() > 0)
            System.out.printf("  Honors Students:   %.1f%% average (%d students)%n", typeComp.getHonorsAverage(), typeComp.getHonorsCount());

        ConsoleUtils.promptEnter(scanner);
    }
}
