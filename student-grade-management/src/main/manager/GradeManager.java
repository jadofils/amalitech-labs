package main.manager;

import main.concurrent.LruCache;
import main.model.enums.SubjectType;
import main.model.grade.Grade;
import main.model.subject.Subject;
import main.repository.subject.SubjectRepository;
import main.service.GradeService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

// Backed by the database (GradeService/GradeRepository) instead of an in-memory array for now.
public class GradeManager {
    private static final String DIVIDER = "───────────────────────────────────────────────────────────────────────";

    private final GradeService gradeService;
    private final SubjectRepository subjectRepository;

    // v3/PBI-5: guards addGrade() against main.concurrent.StatisticsDashboard's background
    // refresh reading grade data at the same moment a console action records a new one -
    // readLocked() is how the dashboard's refresh reads under the same lock. Scoped to grade
    // *entry* specifically, matching PBI-5's acceptance criteria; student mutation isn't guarded
    // here since the dashboard's snapshot doesn't read student-mutation-sensitive state beyond
    // what StudentManager.getAllStudents() already returns as an independent copy.
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    // v3/PBI-8: caches getGradesForStudent()'s result per student ID. addGrade() invalidates the
    // affected student's entry as part of the same write - both live in this class, so cache and
    // write path never drift apart the way they could if a different class owned either half.
    private final LruCache<String, List<Grade>> gradeCache = new LruCache<>();

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
        lock.writeLock().lock();
        try {
            gradeService.recordGrade(grade);
            gradeCache.invalidate(grade.getStudentId());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /** Runs {@code read} under this manager's read lock, so it can't observe a partial {@link #addGrade} write. */
    public <T> T readLocked(Supplier<T> read) {
        lock.readLock().lock();
        try {
            return read.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Grade> getGradesForStudent(String studentId) {
        List<Grade> cached = gradeCache.get(studentId);
        if (cached != null) {
            return cached;
        }
        List<Grade> fetched = gradeService.getGradesByStudentId(studentId);
        gradeCache.put(studentId, fetched);
        return fetched;
    }

    /** This cache's hit rate so far - surfaced on {@link main.concurrent.StatisticsDashboard} per US-8. */
    public double getGradeCacheHitRate() {
        return gradeCache.hitRate();
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
        System.out.println(DIVIDER);
        System.out.printf("%-8s| %-10s | %-16s | %-9s | %s%n", "GRD ID", "DATE", "SUBJECT", "TYPE", "GRADE");
        System.out.println(DIVIDER);
        for (Grade grade : grades) {
            System.out.printf("%-8s| %-10s | %-16s | %-9s | %.1f%%%n",
                    grade.getGradeId(), grade.getDate(), grade.getSubject().getSubjectName(),
                    grade.getSubjectType(), grade.getGrade());
        }
        System.out.println(DIVIDER);
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
