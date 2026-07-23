package dataio;

import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.ExportException;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Writes a list of {@link GradeRecord}s to CSV, JSON, or Java binary - mirrors {@link StudentDataExporter}. */
public final class GradeDataExporter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void exportCsv(List<GradeRecord> grades, Path path) {
        List<String> lines = new ArrayList<>();
        lines.add("gradeId,studentId,subjectCode,grade,date");
        lines.addAll(grades.stream().map(this::toCsvRow).collect(Collectors.toList()));
        try {
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ExportException("Failed to export grades as CSV: " + e.getMessage(), path.toString(), e);
        }
    }

    private String toCsvRow(GradeRecord grade) {
        return String.join(",", grade.gradeId(), grade.studentId(), grade.subjectCode(),
                String.valueOf(grade.grade()), grade.date());
    }

    public void exportJson(List<GradeRecord> grades, Path path) {
        try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, grades);
        } catch (IOException e) {
            throw new ExportException("Failed to export grades as JSON: " + e.getMessage(), path.toString(), e);
        }
    }

    public void exportBinary(List<GradeRecord> grades, Path path) {
        try (var out = new ObjectOutputStream(Files.newOutputStream(path))) {
            out.writeObject(new ArrayList<>(grades));
        } catch (IOException e) {
            throw new ExportException("Failed to export grades as binary: " + e.getMessage(), path.toString(), e);
        }
    }
}
