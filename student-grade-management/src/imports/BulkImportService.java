package imports;

import exceptions.ImportException;
import imports.CSVParser.CSVParseResult;
import imports.CSVParser.CSVRow;
import logging.Logger;
import manager.GradeManager;
import manager.StudentManager;
import model.grade.Grade;
import model.student.Student;
import repository.subject.SubjectRepository;
import utils.DateFormats;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Imports grades in bulk from a CSV file under {@code imports/}, skipping
 * (not aborting on) invalid rows, and writes a log file summarizing the run.
 */
public class BulkImportService {
    private final CSVParser csvParser;
    private final StudentManager studentManager;
    private final GradeManager gradeManager;

    public BulkImportService(SubjectRepository subjectRepository,
                              StudentManager studentManager,
                              GradeManager gradeManager) {
        this.csvParser = new CSVParser(subjectRepository);
        this.studentManager = studentManager;
        this.gradeManager = gradeManager;
    }

    public ImportResult importFromFile(String filename) {
        String path = "imports/" + filename + ".csv";
        Path file = Path.of(path);

        if (!Files.exists(file)) {
            Logger.warn("Bulk import requested but file does not exist: " + path);
            throw new ImportException("File not found: " + path, path, null);
        }

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

        String logFilename = writeImportLog(filename, success, failed, success + failed, failReasons);
        Logger.info("Bulk import of " + path + " complete: " + success + " succeeded, " + failed + " failed");

        return new ImportResult(parseResult.getValidCount(), success, failed, failReasons, logFilename);
    }

    private String writeImportLog(String originalFilename, int success, int failed,
                                   int total, List<String> failReasons) {
        String timestamp = DateFormats.now(DateFormats.FILE_SAFE_TIMESTAMP);
        String logFilename = "import_log_" + timestamp + ".txt";
        String logPath = "imports/" + logFilename;

        StringBuilder content = new StringBuilder();
        content.append("IMPORT LOG\n");
        content.append("================================\n\n");
        content.append("File: ").append(originalFilename).append(".csv\n");
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
