package main.console;

import main.backup.BackupPayload;
import main.backup.BackupService;
import main.dataio.GradeRecord;
import main.dataio.GradeRecordMapper;
import main.dataio.StudentRecord;
import main.dataio.StudentRecordMapper;
import main.exceptions.BackupException;
import main.manager.GradeManager;
import main.manager.StudentManager;
import main.model.enums.Role;
import main.model.grade.Grade;
import main.model.student.Student;
import main.repository.subject.SubjectRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

/**
 * Menu option 11: Backup &amp; Restore. The one menu action whose collaborator,
 * {@link BackupService}, declares a CHECKED exception ({@link BackupException}) rather than one of
 * the {@link main.exceptions.ApplicationException} subtypes every other action deals with - the
 * compiler enforces that {@link #createBackup} and {@link #restoreBackup} either catch it or
 * declare it, unlike everything else in {@code main.console}, which can let its exceptions
 * propagate all the way up to {@code ConsoleApp}'s single catch-all unnoticed by the compiler.
 */
public class BackupAction extends AbstractMenuAction {

    private static final Path BACKUP_DIR = Path.of("backups");

    private final Scanner scanner;
    private final StudentManager studentManager;
    private final GradeManager gradeManager;
    private final SubjectRepository subjectRepository;
    private final BackupService backupService;

    public BackupAction(Scanner scanner, StudentManager studentManager, GradeManager gradeManager,
                         SubjectRepository subjectRepository, BackupService backupService) {
        super(11, "Backup & Restore");
        this.scanner = scanner;
        this.studentManager = studentManager;
        this.gradeManager = gradeManager;
        this.subjectRepository = subjectRepository;
        this.backupService = backupService;
    }

    @Override
    public boolean isAuthorizedFor(Role role) {
        return role == Role.TEACHER;
    }

    @Override
    public void execute() {
        System.out.println("\nBACKUP & RESTORE");
        System.out.println(ConsoleUtils.DIVIDER);
        System.out.println("1. Create backup");
        System.out.println("2. Restore from backup");
        System.out.print("Select option (1-2): ");
        String choice = scanner.nextLine().trim();

        if (choice.equals("1")) {
            createBackup();
        } else if (choice.equals("2")) {
            restoreBackup();
        } else {
            System.out.println("Invalid option.");
        }

        ConsoleUtils.promptEnter(scanner);
    }

    private void createBackup() {
        Path path = promptBackupPath();
        if (path == null) {
            return;
        }
        try {
            Files.createDirectories(BACKUP_DIR);
            List<StudentRecord> students = studentManager.getAllStudents().stream()
                    .map(StudentRecordMapper::toRecord).toList();
            List<GradeRecord> grades = gradeManager.getAllGrades().stream()
                    .map(GradeRecordMapper::toRecord).toList();

            backupService.createBackup(path, students, grades);

            System.out.println("\n✓ Backup created: " + path);
            System.out.println("  Students: " + students.size() + ", Grades: " + grades.size());
        } catch (BackupException e) {
            printBackupError(e);
        } catch (IOException e) {
            System.out.println("\n✗ ERROR: Could not create the backups directory: " + e.getMessage());
        }
    }

    private void restoreBackup() {
        Path path = promptBackupPath();
        if (path == null) {
            return;
        }
        try {
            BackupPayload payload = backupService.restoreBackup(path);

            for (StudentRecord studentRecord : payload.students()) {
                Student student = StudentRecordMapper.toStudent(studentRecord);
                studentManager.addStudent(student);
            }
            for (GradeRecord gradeRecord : payload.grades()) {
                Grade grade = GradeRecordMapper.toGrade(gradeRecord, subjectRepository);
                gradeManager.addGrade(grade);
            }

            System.out.println("\n✓ Restored from backup: " + path);
            System.out.println("  Students: " + payload.students().size() + ", Grades: " + payload.grades().size());
            System.out.println("  Backed up at: " + payload.manifest().createdAt());
        } catch (BackupException e) {
            printBackupError(e);
        }
    }

    private Path promptBackupPath() {
        System.out.print("Enter backup filename (without extension): ");
        String filename = scanner.nextLine().trim();
        if (filename.isEmpty()) {
            System.out.println("Filename cannot be empty.");
            return null;
        }
        return BACKUP_DIR.resolve(filename + ".json");
    }

    private void printBackupError(BackupException e) {
        System.out.println("\n✗ ERROR: BackupException (" + e.getErrorCode() + ")");
        System.out.println("  " + e.getMessage());
    }
}
