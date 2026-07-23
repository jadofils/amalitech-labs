package main.repository.grade;

import main.model.grade.Grade;

import java.util.List;

public interface GradeRepository {
    void addGrade(Grade grade);
    Grade findGradeById(String gradeId);
    List<Grade> findGradesByStudentId(String studentId);
    List<Grade> getAllGrades();
    void deleteGrade(String gradeId);
}


