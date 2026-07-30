package main.imports;

import main.dataio.GradeDataImporter;
import main.dataio.GradeRecord;
import main.dataio.GradeRecordMapper;
import main.exceptions.ApplicationException;
import main.exceptions.ImportException;
import main.imports.CSVParser.CSVParseResult;
import main.imports.CSVParser.CSVRow;
import main.logging.Logger;
import main.manager.GradeManager;
import main.manager.StudentManager;
import main.model.grade.Grade;
import main.model.student.Student;
import main.repository.subject.SubjectRepository;
import main.utils.DateFormats;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Imports grades in bulk from a file under {@code imports/}, skipping
 * (not aborting on) invalid rows/records, and writes a log file summarizing
 * the run. Accepts a base filename (no extension) and auto-detects the
 * format by probing for {@code .csv}, {@code .json}, then {@code .dat}
 * (Java binary serialization) in that order - the CSV path is the original,
 * hand-editable format; JSON/binary are meant for re-importing a file this
 * app previously produced via {@link main.dataio.GradeDataExporter}.
 */
public class BulkImportService {
    private static final List<String> SUPPORTED_EXTENSIONS = List.of("csv", "json", "dat");
    private static final String IMPORTS_DIR = "imports/";

    private final CSVParser csvParser;
    private final GradeDataImporter gradeDataImporter;
    private final SubjectRepository subjectRepository;
    private final StudentManager studentManager;
    private final GradeManager gradeManager;

    public BulkImportService(SubjectRepository subjectRepository,
                              StudentManager studentManager,
                              GradeManager gradeManager) {
        this.csvParser = new CSVParser(subjectRepository);
        this.gradeDataImporter = new GradeDataImporter();
        this.subjectRepository = subjectRepository;
        this.studentManager = studentManager;
        this.gradeManager = gradeManager;
    }

    public ImportResult importFromFile(String filename) {
        for (String extension : SUPPORTED_EXTENSIONS) {
            Path candidate = Path.of(IMPORTS_DIR + filename + "." + extension);
            if (Files.exists(candidate)) {
                return importFromFile(filename, extension, candidate);
            }
        }

        String path = IMPORTS_DIR + filename + ".csv";
        String resolvedPath = Path.of(path).toAbsolutePath().normalize().toString();
        Logger.warn("Bulk import requested but file does not exist: " + resolvedPath);
        throw new ImportException("File not found: " + resolvedPath, resolvedPath, null);
    }

    private ImportResult importFromFile(String filename, String extension, Path file) {
        return switch (extension) {
            case "csv" -> importCsv(filename, file);
            case "json" -> importRecords(filename, extension, gradeDataImporter.importJson(file));
            case "dat" -> importRecords(filename, extension, gradeDataImporter.importBinary(file));
            default -> throw new ImportException("Unsupported file type: ." + extension);
        };
    }

    private ImportResult importCsv(String filename, Path file) {
        CSVParseResult parseResult = csvParser.parse(file);

        int success = 0;
        List<String> failReasons = new ArrayList<>(parseResult.getErrors());
        int failed = failReasons.size();

        for (CSVRow row : parseResult.getValidRows()) {
            Student student = studentManager.findStudent(row.getStudentId());
            if (student == null) {
                failed++;
                failReasons.add("Row " + row.getLineNumber() + ": Invalid student ID (" + row.getStudentId() + ")");
                continue;
            }

            Grade grade = new Grade(row.getStudentId(), row.getSubject(), row.getGrade());
            gradeManager.addGrade(grade);
            success++;
        }

        String logFilename = writeImportLog(filename, "csv", success, failed, success + failed, failReasons);
        Logger.info("Bulk import of " + IMPORTS_DIR + filename + ".csv complete: " + success + " succeeded, " + failed + " failed");

        return new ImportResult(parseResult.getValidCount(), success, failed, failReasons, logFilename);
    }

    /** Shared by the JSON and binary formats: both deserialize to the same flat {@link GradeRecord} list. */
    private ImportResult importRecords(String filename, String extension, List<GradeRecord> records) {
        int success = 0;
        List<String> failReasons = new ArrayList<>();

        int position = 1;
        for (GradeRecord gradeRecord : records) {
            Student student = studentManager.findStudent(gradeRecord.studentId());
            if (student == null) {
                failReasons.add("Record " + position + ": Invalid student ID (" + gradeRecord.studentId() + ")");
                position++;
                continue;
            }
            try {
                Grade grade = GradeRecordMapper.toGrade(gradeRecord, subjectRepository);
                gradeManager.addGrade(grade);
                success++;
            } catch (ApplicationException e) {
                failReasons.add("Record " + position + ": " + e.getMessage());
            }
            position++;
        }

        int failed = failReasons.size();
        String logFilename = writeImportLog(filename, extension, success, failed, success + failed, failReasons);
        Logger.info("Bulk import of " + IMPORTS_DIR + filename + "." + extension + " complete: "
                + success + " succeeded, " + failed + " failed");

        return new ImportResult(records.size(), success, failed, failReasons, logFilename);
    }

    private String writeImportLog(String originalFilename, String extension, int success, int failed,
                                   int total, List<String> failReasons) {
        String timestamp = DateFormats.now(DateFormats.FILE_SAFE_TIMESTAMP);
        String logFilename = "import_log_" + timestamp + ".txt";
        String logPath = IMPORTS_DIR + logFilename;

        StringBuilder content = new StringBuilder();
        content.append("IMPORT LOG\n");
        content.append("================================\n\n");
        content.append("File: ").append(originalFilename).append(".").append(extension).append("\n");
        content.append("Date: ").append(DateFormats.now(DateFormats.DISPLAY_DATE_TIME)).append("\n\n");
        content.append("Total Rows: ").append(total).append("\n");
        content.append("Successfully Imported: ").append(success).append("\n");
        content.append("Failed: ").append(failed).append("\n\n");

        if (!failReasons.isEmpty()) {
            content.append("FAILED RECORDS\n");
            content.append("--------------------------------\n");
            for (String reason : failReasons) {
                content.append(reason).append("\n");
            }
        }

        try {
            Files.writeString(Path.of(logPath), content.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // The import itself already succeeded/failed by this point, so
            // a log-write failure doesn't roll anything back - but it must
            // not vanish silently either (CHANGELOG.md KI-3).
            Logger.error("Failed to write import log file: " + logPath, e);
        }

        return logFilename;
    }

    public static class ImportResult {
        private final int totalParsed;
        private final int successCount;
        private final int failedCount;
        private final List<String> failReasons;
        private final String logFilename;

        public ImportResult(int totalParsed, int successCount, int failedCount,
                            List<String> failReasons, String logFilename) {
            this.totalParsed = totalParsed;
            this.successCount = successCount;
            this.failedCount = failedCount;
            this.failReasons = failReasons;
            this.logFilename = logFilename;
        }

        public int getTotalParsed() { return totalParsed; }
        public int getSuccessCount() { return successCount; }
        public int getFailedCount() { return failedCount; }
        public List<String> getFailReasons() { return failReasons; }
        public String getLogFilename() { return logFilename; }
    }
}
