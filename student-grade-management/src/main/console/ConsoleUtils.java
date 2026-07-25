package main.console;

import main.exceptions.StudentNotFoundException;
import main.manager.StudentManager;
import main.model.student.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Small pieces of main.console behavior shared by more than one {@link MenuAction}. */
public final class ConsoleUtils {

    /** The standard section divider printed under most headings. */
    public static final String DIVIDER = "─────────────────────────";

    /** A wider divider for tables with more columns (e.g. student/search-result listings). */
    public static final String WIDE_DIVIDER = "────────────────────────────────────────────────────";

    private ConsoleUtils() {
    }

    public static void promptEnter(Scanner scanner) {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public static List<String> getAvailableStudentIds(StudentManager studentManager) {
        List<String> ids = new ArrayList<>();
        for (Student s : studentManager.getAllStudents()) {
            ids.add(s.getStudentId());
        }
        return ids;
    }

    /**
     * Looks up {@code studentId}, throwing {@link StudentNotFoundException} (with the currently
     * available IDs attached) if there's no match - the same "find or throw" a handful of menu
     * actions each need before they can do anything else with a student.
     */
    public static Student requireStudent(StudentManager studentManager, String studentId) {
        Student student = studentManager.findStudent(studentId);
        if (student == null) {
            throw new StudentNotFoundException("Student with ID '" + studentId + "' not found.",
                    studentId, getAvailableStudentIds(studentManager));
        }
        return student;
    }
}
