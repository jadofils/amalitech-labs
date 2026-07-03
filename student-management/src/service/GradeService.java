package service;

import model.grade.Grade;

import java.util.List;

public interface GradeService {
    void recordGrade(Grade grade);
    Grade getGradeById(String gradeId);
    List<Grade> getGradesByStudentId(String studentId);
    List<Grade> getAllGrades();
    void deleteGrade(String gradeId);
}
