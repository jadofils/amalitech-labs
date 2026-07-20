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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
        File file = new File(path);

        if (!file.exists()) {
            Logger.warn("Bulk import requested but file does not exist: " + path);
            throw new ImportException("File not found: " + path, path, null);
        }

        CSVParseResult parseResult = csvParser.parse(file);

        int success = 0;
        int failed = 0;
        List<String> failReasons = new ArrayList<>(parseResult.getErrors());

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
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String logFilename = "import_log_" + timestamp + ".txt";
        String logPath = "imports/" + logFilename;

        try (FileWriter writer = new FileWriter(logPath)) {
            writer.write("IMPORT LOG\n");
            writer.write("================================\n\n");
            writer.write("File: " + originalFilename + ".csv\n");
            writer.write("Date: " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()) + "\n\n");
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
