package main.manager;

import main.logging.Logger;
import main.model.enums.StudentType;
import main.model.student.Student;

import java.util.ArrayList;
import java.util.List;

/** Search Students (menu option 9): by exact ID, partial name, grade range, or student type. */
public class StudentSearcher implements Searchable {
    private final StudentManager studentManager;

    public StudentSearcher(StudentManager studentManager) {
        this.studentManager = studentManager;
    }

    @Override
    public List<Student> searchById(String studentId) {
        Logger.debug("Searching students by ID: " + studentId);
        List<Student> results = new ArrayList<>();
        Student s = studentManager.findStudent(studentId);
        if (s != null) {
            results.add(s);
        }
        return results;
    }

    @Override
    public List<Student> searchByName(String name) {
        Logger.debug("Searching students by name containing: " + name);
        List<Student> results = new ArrayList<>();
        String lower = name.toLowerCase();
        for (Student s : studentManager.getAllStudents()) {
            if (s.getName().toLowerCase().contains(lower)) {
                results.add(s);
            }
        }
        return results;
    }

    @Override
    public List<Student> searchByGradeRange(double min, double max) {
        Logger.debug("Searching students with average grade between " + min + " and " + max);
        List<Student> results = new ArrayList<>();
        for (Student s : studentManager.getAllStudents()) {
            double avg = s.calculateAverageGrade();
            if (avg >= min && avg <= max) {
                results.add(s);
            }
        }
        return results;
    }

    @Override
    public List<Student> searchByType(StudentType studentType) {
        Logger.debug("Searching students by type: " + studentType);
        List<Student> results = new ArrayList<>();
        for (Student s : studentManager.getAllStudents()) {
            if (s.getType() == studentType) {
                results.add(s);
            }
        }
        return results;
    }

    /** Human-readable description of a search, for display alongside its results. */
    public String getSearchDescription(String option, String input) {
        return switch (option) {
            case "1" -> "ID: " + input;
            case "2" -> "Name: \"" + input + "\"";
            case "3" -> "Grade range: " + input;
            case "4" -> "Type: " + (input.equals("2") ? "Honors" : "Regular");
            default -> "";
        };
    }
}
