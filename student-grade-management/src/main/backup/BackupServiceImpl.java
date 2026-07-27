package main.backup;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import main.dataio.GradeRecord;
import main.dataio.StudentRecord;
import main.exceptions.BackupErrorCode;
import main.exceptions.BackupException;
import main.utils.DateFormats;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class BackupServiceImpl implements BackupService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void createBackup(Path path, List<StudentRecord> students, List<GradeRecord> grades) throws BackupException {
        BackupManifest manifest = new BackupManifest(CURRENT_VERSION, DateFormats.now(DateFormats.LOG_TIMESTAMP),
                students.size(), grades.size());
        BackupPayload payload = new BackupPayload(manifest, students, grades);
        try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, payload);
        } catch (IOException e) {
            throw new BackupException("Failed to write backup: " + e.getMessage(), BackupErrorCode.IO_FAILURE,
                    path.toString(), e);
        }
    }

    @Override
    public BackupPayload restoreBackup(Path path) throws BackupException {
        BackupPayload payload;
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            payload = objectMapper.readValue(reader, BackupPayload.class);
        } catch (JsonParseException | JsonMappingException e) {
            throw new BackupException("Backup file is corrupt: " + e.getMessage(), BackupErrorCode.CORRUPT_FILE,
                    path.toString(), e);
        } catch (IOException e) {
            throw new BackupException("Failed to read backup: " + e.getMessage(), BackupErrorCode.IO_FAILURE,
                    path.toString(), e);
        }
        if (payload.manifest() == null || payload.manifest().version() != CURRENT_VERSION) {
            String found = payload.manifest() == null ? "none" : String.valueOf(payload.manifest().version());
            throw new BackupException("Unsupported backup version: " + found, BackupErrorCode.VERSION_MISMATCH,
                    path.toString());
        }
        return payload;
    }
}
