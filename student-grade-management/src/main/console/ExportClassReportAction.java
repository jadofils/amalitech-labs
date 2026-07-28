package main.console;

import main.dataio.GradeDataExporter;
import main.dataio.GradeRecordMapper;
import main.export.FileExporter;
import main.export.ReportGenerator;
import main.dataio.GradeRecord;
import main.manager.GradeManager;
import main.model.subject.Subject;
import main.repository.subject.SubjectRepository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * Bulk-exports grade reports for one, several, or all classes (subjects) at
 * once - the class-level counterpart to {@link ExportGradeReportAction},
 * which only ever exports a single student. Offers the same three output
 * formats: a human-readable text report (via {@link ReportGenerator}), and
 * raw CSV/JSON data dumps (via {@link main.dataio.GradeDataExporter}, the
 * same multi-format machinery {@link main.dataio.GradeDataImporter} reads
 * back on the Bulk Import side).
 */
public class ExportClassReportAction extends AbstractMenuAction {

    private final Scanner scanner;
    private final SubjectRepository subjectRepository;
    private final GradeManager gradeManager;
    private final ReportGenerator reportGenerator;
    private final FileExporter fileExporter;
    private final GradeDataExporter gradeDataExporter = new GradeDataExporter();

    public ExportClassReportAction(Scanner scanner, SubjectRepository subjectRepository, GradeManager gradeManager,
                                    ReportGenerator reportGenerator, FileExporter fileExporter) {
        super(12, "Export Class Report");
        this.scanner = scanner;
        this.subjectRepository = subjectRepository;
        this.gradeManager = gradeManager;
        this.reportGenerator = reportGenerator;
        this.fileExporter = fileExporter;
    }

    @Override
    public void execute() {
        System.out.println("\nEXPORT CLASS REPORT");
        System.out.println(ConsoleUtils.DIVIDER);

        List<Subject> subjects = subjectRepository.getAllSubjects();
        if (subjects.isEmpty()) {
            System.out.println("No classes are configured yet.");
            ConsoleUtils.promptEnter(scanner);
            return;
        }

        System.out.println("\nAvailable Classes:");
        for (int i = 0; i < subjects.size(); i++) {
            Subject s = subjects.get(i);
            System.out.printf("%d. %s (%s) - %s%n", i + 1, s.getSubjectName(), s.getSubjectCode(), s.getSubjectType());
        }

        System.out.print("\nEnter class number(s) separated by commas, or 'all': ");
        String selectionInput = scanner.nextLine().trim();
        List<Subject> selected = resolveSelection(selectionInput, subjects);
        if (selected.isEmpty()) {
            System.out.println("No valid class selected.");
            ConsoleUtils.promptEnter(scanner);
            return;
        }

        System.out.println("\nExport format:");
        System.out.println("1. Text Report");
        System.out.println("2. CSV");
        System.out.println("3. JSON");
        System.out.println("4. All formats");
        System.out.print("Select option (1-4): ");
        String formatOption = scanner.nextLine().trim();

        System.out.print("\nEnter filename prefix (without extension): ");
        String prefix = scanner.nextLine().trim();
        if (prefix.isEmpty()) {
            System.out.println("Filename cannot be empty.");
            return;
        }

        boolean text = formatOption.equals("1") || formatOption.equals("4");
        boolean csv = formatOption.equals("2") || formatOption.equals("4");
        boolean json = formatOption.equals("3") || formatOption.equals("4");

        if (csv || json) {
            fileExporter.ensureDirectory();
        }

        System.out.println("\n✓ Class report(s) exported successfully!");
        for (Subject subject : selected) {
            List<String> filesForSubject = exportOneClass(subject, prefix, text, csv, json);
            System.out.println("  " + subject.getSubjectName() + ": " + String.join(", ", filesForSubject));
        }
        System.out.println("  Location: ./" + fileExporter.getReportsDir() + "/");

        ConsoleUtils.promptEnter(scanner);
    }

    private List<String> exportOneClass(Subject subject, String prefix, boolean text, boolean csv, boolean json) {
        List<String> filesWritten = new ArrayList<>();
        String base = prefix + "_" + subject.getSubjectCode();

        if (text) {
            String content = reportGenerator.exportClassDetailed(subject);
            fileExporter.exportToFile(base + "_report.txt", content);
            filesWritten.add(base + "_report.txt");
        }
        if (csv) {
            gradeDataExporter.exportCsv(gradeRecordsFor(subject), Path.of(fileExporter.getReportsDir(), base + ".csv"));
            filesWritten.add(base + ".csv");
        }
        if (json) {
            gradeDataExporter.exportJson(gradeRecordsFor(subject), Path.of(fileExporter.getReportsDir(), base + ".json"));
            filesWritten.add(base + ".json");
        }
        return filesWritten;
    }

    private List<GradeRecord> gradeRecordsFor(Subject subject) {
        return gradeManager.getAllGrades().stream()
                .filter(g -> g.getSubject().getSubjectCode().equals(subject.getSubjectCode()))
                .map(GradeRecordMapper::toRecord)
                .toList();
    }

    /** Parses "all" or a comma-separated list of 1-based class numbers, silently skipping out-of-range/non-numeric tokens. */
    private List<Subject> resolveSelection(String input, List<Subject> subjects) {
        if (input.equalsIgnoreCase("all")) {
            return subjects;
        }

        Set<Subject> selected = new LinkedHashSet<>();
        for (String token : input.split(",")) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                int index = Integer.parseInt(trimmed) - 1;
                if (index >= 0 && index < subjects.size()) {
                    selected.add(subjects.get(index));
                }
            } catch (NumberFormatException ignored) {
                // Non-numeric token: skipped, same tolerant handling as an out-of-range number.
            }
        }
        return new ArrayList<>(selected);
    }
}
