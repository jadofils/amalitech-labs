package service.serviceimpl;

import exceptions.grades.GradeException;
import exceptions.subjects.SubjectNotFoundException;
import logging.Logger;
import model.grade.Grade;
import model.subject.Subject;
import repository.grade.GradeRepository;
import repository.grade.impl.GradeRepositoryImpl;
import repository.student.StudentRepository;
import repository.student.StudentRepositoryImpl;
import repository.subject.SubjectRepository;
import repository.subject.impl.SubjectRepositoryImpl;
import service.GradeService;

import java.util.List;

public class GradeServiceImpl implements GradeService {
    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;

    public GradeServiceImpl() {
        this(new GradeRepositoryImpl(), new StudentRepositoryImpl(), new SubjectRepositoryImpl());
    }

    public GradeServiceImpl(StudentRepository studentRepository, SubjectRepository subjectRepository) {
        this(new GradeRepositoryImpl(), studentRepository, subjectRepository);
    }

    public GradeServiceImpl(GradeRepository gradeRepository,
                            StudentRepository studentRepository,
                            SubjectRepository subjectRepository) {
        this.gradeRepository = gradeRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
    }

    @Override
    public void recordGrade(Grade grade) {
        Logger.debug("Recording grade for student " + grade.getStudentId()
                + " in subject " + grade.getSubject().getSubjectCode());

        // Validate student exists (throws StudentNotFoundException if not)
        try {
            studentRepository.findStudentById(grade.getStudentId());
        } catch (RuntimeException e) {
            Logger.warn("recordGrade rejected: unknown student " + grade.getStudentId());
            throw e;
        }

        // Validate subject exists
        Subject subject;
        try {
            subject = subjectRepository.findSubjectByCode(grade.getSubject().getSubjectCode());
        } catch (RuntimeException e) {
            Logger.warn("recordGrade rejected: unknown subject " + grade.getSubject().getSubjectCode());
            throw e;
        }
        if (subject == null) {
            throw new SubjectNotFoundException("Subject with code " + grade.getSubject().getSubjectCode() + " not found.");
        }

        // Grade value range is already enforced by Grade's constructor (Gradable.recordGrade)

        // Save grade
        gradeRepository.addGrade(grade);
        Logger.info("Grade recorded: " + grade.getGradeId() + " (" + grade.getGrade() + ") for student " + grade.getStudentId());
    }

    @Override
    public Grade getGradeById(String gradeId) {
        Grade grade = gradeRepository.findGradeById(gradeId);
        if (grade == null) {
            Logger.error("Grade lookup returned null unexpectedly for id: " + gradeId);
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
        Logger.info("Grade deleted: " + gradeId);
    }
}
