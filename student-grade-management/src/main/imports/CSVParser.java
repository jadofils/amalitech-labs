package main.imports;

import main.exceptions.CSVImportException;
import main.logging.Logger;
import main.model.enums.SubjectType;
import main.model.subject.Subject;
import main.repository.subject.SubjectRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Parses a bulk-import CSV ({@code StudentID,SubjectName,SubjectType,Grade})
 * into valid rows plus a list of per-row error messages - an invalid row is
 * skipped and recorded as an error, never thrown, so one bad row doesn't
 * abort the whole file (per US-4/PBI-4's "skip invalid rows but continue
 * processing").
 */
public class CSVParser {
    private static final int EXPECTED_COLUMN_COUNT = 4;
    private static final double MIN_GRADE = 0;
    private static final double MAX_GRADE = 100;

    private final SubjectRepository subjectRepository;

    public CSVParser(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    /**
     * @throws CSVImportException if the file itself cannot be read (missing,
     *                             permissions, ...) - unlike a malformed row,
     *                             which is collected in the result instead
     */
    public CSVParseResult parse(File file) {
        Logger.debug("Parsing CSV file: " + file.getPath());
        List<CSVRow> rows = new ArrayList<>();
        int lineNum = 0;
        List<String> errors = new ArrayList<>();

        try (Scanner fileScanner = new Scanner(file)) {
            if (fileScanner.hasNextLine()) {
                fileScanner.nextLine();
                lineNum++;
            }

            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                lineNum++;

                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length != EXPECTED_COLUMN_COUNT) {
                    errors.add("Row " + lineNum + ": Invalid format (expected "
                            + EXPECTED_COLUMN_COUNT + " columns, got " + parts.length + ")");
                    continue;
                }

                String sid = parts[0].trim();
                String subjName = parts[1].trim();
                String subjTypeStr = parts[2].trim();
                String gradeStr = parts[3].trim();

                if (sid.isEmpty() || subjName.isEmpty() || subjTypeStr.isEmpty() || gradeStr.isEmpty()) {
                    errors.add("Row " + lineNum + ": Empty field");
                    continue;
                }

                double gradeVal;
                try {
                    gradeVal = Double.parseDouble(gradeStr);
                } catch (NumberFormatException e) {
                    errors.add("Row " + lineNum + ": Invalid grade number (" + gradeStr + ")");
                    continue;
                }

                if (gradeVal < MIN_GRADE || gradeVal > MAX_GRADE) {
                    errors.add("Row " + lineNum + ": Grade out of range (" + (int) gradeVal + ")");
                    continue;
                }

                Subject matchedSubject = null;
                for (Subject subj : subjectRepository.getAllSubjects()) {
                    if (subj.getSubjectName().equalsIgnoreCase(subjName)) {
                        matchedSubject = subj;
                        break;
                    }
                }

                if (matchedSubject == null) {
                    errors.add("Row " + lineNum + ": Unknown subject (" + subjName + ")");
                    continue;
                }

                // The CSV's own SubjectType column was previously read and
                // discarded - a row could claim "Elective" for a Core
                // subject and still import silently (CHANGELOG.md KI-10).
                SubjectType declaredType = parseSubjectType(subjTypeStr);
                if (declaredType == null) {
                    errors.add("Row " + lineNum + ": Unknown subject type (" + subjTypeStr + ")");
                    continue;
                }
                if (declaredType != matchedSubject.getSubjectType()) {
                    errors.add("Row " + lineNum + ": Subject type mismatch - " + subjName + " is "
                            + matchedSubject.getSubjectType() + ", not " + declaredType);
                    continue;
                }

                rows.add(new CSVRow(sid, subjName, matchedSubject, gradeVal, lineNum));
            }
        } catch (IOException e) {
            Logger.error("Failed to read CSV file: " + file.getPath(), e);
            throw new CSVImportException("Failed to read CSV file: " + e.getMessage(), e);
        }

        Logger.info("Parsed " + file.getPath() + ": " + rows.size() + " valid row(s), " + errors.size() + " error(s)");
        return new CSVParseResult(rows, errors);
    }

    private SubjectType parseSubjectType(String raw) {
        try {
            return SubjectType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static class CSVRow {
        private final String studentId;
        private final String subjectName;
        private final Subject subject;
        private final double grade;
        private final int lineNumber;

        public CSVRow(String studentId, String subjectName, Subject subject, double grade, int lineNumber) {
            this.studentId = studentId;
            this.subjectName = subjectName;
            this.subject = subject;
            this.grade = grade;
            this.lineNumber = lineNumber;
        }

        public String getStudentId() { return studentId; }
        public String getSubjectName() { return subjectName; }
        public Subject getSubject() { return subject; }
        public double getGrade() { return grade; }
        public int getLineNumber() { return lineNumber; }
    }

    public static class CSVParseResult {
        private final List<CSVRow> validRows;
        private final List<String> errors;

        public CSVParseResult(List<CSVRow> validRows, List<String> errors) {
            this.validRows = validRows;
            this.errors = errors;
        }

        public List<CSVRow> getValidRows() { return validRows; }
        public List<String> getErrors() { return errors; }
        public int getTotalRows() { return validRows.size() + errors.size(); }
        public int getValidCount() { return validRows.size(); }
        public int getErrorCount() { return errors.size(); }
    }
}
