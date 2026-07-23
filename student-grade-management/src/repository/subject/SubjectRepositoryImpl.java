package repository.subject;

import exceptions.SubjectException;
import exceptions.SubjectNotFoundException;
import logging.Logger;
import model.subject.Subject;
import utils.validators.SubjectValidator;
import model.subject.CoreSubject;
import model.subject.ElectiveSubject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * v3: backed by a {@code LinkedHashMap<String, Subject>} keyed on subject code instead of a fixed
 * array - same O(1)-keyed-operation rationale as {@link repository.student.StudentRepositoryImpl}.
 * Also closes a real gap the array version had: {@link #addSubject} now actually rejects a
 * duplicate subject code (checked via the map key itself - a second {@code HashSet} would just
 * duplicate what the map's own keys already are) instead of silently accepting a second entry that
 * {@link #findSubjectByCode}/{@link #deleteSubject} could never reach.
 */
public class SubjectRepositoryImpl implements SubjectRepository {

    private static final int MAX_CAPACITY = 50;

    private final Map<String, Subject> subjects = new LinkedHashMap<>();

    public SubjectRepositoryImpl() {
        // Seed Core Subjects (matching README specification)
        seedSubject(new CoreSubject("Mathematics", "MATH01"));
        seedSubject(new CoreSubject("English", "ENGL01"));
        seedSubject(new CoreSubject("Science", "SCIE01"));

        // Seed Elective Subjects (matching README specification)
        seedSubject(new ElectiveSubject("Music", "MUSC01"));
        seedSubject(new ElectiveSubject("Art", "ART01"));
        seedSubject(new ElectiveSubject("Physical Education", "PHED01"));
    }

    private void seedSubject(Subject subject) {
        subjects.put(subject.getSubjectCode(), subject);
    }

    /** O(1) - HashMap containsKey + put, guarded by an O(1) size() capacity check. */
    @Override
    public void addSubject(Subject subject) {
        SubjectValidator.validateSubject(subject);
        String subjectCode = subject.getSubjectCode();
        if (subjects.containsKey(subjectCode)) {
            throw new SubjectException("A subject with code '" + subjectCode + "' already exists.", subjectCode);
        }
        if (subjects.size() >= MAX_CAPACITY) {
            Logger.error("Subject storage full at capacity " + MAX_CAPACITY + "; rejected " + subjectCode);
            throw new SubjectException("Cannot add more subjects. Storage is full.");
        }
        subjects.put(subjectCode, subject);
    }

    /** O(1) - direct keyed lookup, vs. O(n) linear scan in the array version. */
    @Override
    public Subject findSubjectByCode(String subjectCode) {
        Subject subject = subjects.get(subjectCode);
        if (subject == null) {
            throw new SubjectNotFoundException("Subject with code " + subjectCode + " not found");
        }
        return subject;
    }

    /** O(n) - must copy every entry into the returned list; same complexity as the array version. */
    @Override
    public List<Subject> getAllSubjects() {
        return new ArrayList<>(subjects.values());
    }

    /** O(1) - keyed remove, vs. O(n) scan-then-swap-with-last in the array version. */
    @Override
    public void deleteSubject(String subjectCode) {
        if (subjects.remove(subjectCode) == null) {
            throw new SubjectNotFoundException("Subject with code " + subjectCode + " not found.");
        }
    }
}
