package serviceimpl;

import model.Student;
import repository.StudentRepository;
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
        List<Student> students = studentRepository.getAllStudents();

        if (students == null || students.isEmpty()) {
            throw new RuntimeException("No students found in database");
        }

        return students;
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