package repository.student;

import exceptions.StudentNotFoundException;
import logging.Logger;
import model.student.HonorsStudent;
import model.student.RegularStudent;
import model.student.Student;

import java.util.ArrayList;
import java.util.List;

public class StudentRepositoryImpl implements StudentRepository {

    private final Student[] students = new Student[50];
    private int studentCount = 0;

    public StudentRepositoryImpl() {
        // Seed 5 students: 3 Regular, 2 Honors (matching README specification)
        seedStudent(new RegularStudent("Alice Johnson", 17, "alice.johnson@school.edu", "+1-555-0101"));
        seedStudent(new HonorsStudent("Bob Smith", 18, "bob.smith@school.edu", "+1-555-0102"));
        seedStudent(new RegularStudent("Carol Martinez", 16, "carol.martinez@school.edu", "+1-555-0103"));
        seedStudent(new HonorsStudent("David Chen", 17, "david.chen@school.edu", "+1-555-0104"));
        seedStudent(new RegularStudent("Emma Wilson", 16, "emma.wilson@school.edu", "+1-555-0105"));
    }

    private void seedStudent(Student student) {
        students[studentCount++] = student;
    }

    @Override
    public void addStudent(Student student) {
        if (studentCount >= students.length) {
            Logger.error("Student storage full at capacity " + students.length + "; rejected " + student.getName());
            throw new RuntimeException("Cannot add more students. Storage is full.");
        }
        students[studentCount++] = student;
    }

    @Override
    public Student findStudentById(String studentId) {
        for (int i = 0; i < studentCount; i++) {
            if (students[i].getStudentId().equals(studentId)) {
                return students[i];
            }
        }
        throw new StudentNotFoundException("Student with ID " + studentId + " not found.", studentId, getAvailableIds());
    }

    @Override
    public List<Student> getAllStudents() {
        List<Student> result = new ArrayList<>();
        for (int i = 0; i < studentCount; i++) {
            result.add(students[i]);
        }
        return result;
    }

    @Override
    public void updateStudent(Student student) {
        for (int i = 0; i < studentCount; i++) {
            if (students[i].getStudentId().equals(student.getStudentId())) {
                students[i] = student;
                return;
            }
        }
        throw new StudentNotFoundException("Student with ID " + student.getStudentId() + " not found.", student.getStudentId(), getAvailableIds());
    }

    @Override
    public void deleteStudent(String studentId) {
        for (int i = 0; i < studentCount; i++) {
            if (students[i].getStudentId().equals(studentId)) {
                students[i] = students[studentCount - 1];
                students[studentCount - 1] = null;
                studentCount--;
                return;
            }
        }
        throw new StudentNotFoundException("Student with ID " + studentId + " not found.", studentId, getAvailableIds());
    }

    private List<String> getAvailableIds() {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < studentCount; i++) {
            ids.add(students[i].getStudentId());
        }
        return ids;
    }
}
