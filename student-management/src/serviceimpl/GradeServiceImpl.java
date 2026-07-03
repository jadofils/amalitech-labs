package serviceimpl;

import exceptions.StudentNotFoundException;
import exceptions.grades.GradeException;
import exceptions.subjects.SubjectNotFoundException;
import model.grade.Grade;
import model.subject.Subject;
import repository.StudentRepository;
import repository.grade.GradeRepository;
import repository.grade.impl.GradeRepositoryImpl;
import repository.impl.StudentRepositoryImpl;
import repository.subject.SubjectRepository;
import repository.subject.impl.SubjectRepositoryImpl;
import service.GradeService;
import validation.GradeValidator;

import java.util.List;

public class GradeServiceImpl implements GradeService {
    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;

    public GradeServiceImpl() {
        this.gradeRepository = new GradeRepositoryImpl();
        this.studentRepository = new StudentRepositoryImpl();
        this.subjectRepository = new SubjectRepositoryImpl();
    }

    @Override
    public void recordGrade(Grade grade) {
        // Validate student exists
        if (studentRepository.findStudentById(grade.getStudentId()) == null) {
            throw new StudentNotFoundException("Student with ID " + grade.getStudentId() + " not found.");
        }

        // Validate subject exists
        Subject subject = subjectRepository.findSubjectByCode(grade.getSubject().getSubjectCode());
        if (subject == null) {
            throw new SubjectNotFoundException("Subject with code " + grade.getSubject().getSubjectCode() + " not found.");
        }

        // Validate grade range
        GradeValidator.validateGrade(grade.getGrade());

        // Save grade
        gradeRepository.addGrade(grade);
    }

    @Override
    public Grade getGradeById(String gradeId) {
        Grade grade = gradeRepository.findGradeById(gradeId);
        if (grade == null) {
            throw new GradeException("Grade with ID " + gradeId + " not found.");
        }
        return grade;
    }

    @Override
    public List<Grade> getGradesByStudentId(String studentId) {
        return gradeRepository.findGradesByStudentId(studentId);
    }

    @Override
    public List<Grade> getAllGrades() {
        return gradeRepository.getAllGrades();
    }

    @Override
    public void deleteGrade(String gradeId) {
        gradeRepository.deleteGrade(gradeId);
    }
}
