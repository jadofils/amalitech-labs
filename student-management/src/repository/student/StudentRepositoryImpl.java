package repository.student;

import exceptions.StudentNotFoundException;
import model.student.HonorsStudent;
import model.student.RegularStudent;
import model.student.Student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentRepositoryImpl implements repository.student.StudentRepository {

    private final Map<String, Student> studentsMap = new HashMap<>(50);

    public StudentRepositoryImpl() {
        // Seed 3 students
        seedStudent(new RegularStudent("John Doe", 20, "john.doe@example.com", "1234567890"));
        seedStudent(new HonorsStudent("Jane Smith", 22, "jane.smith@example.com", "0987654321"));
        seedStudent(new RegularStudent("Bob Johnson", 21, "bob.johnson@example.com", "5555555555"));
        
    }

    private void seedStudent(Student student) {
        studentsMap.put(student.getStudentId(), student);
    }

    @Override
    public void addStudent(Student student) {
        studentsMap.put(student.getStudentId(), student);
    }

    @Override
    public Student findStudentById(String studentId) {
        Student student = studentsMap.get(studentId);
        if (student == null) {
            throw new StudentNotFoundException("Student with ID " + studentId + " not found");
        }
        return student;
    }

    @Override
    public List<Student> getAllStudents() {
        return new ArrayList<>(studentsMap.values());
    }

    @Override
    public void updateStudent(Student student) {
        if (!studentsMap.containsKey(student.getStudentId())) {
            throw new StudentNotFoundException("Student with ID " + student.getStudentId() + " not found");
        }
        studentsMap.put(student.getStudentId(), student);
    }

    @Override
    public void deleteStudent(String studentId) {
        if (!studentsMap.containsKey(studentId)) {
            throw new StudentNotFoundException("Student with ID " + studentId + " not found");
        }
        studentsMap.remove(studentId);
    }
}