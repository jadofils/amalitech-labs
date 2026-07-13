package service.serviceimpl;

import model.student.Student;
import repository.student.StudentRepository;
import service.StudentService;
import validation.StudentValidator;

import java.util.List;

public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public void addStudent(Student student) {
        StudentValidator.validateStudent(student);
        studentRepository.addStudent(student);
    }

    @Override
    public Student getStudentById(String studentId) {
        Student student = studentRepository.findStudentById(studentId);

        if (student == null) {
            throw new RuntimeException("Student with id: " + studentId + " not found");
        }

        return student;
    }

    @Override
    public List<Student> getAllStudents() {
        return studentRepository.getAllStudents();
    }

    @Override
    public void updateStudent(Student student) {
        StudentValidator.validateStudent(student);
        studentRepository.updateStudent(student);
    }

    @Override
    public void deleteStudent(String studentId) {
        studentRepository.deleteStudent(studentId);
    }
}