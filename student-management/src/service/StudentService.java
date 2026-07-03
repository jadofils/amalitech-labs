package service;

import model.student.Student;

import java.util.List;

public interface StudentService {
    void addStudent(Student student);
    Student getStudentById(String studentId);
    List<Student> getAllStudents();
    void updateStudent(Student student);
    void deleteStudent(String studentId);


}
