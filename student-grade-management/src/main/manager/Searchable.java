package main.manager;

import main.model.enums.StudentType;
import main.model.student.Student;

import java.util.List;

public interface Searchable {
    List<Student> searchById(String studentId);
    List<Student> searchByName(String name);
    List<Student> searchByGradeRange(double min, double max);
    List<Student> searchByType(StudentType studentType);

    /**
     * v3/US-7: matches students whose email satisfies an arbitrary regex - e.g. {@code .*@university\.edu$}
     * to find every student at one email domain, something {@link #searchByName} substring
     * matching can't express.
     *
     * @throws java.util.regex.PatternSyntaxException if {@code regex} isn't a valid pattern
     */
    List<Student> searchByEmailPattern(String regex);
}
