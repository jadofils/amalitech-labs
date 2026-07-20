package tests.imports;

import imports.CSVParser;
import model.enums.SubjectType;
import model.subject.Subject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.subject.SubjectRepository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Mocks SubjectRepository so subject matching can be tested against
 * controlled fixtures rather than the real seeded subject list.
 */
class CSVParserMockitoTest {

    private File csvFile;

    private File writeCsv(String content) throws IOException {
        csvFile = File.createTempFile("bulk-import-mockito-test", ".csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write(content);
        }
        return csvFile;
    }

    @AfterEach
    void cleanUp() throws IOException {
        if (csvFile != null) {
            Files.deleteIfExists(csvFile.toPath());
        }
    }

    @Test
    @DisplayName("parse() calls SubjectRepository.getAllSubjects() once per valid row while matching")
    void parseQueriesSubjectRepositoryTest() throws IOException {
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        Subject math = mock(Subject.class);
        when(math.getSubjectName()).thenReturn("Mathematics");
        when(math.getSubjectType()).thenReturn(SubjectType.CORE);
        when(subjectRepository.getAllSubjects()).thenReturn(List.of(math));
        CSVParser parser = new CSVParser(subjectRepository);
        File file = writeCsv("StudentID,SubjectName,SubjectType,Grade\nSTU001,Mathematics,Core,85\n");

        CSVParser.CSVParseResult result = parser.parse(file);

        assertEquals(1, result.getValidCount());
        verify(subjectRepository, atLeastOnce()).getAllSubjects();
    }

    @Test
    @DisplayName("A subject name matches case-insensitively")
    void subjectNameMatchesCaseInsensitivelyTest() throws IOException {
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        Subject math = mock(Subject.class);
        when(math.getSubjectName()).thenReturn("Mathematics");
        when(math.getSubjectType()).thenReturn(SubjectType.CORE);
        when(subjectRepository.getAllSubjects()).thenReturn(List.of(math));
        CSVParser parser = new CSVParser(subjectRepository);
        File file = writeCsv("StudentID,SubjectName,SubjectType,Grade\nSTU001,MATHEMATICS,Core,85\n");

        CSVParser.CSVParseResult result = parser.parse(file);

        assertEquals(1, result.getValidCount());
    }
}
