package repository.grade.impl;

import exceptions.grades.GradeException;
import model.grade.Grade;
import repository.grade.GradeRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GradeRepositoryImpl implements GradeRepository {

    private final Map<String, Grade> gradesMap = new HashMap<>(50);

    @Override
    public void addGrade(Grade grade) {
        gradesMap.put(grade.getGradeId(), grade);
    }

    @Override
    public Grade findGradeById(String gradeId) {
        Grade grade = gradesMap.get(gradeId);
        if (grade == null) {
            throw new GradeException("Grade with ID " + gradeId + " not found");
        }
        return grade;
    }

    @Override
    public List<Grade> findGradesByStudentId(String studentId) {
        return gradesMap.values().stream()
                .filter(grade -> grade.getStudentId().equals(studentId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Grade> getAllGrades() {
        return new ArrayList<>(gradesMap.values());
    }

    @Override
    public void deleteGrade(String gradeId) {
        if (!gradesMap.containsKey(gradeId)) {
            throw new GradeException("Grade with ID " + gradeId + " not found");
        }
        gradesMap.remove(gradeId);
    }
}