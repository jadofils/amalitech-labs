package dataio;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.ImportException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reads a list of {@link StudentRecord}s back from CSV, JSON, or Java binary serialization -
 * the inverse of {@link StudentDataExporter}, and the other half of US-2's "round-trip" test:
 * exporting then re-importing must reproduce the original list exactly.
 */
public final class StudentDataImporter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** NIO.2 {@code Files.lines()} streamed straight through a map/filter/collect pipeline (US-10). */
    public List<StudentRecord> importCsv(Path path) {
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            return lines
                    .skip(1) // header
                    .filter(line -> !line.isBlank())
                    .map(this::parseCsvRow)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ImportException("Failed to import students from CSV: " + e.getMessage(), path.toString(), e);
        }
    }

    private StudentRecord parseCsvRow(String line) {
        String[] fields = line.split(",", -1);
        if (fields.length != 7) {
            throw new ImportException("Malformed CSV row (expected 7 fields, got " + fields.length + "): " + line);
        }
        return new StudentRecord(fields[0], fields[1], fields[2],
                Integer.parseInt(fields[3]), fields[4], fields[5], fields[6]);
    }

    public List<StudentRecord> importJson(Path path) {
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return objectMapper.readValue(reader, new TypeReference<List<StudentRecord>>() {
            });
        } catch (IOException e) {
            throw new ImportException("Failed to import students from JSON: " + e.getMessage(), path.toString(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<StudentRecord> importBinary(Path path) {
        try (var in = new ObjectInputStream(Files.newInputStream(path))) {
            return (List<StudentRecord>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new ImportException("Failed to import students from binary: " + e.getMessage(), path.toString(), e);
        }
    }
}
