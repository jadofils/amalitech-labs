package service;

import exceptions.StudentNotFoundException;
import logging.Logger;
import model.student.Student;
import repository.student.StudentRepository;
import utils.validators.StudentValidator;

import java.util.List;

public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public void addStudent(Student student) {
        Logger.debug("Validating new student: " + student.getName());
        try {
            StudentValidator.validateStudent(student);
        } catch (RuntimeException e) {
            Logger.warn("Rejected new student '" + student.getName() + "': " + e.getMessage());
            throw e;
        }
        studentRepository.addStudent(student);
        Logger.info("Student added: " + student.getStudentId() + " (" + student.getName() + ")");
    }

    @Override
    public Student getStudentById(String studentId){
        Student student = studentRepository.findStudentById(studentId);

        if (student == null) {
            Logger.error("Student lookup returned null unexpectedly for id: " + studentId);
            throw new StudentNotFoundException("Student with id: " + studentId + " not found");

        }

        return student;
    }

    @Override
    public List<Student> getAllStudents() {
        return studentRepository.getAllStudents();
    }

    @Override
    public void updateStudent(Student student) {
        Logger.debug("Validating update for student: " + student.getStudentId());
        try {
            StudentValidator.validateStudent(student);
        } catch (RuntimeException e) {
            Logger.warn("Rejected update for student '" + student.getStudentId() + "': " + e.getMessage());
            throw e;
        }
        studentRepository.updateStudent(student);
        Logger.info("Student updated: " + student.getStudentId());
    }

    @Override
    public void deleteStudent(String studentId) {
        studentRepository.deleteStudent(studentId);
        Logger.info("Student deleted: " + studentId);
    }
}