package app;

import calculators.GPACalculator;
import calculators.StatisticsCalculator;
import console.AddStudentAction;
import console.BulkImportAction;
import console.CalculateGpaAction;
import console.ClassStatisticsAction;
import console.ExitAction;
import console.ExportGradeReportAction;
import console.MenuAction;
import console.RecordGradeAction;
import console.SearchStudentsAction;
import console.ViewGradeReportAction;
import console.ViewStudentsAction;
import export.FileExporter;
import export.ReportGenerator;
import imports.BulkImportService;
import manager.GradeManager;
import manager.StudentManager;
import manager.StudentSearcher;
import repository.student.StudentRepository;
import repository.student.StudentRepositoryImpl;
import repository.subject.SubjectRepository;
import repository.subject.SubjectRepositoryImpl;
import service.GradeService;
import service.StudentService;
import service.GradeServiceImpl;
import service.StudentServiceImpl;

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
