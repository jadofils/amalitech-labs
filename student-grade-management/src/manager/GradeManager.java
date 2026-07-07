package manager;

import model.enums.SubjectType;
import model.grade.Grade;
import model.subject.Subject;
import repository.subject.SubjectRepository;
import service.GradeService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Backed by the database (GradeService/GradeRepository) instead of an in-memory array for now.
public class GradeManager {
    private final GradeService gradeService;
    private final SubjectRepository subjectRepository;

    public GradeManager(GradeService gradeService, SubjectRepository subjectRepository) {
        this.gradeService = gradeService;
        this.subjectRepository = subjectRepository;
        syncGradeCounter();
    }

    private void syncGradeCounter() {
        try {
            int highest = 0;
            for (Grade grade : gradeService.getAllGrades()) {
                highest = Math.max(highest, extractSequence(grade.getGradeId()));
            }
            Grade.initializeCounter(highest);
        } catch (RuntimeException e) {
            // Database not reachable yet; counter will sync next time grades are read successfully.
        }
    }

    private int extractSequence(String id) {
        String digits = id.replaceAll("\\D", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    public void addGrade(Grade grade) {
        gradeService.recordGrade(grade);
    }

    public List<Grade> getGradesForStudent(String studentId) {
        return gradeService.getGradesByStudentId(studentId);
    }

    public List<Subject> getSubjectsByType(SubjectType type) {
        List<Subject> result = new ArrayList<>();
        for (Subject subject : subjectRepository.getAllSubjects()) {
            if (subject.getSubjectType() == type) {
                result.add(subject);
            }
        }
        return result;
    }

    public void viewGradesByStudent(String studentId) {
        List<Grade> grades = new ArrayList<>(gradeService.getGradesByStudentId(studentId));
        if (grades.isEmpty()) {
            System.out.println("No grades recorded for this student.");
            return;
        }
        // Reverse chronological order; break same-day ties by grade ID so the newest
        // recorded grade still comes first (grade IDs are zero-padded, so lexicographic
        // and numeric order agree).
        grades.sort(Comparator.comparing(Grade::getDate).thenComparing(Grade::getGradeId).reversed());

        System.out.println("GRADE HISTORY");
        System.out.println("───────────────────────────────────────────────────────────────────────");
        System.out.printf("%-8s| %-10s | %-16s | %-9s | %s%n", "GRD ID", "DATE", "SUBJECT", "TYPE", "GRADE");
        System.out.println("───────────────────────────────────────────────────────────────────────");
        for (Grade grade : grades) {
            System.out.printf("%-8s| %-10s | %-16s | %-9s | %.1f%%%n",
                    grade.getGradeId(), grade.getDate(), grade.getSubject().getSubjectName(),
                    grade.getSubjectType(), grade.getGrade());
        }
        System.out.println("───────────────────────────────────────────────────────────────────────");
        System.out.println("Total Grades: " + grades.size());
        System.out.printf("Core Subjects Average: %.1f%%%n", calculateCoreAverage(studentId));
        System.out.printf("Elective Subjects Average: %.1f%%%n", calculateElectiveAverage(studentId));
        System.out.printf("Overall Average: %.1f%%%n", calculateOverallAverage(studentId));
    }

    public double calculateCoreAverage(String studentId) {
        return averageByType(studentId, SubjectType.CORE);
    }

    public double calculateElectiveAverage(String studentId) {
        return averageByType(studentId, SubjectType.ELECTIVE);
    }

    public double calculateOverallAverage(String studentId) {
        return average(gradeService.getGradesByStudentId(studentId));
    }

    public int getGradeCount() {
        return gradeService.getAllGrades().size();
    }

    private double averageByType(String studentId, SubjectType type) {
        List<Grade> filtered = new ArrayList<>();
        for (Grade grade : gradeService.getGradesByStudentId(studentId)) {
            if (grade.getSubjectType() == type) {
                filtered.add(grade);
            }
        }
        return average(filtered);
    }

    private double average(List<Grade> grades) {
        if (grades.isEmpty()) return 0.0;
        double sum = 0;
        for (Grade grade : grades) {
            sum += grade.getGrade();
        }
        return sum / grades.size();
    }
}
