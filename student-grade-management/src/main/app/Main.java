package main.app;

import main.calculators.GPACalculator;
import main.calculators.StatisticsCalculator;
import main.console.AddStudentAction;
import main.console.BulkImportAction;
import main.console.CalculateGpaAction;
import main.console.ClassStatisticsAction;
import main.console.ExitAction;
import main.console.ExportGradeReportAction;
import main.console.MenuAction;
import main.console.RecordGradeAction;
import main.console.SearchStudentsAction;
import main.console.ViewGradeReportAction;
import main.console.ViewStudentsAction;
import main.export.FileExporter;
import main.export.ReportGenerator;
import main.imports.BulkImportService;
import main.manager.GradeManager;
import main.manager.StudentManager;
import main.manager.StudentSearcher;
import main.repository.student.StudentRepository;
import main.repository.student.StudentRepositoryImpl;
import main.repository.subject.SubjectRepository;
import main.repository.subject.SubjectRepositoryImpl;
import main.service.GradeService;
import main.service.StudentService;
import main.service.GradeServiceImpl;
import main.service.StudentServiceImpl;

import java.util.List;
import java.util.Scanner;

/**
 * Composition root: builds the full dependency graph exactly once, then
 * hands it to {@link ConsoleApp} to run. Deliberately too thin to need a
 * test of its own - every behavior it used to hold directly now lives in
 * {@code ConsoleApp}, which takes its {@link Scanner} as a constructor
 * argument instead of binding to {@code System.in} in a {@code static}
 * field, which is what made the old, single-file {@code Main} untestable.
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        StudentRepository studentRepository = new StudentRepositoryImpl();
        SubjectRepository subjectRepository = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(studentRepository, subjectRepository);
        GradeManager gradeManager = new GradeManager(gradeService, subjectRepository);
        StudentService studentService = new StudentServiceImpl(studentRepository);
        StudentManager studentManager = new StudentManager(studentService, gradeManager);
        ReportGenerator reportGenerator = new ReportGenerator(gradeManager, studentManager);
        FileExporter fileExporter = new FileExporter();
        GPACalculator gpaCalculator = new GPACalculator(gradeManager, studentManager);
        BulkImportService bulkImportService = new BulkImportService(subjectRepository, studentManager, gradeManager);
        StatisticsCalculator statisticsCalculator = new StatisticsCalculator();
        StudentSearcher studentSearcher = new StudentSearcher(studentManager);

        List<MenuAction> actions = List.of(
                new AddStudentAction(scanner, studentManager),
                new ViewStudentsAction(scanner, studentManager),
                new RecordGradeAction(scanner, studentManager, gradeManager),
                new ViewGradeReportAction(scanner, studentManager, gradeManager),
                new ExportGradeReportAction(scanner, studentManager, gradeManager, reportGenerator, fileExporter),
                new CalculateGpaAction(scanner, studentManager, gradeManager, gpaCalculator),
                new BulkImportAction(scanner, bulkImportService),
                new ClassStatisticsAction(scanner, studentManager, gradeManager, statisticsCalculator, subjectRepository),
                new SearchStudentsAction(scanner, studentManager, studentSearcher, fileExporter),
                new ExitAction()
        );

        new ConsoleApp(scanner, actions).run();
    }
}
