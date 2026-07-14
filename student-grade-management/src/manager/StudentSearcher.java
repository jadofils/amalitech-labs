package manager;

import interfaces.Searchable;
import model.student.HonorsStudent;
import model.student.Student;

import java.util.ArrayList;
import java.util.List;

public class StudentSearcher implements Searchable {
    private final StudentManager studentManager;

    public StudentSearcher(StudentManager studentManager) {
        this.studentManager = studentManager;
    }

    @Override
    public List<Student> searchById(String studentId) {
        List<Student> results = new ArrayList<>();
        Student s = studentManager.findStudent(studentId);
        if (s != null) {
            results.add(s);
        }
        return results;
    }

    @Override
    public List<Student> searchByName(String name) {
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
    public List<Student> searchByType(boolean isHonors) {
        List<Student> results = new ArrayList<>();
        for (Student s : studentManager.getAllStudents()) {
            if (isHonors && s instanceof HonorsStudent) {
                results.add(s);
            } else if (!isHonors && !(s instanceof HonorsStudent)) {
                results.add(s);
            }
        }
        return results;
    }

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
