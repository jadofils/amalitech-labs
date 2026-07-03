package repository.subject;

import model.subject.Subject;
import java.util.List;

public interface SubjectRepository {
    void addSubject(Subject subject);
    Subject findSubjectByCode(String subjectCode);
    List<Subject> getAllSubjects();
    void deleteSubject(String subjectCode);
}
