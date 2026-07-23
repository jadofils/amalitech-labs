package main.manager;

import main.model.enums.StudentType;
import main.model.student.Student;

import java.util.List;

public interface Searchable {
    List<Student> searchById(String studentId);
    List<Student> searchByName(String name);
    List<Student> searchByGradeRange(double min, double max);
    List<Student> searchByType(StudentType studentType);
}
