package repository;

import model.student.Student;

import java.util.List;

public interface StudentRepository {
    void addStudent(Student student);
    Student findStudentById(String studentId);
    List<Student> getAllStudents();
    void updateStudent(Student student);
    void deleteStudent(String studentId);
}
