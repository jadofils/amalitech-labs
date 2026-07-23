package main.imports;

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Imports grades in bulk from a CSV file under {@code main.imports/}, skipping
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
        String path = "main.imports/" + filename + ".csv";
        File file = new File(path);

        if (!file.exists()) {
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
        String logPath = "main.imports/" + logFilename;

        try (FileWriter writer = new FileWriter(logPath)) {
            writer.write("IMPORT LOG\n");
            writer.write("================================\n\n");
            writer.write("File: " + originalFilename + ".csv\n");
            writer.write("Date: " + DateFormats.now(DateFormats.DISPLAY_DATE_TIME) + "\n\n");
            writer.write("Total Rows: " + total + "\n");
            writer.write("Successfully Imported: " + success + "\n");
            writer.write("Failed: " + failed + "\n\n");

            if (!failReasons.isEmpty()) {
                writer.write("FAILED RECORDS\n");
                writer.write("--------------------------------\n");
                for (String reason : failReasons) {
                    writer.write(reason + "\n");
                }
            }
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
