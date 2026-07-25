package main.console;

import main.manager.GradeManager;
import main.manager.StudentManager;

import java.util.Scanner;

/**
 * Shared scanner/studentManager/gradeManager plumbing for the menu actions that all
 * need this exact trio of collaborators (view/record/export a report, calculate GPA).
 */
public abstract class AbstractGradeAction extends AbstractMenuAction {

    protected final Scanner scanner;
    protected final StudentManager studentManager;
    protected final GradeManager gradeManager;

    protected AbstractGradeAction(int optionNumber, String label, Scanner scanner,
                                   StudentManager studentManager, GradeManager gradeManager) {
        super(optionNumber, label);
        this.scanner = scanner;
        this.studentManager = studentManager;
        this.gradeManager = gradeManager;
    }
}
