package repository.student;

import exceptions.StudentException;
import exceptions.StudentNotFoundException;
import logging.Logger;
import model.student.HonorsStudent;
import model.student.RegularStudent;
import model.student.Student;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * v3: backed by a {@code LinkedHashMap<String, Student>} keyed on student ID instead of a fixed
 * array - {@link #findStudentById}, {@link #updateStudent}, and {@link #deleteStudent} are O(1)
 * keyed operations instead of an O(n) linear scan. {@code LinkedHashMap} specifically (not
 * {@code HashMap}) so {@link #getAllStudents()} keeps returning students in insertion order,
 * exactly like the array version did - existing callers that grab "the first seeded student" via
 * {@code getAllStudents().get(0)} still get the same student. The public {@link StudentRepository}
 * contract is unchanged; this is an internal storage swap only.
 */
public class StudentRepositoryImpl implements StudentRepository {

    private static final int MAX_CAPACITY = 50;

    private final Map<String, Student> students = new LinkedHashMap<>();

    public StudentRepositoryImpl() {
        // Seed 5 students: 3 Regular, 2 Honors (matching README specification)
        seedStudent(new RegularStudent("Alice Johnson", 17, "alice.johnson@school.edu", "+1-555-0101"));
        seedStudent(new HonorsStudent("Bob Smith", 18, "bob.smith@school.edu", "+1-555-0102"));
        seedStudent(new RegularStudent("Carol Martinez", 16, "carol.martinez@school.edu", "+1-555-0103"));
        seedStudent(new HonorsStudent("David Chen", 17, "david.chen@school.edu", "+1-555-0104"));
        seedStudent(new RegularStudent("Emma Wilson", 16, "emma.wilson@school.edu", "+1-555-0105"));
    }

    private void seedStudent(Student student) {
        students.put(student.getStudentId(), student);
    }

    /** O(1) amortized - one HashMap put, guarded by an O(1) size() capacity check. */
    @Override
    public void addStudent(Student student) {
        if (students.size() >= MAX_CAPACITY) {
            Logger.error("Student storage full at capacity " + MAX_CAPACITY + "; rejected " + student.getName());
            throw new StudentException("Cannot add more students. Storage is full.");
        }
        students.put(student.getStudentId(), student);
    }

    /** O(1) - direct keyed lookup, vs. O(n) linear scan in the array version. */
    @Override
    public Student findStudentById(String studentId) {
        Student student = students.get(studentId);
        if (student == null) {
            throw new StudentNotFoundException("Student with ID " + studentId + " not found.", studentId, getAvailableIds());
        }
        return student;
    }

    /** O(n) - must copy every entry into the returned list; same complexity as the array version. */
    @Override
    public List<Student> getAllStudents() {
        return new ArrayList<>(students.values());
    }

    /** O(1) - keyed replace, vs. O(n) scan-then-replace in the array version. */
    @Override
    public void updateStudent(Student student) {
        String studentId = student.getStudentId();
        if (!students.containsKey(studentId)) {
            throw new StudentNotFoundException("Student with ID " + studentId + " not found.", studentId, getAvailableIds());
        }
        students.put(studentId, student);
    }

    /** O(1) - keyed remove, vs. O(n) scan-then-swap-with-last in the array version. */
    @Override
    public void deleteStudent(String studentId) {
        if (students.remove(studentId) == null) {
            throw new StudentNotFoundException("Student with ID " + studentId + " not found.", studentId, getAvailableIds());
        }
    }

    private List<String> getAvailableIds() {
        return new ArrayList<>(students.keySet());
    }
}
