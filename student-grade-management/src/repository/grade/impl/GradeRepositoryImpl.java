package repository.grade.impl;

import exceptions.grades.GradeException;
import model.grade.Grade;
import repository.grade.GradeRepository;

import java.util.ArrayList;
import java.util.List;

public class GradeRepositoryImpl implements GradeRepository {

    private final Grade[] grades = new Grade[200];
    private int gradeCount = 0;

    @Override
    public void addGrade(Grade grade) {
        if (gradeCount >= grades.length) {
            throw new GradeException("Cannot add more grades. Storage is full.");
        }
        grades[gradeCount++] = grade;
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

    @Override
    public List<Grade> findGradesByStudentId(String studentId) {
        List<Grade> result = new ArrayList<>();
        for (int i = 0; i < gradeCount; i++) {
            if (grades[i].getStudentId().equals(studentId)) {
                result.add(grades[i]);
            }
        }
        return result;
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
                grades[i] = grades[gradeCount - 1];
                grades[gradeCount - 1] = null;
                gradeCount--;
                return;
            }
        }
        throw new GradeException("Grade with ID " + gradeId + " not found");
    }

    public int getGradeCount() {
        return gradeCount;
    }
}
