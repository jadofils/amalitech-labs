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

/**
 * Writes a list of {@link StudentRecord}s to CSV, JSON, or Java binary serialization - the three
 * formats US-2 asks for. NIO.2 ({@link Path}/{@link Files}) throughout instead of the
 * {@code java.io.FileWriter} the v2 {@link export.FileExporter} uses; kept as a separate class
 * rather than added onto {@code FileExporter}, since that class's job is writing already-formatted
 * human-readable report *text*, not serializing structured data - a different responsibility, and
 * a different reason to change.
 */
public final class StudentDataExporter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** CSV, one header row plus one row per record - a {@code Stream} pipeline builds each line. */
    public void exportCsv(List<StudentRecord> students, Path path) {
        List<String> lines = new ArrayList<>();
        lines.add("studentId,name,studentType,age,email,phone,status");
        lines.addAll(students.stream().map(this::toCsvRow).collect(Collectors.toList()));
        try {
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ExportException("Failed to export students as CSV: " + e.getMessage(), path.toString(), e);
        }
    }

    private String toCsvRow(StudentRecord student) {
        return String.join(",",
                student.studentId(), student.name(), student.studentType(),
                String.valueOf(student.age()), student.email(), student.phone(), student.status());
    }

    /** JSON array, pretty-printed for readability, via Jackson. */
    public void exportJson(List<StudentRecord> students, Path path) {
        try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, students);
        } catch (IOException e) {
            throw new ExportException("Failed to export students as JSON: " + e.getMessage(), path.toString(), e);
        }
    }

    /** Java binary serialization of the whole list in one object graph. */
    public void exportBinary(List<StudentRecord> students, Path path) {
        try (var out = new ObjectOutputStream(Files.newOutputStream(path))) {
            out.writeObject(new ArrayList<>(students));
        } catch (IOException e) {
            throw new ExportException("Failed to export students as binary: " + e.getMessage(), path.toString(), e);
        }
    }
}
