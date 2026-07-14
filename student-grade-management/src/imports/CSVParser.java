package imports;

import exceptions.InvalidFileFormatException;
import model.subject.Subject;
import repository.subject.SubjectRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CSVParser {
    private final SubjectRepository subjectRepository;

    public CSVParser(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public CSVParseResult parse(File file) {
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
                if (parts.length != 4) {
                    errors.add("Row " + lineNum + ": Invalid format (expected 4 columns, got " + parts.length + ")");
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

                if (gradeVal < 0 || gradeVal > 100) {
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

                rows.add(new CSVRow(sid, subjName, matchedSubject, gradeVal, lineNum));
            }
        } catch (IOException e) {
            throw new InvalidFileFormatException("Failed to read CSV file: " + e.getMessage());
        }

        return new CSVParseResult(rows, errors);
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
