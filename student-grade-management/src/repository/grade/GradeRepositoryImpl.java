package repository.grade;

import exceptions.GradeException;
import logging.Logger;
import model.grade.Grade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * v3: the fixed-size array stays the source of truth for {@link #getAllGrades()} ordering,
 * {@link #findGradeById}, and the "storage full" capacity check - unchanged from v2. Alongside it,
 * {@link #addGrade}/{@link #deleteGrade} now also maintain a
 * {@code Map<String, LinkedList<Grade>>} secondary index keyed by student ID, so
 * {@link #findGradesByStudentId} is an O(1) map lookup + O(k) copy (k = that one student's own
 * grade count) instead of an O(n) scan over every grade ever recorded. {@code LinkedList} because
 * a student's own grade history, in the order it was recorded, is exactly what PBI-1 asks for.
 */
public class GradeRepositoryImpl implements GradeRepository {

    private final Grade[] grades = new Grade[200];
    private int gradeCount = 0;
    private final Map<String, LinkedList<Grade>> gradesByStudent = new HashMap<>();

    @Override
    public void addGrade(Grade grade) {
        if (gradeCount >= grades.length) {
            Logger.error("Grade storage full at capacity " + grades.length + "; rejected grade for student " + grade.getStudentId());
            throw new GradeException("Cannot add more grades. Storage is full.");
        }
        grades[gradeCount++] = grade;
        gradesByStudent.computeIfAbsent(grade.getStudentId(), id -> new LinkedList<>()).add(grade);
    }

    @Override
    public Grade findGradeById(String gradeId) {
        for (int i = 0; i < gradeCount; i++) {
            if (grades[i].getGradeId().equals(gradeId)) {
                return grades[i];
            }
        }
        throw new GradeException("Grade with ID " + gradeId + " not found");
    }

    /** O(1) map lookup + O(k) copy (k = this student's own grade count), vs. O(n) over every grade. */
    @Override
    public List<Grade> findGradesByStudentId(String studentId) {
        LinkedList<Grade> studentGrades = gradesByStudent.get(studentId);
        return studentGrades == null ? new ArrayList<>() : new ArrayList<>(studentGrades);
    }

    @Override
    public List<Grade> getAllGrades() {
        List<Grade> result = new ArrayList<>();
        for (int i = 0; i < gradeCount; i++) {
            result.add(grades[i]);
        }
        return result;
    }

    @Override
    public void deleteGrade(String gradeId) {
        for (int i = 0; i < gradeCount; i++) {
            if (grades[i].getGradeId().equals(gradeId)) {
                Grade removed = grades[i];
                grades[i] = grades[gradeCount - 1];
                grades[gradeCount - 1] = null;
                gradeCount--;
                removeFromStudentIndex(removed);
                return;
            }
        }
        throw new GradeException("Grade with ID " + gradeId + " not found");
    }

    private void removeFromStudentIndex(Grade removed) {
        LinkedList<Grade> studentGrades = gradesByStudent.get(removed.getStudentId());
        if (studentGrades == null) {
            return;
        }
        studentGrades.remove(removed);
        if (studentGrades.isEmpty()) {
            gradesByStudent.remove(removed.getStudentId());
        }
    }

    public int getGradeCount() {
        return gradeCount;
    }
}
