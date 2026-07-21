package tests.exceptions;

import exceptions.ApplicationException;
import exceptions.CSVImportException;
import exceptions.GradeException;
import exceptions.ImportException;
import exceptions.InvalidGradeException;
import exceptions.StudentNotFoundException;
import exceptions.StudentValidationException;
import exceptions.SubjectNotFoundException;
import exceptions.SubjectValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Directly exercises every custom exception's constructors and getters -
 * the pieces of the exceptions package that aren't already hit indirectly
 * through other classes' real failure paths (Grade, the repositories,
 * FileExporter, ...). Pure data classes with no collaborators - no
 * Mockito pair (see tests/README.md).
 */
class ApplicationExceptionHierarchyTest {

    @Test
    @DisplayName("Every custom exception is an unchecked ApplicationException, not a checked Exception")
    void everyCustomExceptionIsUncheckedTest() {
        ApplicationException ex = new GradeException("boom");
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    @DisplayName("StudentNotFoundException carries the student ID and available IDs through to the top-level handler")
    void studentNotFoundExceptionCarriesRecoveryDataTest() {
        List<String> available = List.of("STU001", "STU002");
        StudentNotFoundException ex = new StudentNotFoundException("not found", "STU999", available);

        assertEquals("not found", ex.getMessage());
        assertEquals("STU999", ex.getStudentId());
        assertEquals(available, ex.getAvailableIds());
    }

    @Test
    @DisplayName("StudentNotFoundException's message-only constructor leaves recovery data null")
    void studentNotFoundExceptionMessageOnlyConstructorTest() {
        StudentNotFoundException ex = new StudentNotFoundException("not found");

        assertEquals("not found", ex.getMessage());
        assertNull(ex.getStudentId());
        assertNull(ex.getAvailableIds());
    }

    @Test
    @DisplayName("StudentValidationException and SubjectValidationException carry only a message")
    void validationExceptionsCarryMessageTest() {
        assertEquals("bad student", new StudentValidationException("bad student").getMessage());
        assertEquals("bad subject", new SubjectValidationException("bad subject").getMessage());
    }

    @Test
    @DisplayName("SubjectNotFoundException carries only a message")
    void subjectNotFoundExceptionCarriesMessageTest() {
        assertEquals("no such subject", new SubjectNotFoundException("no such subject").getMessage());
    }

    @Test
    @DisplayName("GradeException's two constructors: with and without a gradeId")
    void gradeExceptionConstructorsTest() {
        GradeException withId = new GradeException("bad grade", "GRD001");
        assertEquals("GRD001", withId.getGradeId());

        GradeException withoutId = new GradeException("bad grade");
        assertNull(withoutId.getGradeId());
    }

    @Test
    @DisplayName("InvalidGradeException's two constructors: with and without the attempted value")
    void invalidGradeExceptionConstructorsTest() {
        InvalidGradeException withValue = new InvalidGradeException("out of range", 150.0);
        assertEquals(150.0, withValue.getAttemptedGrade());

        InvalidGradeException withoutValue = new InvalidGradeException("out of range");
        assertEquals(-1, withoutValue.getAttemptedGrade());
    }

    @Test
    @DisplayName("ImportException's three constructors: file-path+cause, row-counts+cause, and message-only")
    void importExceptionConstructorsTest() {
        Throwable cause = new RuntimeException("io failure");

        ImportException withPath = new ImportException("failed", "imports/bad.csv", cause);
        assertEquals("imports/bad.csv", withPath.getFilePath());
        assertEquals(0, withPath.getSuccessfulRows());
        assertEquals(0, withPath.getFailedRows());
        assertSame(cause, withPath.getCause());

        ImportException withCounts = new ImportException("partial failure", 3, 2, cause);
        assertNull(withCounts.getFilePath());
        assertEquals(3, withCounts.getSuccessfulRows());
        assertEquals(2, withCounts.getFailedRows());

        ImportException messageOnly = new ImportException("failed");
        assertNull(messageOnly.getFilePath());
        assertEquals(0, messageOnly.getSuccessfulRows());
        assertEquals(0, messageOnly.getFailedRows());
    }

    @Test
    @DisplayName("CSVImportException's two constructors: message-only and message+cause")
    void csvImportExceptionConstructorsTest() {
        CSVImportException messageOnly = new CSVImportException("cannot read file");
        assertEquals("cannot read file", messageOnly.getMessage());
        assertNull(messageOnly.getCause());

        Throwable cause = new RuntimeException("disk error");
        CSVImportException withCause = new CSVImportException("cannot read file", cause);
        assertSame(cause, withCause.getCause());
    }
}
