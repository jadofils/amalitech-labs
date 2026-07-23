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

/** Reads a list of {@link GradeRecord}s back from CSV, JSON, or Java binary - mirrors {@link StudentDataImporter}. */
public final class GradeDataImporter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<GradeRecord> importCsv(Path path) {
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            return lines
                    .skip(1) // header
                    .filter(line -> !line.isBlank())
                    .map(this::parseCsvRow)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ImportException("Failed to import grades from CSV: " + e.getMessage(), path.toString(), e);
        }
    }

    private GradeRecord parseCsvRow(String line) {
        String[] fields = line.split(",", -1);
        if (fields.length != 5) {
            throw new ImportException("Malformed CSV row (expected 5 fields, got " + fields.length + "): " + line);
        }
        return new GradeRecord(fields[0], fields[1], fields[2], Double.parseDouble(fields[3]), fields[4]);
    }

    public List<GradeRecord> importJson(Path path) {
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return objectMapper.readValue(reader, new TypeReference<List<GradeRecord>>() {
            });
        } catch (IOException e) {
            throw new ImportException("Failed to import grades from JSON: " + e.getMessage(), path.toString(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<GradeRecord> importBinary(Path path) {
        try (var in = new ObjectInputStream(Files.newInputStream(path))) {
            return (List<GradeRecord>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new ImportException("Failed to import grades from binary: " + e.getMessage(), path.toString(), e);
        }
    }
}
