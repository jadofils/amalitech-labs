package tests.concurrent;

import main.concurrent.BatchReportService;
import main.concurrent.BatchReportService.BatchResult;
import main.concurrent.BatchReportService.ReportKind;
import main.concurrent.BatchReportService.StudentReportOutcome;
import main.export.FileExporter;
import main.export.ReportGenerator;
import main.manager.GradeManager;
import main.manager.StudentManager;
import main.model.grade.Grade;
import main.model.student.RegularStudent;
import main.model.student.Student;
import main.model.subject.Subject;
import main.repository.student.StudentRepositoryImpl;
import main.repository.subject.SubjectRepositoryImpl;
import main.service.GradeService;
import main.service.GradeServiceImpl;
import main.service.StudentService;
import main.service.StudentServiceImpl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Real collaborators throughout - the point of this class is proving that concurrent report
// generation over the *real*, Map-backed (PBI-1) repositories produces exactly the same output a
// sequential run would, i.e. that concurrent reads of shared student/grade storage are safe.
class BatchReportServiceTest {

    private final StudentRepositoryImpl students = new StudentRepositoryImpl();
    private final SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
    private final GradeService gradeService = new GradeServiceImpl(students, subjects);
    private final GradeManager gradeManager = new GradeManager(gradeService, subjects);
    private final StudentService studentService = new StudentServiceImpl(students);
    private final StudentManager studentManager = new StudentManager(studentService, gradeManager);
    private final ReportGenerator reportGenerator = new ReportGenerator(gradeManager, studentManager);

    private final Path tempDir = Path.of("target/test-batch-reports-" + System.nanoTime());
    private final FileExporter fileExporter = new FileExporter(tempDir.toString());

    @AfterEach
    void cleanUp() throws IOException {
        if (Files.exists(tempDir)) {
            try (var files = Files.list(tempDir)) {
                for (Path file : files.toList()) {
                    Files.deleteIfExists(file);
                }
            }
            Files.deleteIfExists(tempDir);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 8, 9})
    @DisplayName("Constructor rejects a thread count outside 2-8")
    void rejectsThreadCountOutsideRangeTest(int threadCount) {
        if (threadCount >= BatchReportService.MIN_THREADS && threadCount <= BatchReportService.MAX_THREADS) {
            return; // only the out-of-range values in this source matter for this test
        }
        assertThrows(IllegalArgumentException.class,
                () -> new BatchReportService(studentManager, reportGenerator, fileExporter, threadCount));
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 4, 8})
    @DisplayName("Constructor accepts every thread count in 2-8")
    void acceptsThreadCountInRangeTest(int threadCount) {
        assertDoesNotThrow(() -> new BatchReportService(studentManager, reportGenerator, fileExporter, threadCount));
    }

    @Test
    @DisplayName("generateBatch() produces exactly the same content a sequential call would, for every one of many concurrently-read students")
    void concurrentReadsMatchSequentialOutputTest() {
        Subject subject = subjects.getAllSubjects().get(0);
        List<String> studentIds = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            Student student = new RegularStudent("Test Student", 16, "student" + i + "@school.edu", "1234567890");
            studentManager.addStudent(student);
            gradeManager.addGrade(new Grade(student.getStudentId(), subject, 70.0 + i % 30));
            studentIds.add(student.getStudentId());
        }

        BatchReportService service = new BatchReportService(studentManager, reportGenerator, fileExporter, BatchReportService.MAX_THREADS);
        BatchResult result = service.generateBatch(studentIds, ReportKind.DETAILED, "batch_");

        assertEquals(studentIds.size(), result.totalRequested());
        assertEquals(studentIds.size(), result.succeeded());
        assertEquals(0, result.failed());
        assertEquals(BatchReportService.MAX_THREADS, result.threadPoolSize());

        for (StudentReportOutcome outcome : result.outcomes()) {
            assertTrue(outcome.success());
            String expectedContent = reportGenerator.exportDetailed(outcome.studentId());
            String actualContent = readFile(outcome.filePathOrError());
            assertEquals(expectedContent, actualContent,
                    "Concurrently-generated report for " + outcome.studentId() + " must match a fresh sequential read");
        }
    }

    @Test
    @DisplayName("An unknown student ID is recorded as a failed outcome, not thrown out of the whole batch")
    void unknownStudentIsRecordedAsFailureTest() {
        Subject subject = subjects.getAllSubjects().get(0);
        Student known = new RegularStudent("Known Student", 17, "known@school.edu", "1234567890");
        studentManager.addStudent(known);
        gradeManager.addGrade(new Grade(known.getStudentId(), subject, 88.0));

        BatchReportService service = new BatchReportService(studentManager, reportGenerator, fileExporter, BatchReportService.MIN_THREADS);
        BatchResult result = service.generateBatch(List.of(known.getStudentId(), "NOPE999"), ReportKind.SUMMARY, "batch_");

        assertEquals(2, result.totalRequested());
        assertEquals(1, result.succeeded());
        assertEquals(1, result.failed());
    }

    private String readFile(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            throw new AssertionError("Expected report file to exist at " + path, e);
        }
    }
}
