package console;

import manager.StudentManager;
import model.student.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Small pieces of console behavior shared by more than one {@link MenuAction}. */
public final class ConsoleUtils {

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
}
