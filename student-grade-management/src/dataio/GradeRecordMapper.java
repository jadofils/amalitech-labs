package dataio;

import model.grade.Grade;
import model.subject.Subject;
import repository.subject.SubjectRepository;

/** Converts between the domain {@link Grade} and the flat, format-agnostic {@link GradeRecord}. */
public final class GradeRecordMapper {

    private GradeRecordMapper() {
    }

    public static GradeRecord toRecord(Grade grade) {
        return new GradeRecord(
                grade.getGradeId(),
                grade.getStudentId(),
                grade.getSubject().getSubjectCode(),
                grade.getGrade(),
                grade.getDate()
        );
    }

    /** Resolves {@code subjectCode} back into a real {@link Subject} via the repository, then rebuilds the persisted Grade. */
    public static Grade toGrade(GradeRecord record, SubjectRepository subjectRepository) {
        Subject subject = subjectRepository.findSubjectByCode(record.subjectCode());
        return Grade.reconstruct(record.gradeId(), record.studentId(), subject, record.grade(), record.date());
    }
}
