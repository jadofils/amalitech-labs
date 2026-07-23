package tests.subject;

import main.model.enums.SubjectType;
import main.model.subject.CoreSubject;
import main.model.subject.ElectiveSubject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class SubjectTest {

    @Test
    @DisplayName("Core subject reports type CORE and is mandatory")
    void coreSubjectTypeAndMandatoryTest() {
        CoreSubject subject = new CoreSubject("Mathematics", "MATH01");
        assertEquals(SubjectType.CORE, subject.getSubjectType());
        assertTrue(subject.isMandatory());
    }

    @Test
    @DisplayName("Elective subject reports type ELECTIVE and is not mandatory")
    void electiveSubjectTypeAndMandatoryTest() {
        ElectiveSubject subject = new ElectiveSubject("Music", "MUSC01");
        assertEquals(SubjectType.ELECTIVE, subject.getSubjectType());
        assertFalse(subject.isMandatory());
    }

    @Test
    @DisplayName("Name and code getters/setters round-trip")
    void gettersAndSettersTest() {
        CoreSubject subject = new CoreSubject("Mathematics", "MATH01");
        subject.setSubjectName("Advanced Mathematics");
        subject.setSubjectCode("MATH02");
        assertEquals("Advanced Mathematics", subject.getSubjectName());
        assertEquals("MATH02", subject.getSubjectCode());
    }

    @Test
    @DisplayName("Core subject details include the word 'Core'")
    void coreSubjectDisplayTest() {
        CoreSubject subject = new CoreSubject("Mathematics", "MATH01");
        String output = captureStdOut(subject::displaySubjectDetails);
        assertTrue(output.contains("Core Subject"));
        assertTrue(output.contains("Mathematics"));
        assertTrue(output.contains("MATH01"));
    }

    @Test
    @DisplayName("Elective subject details include the word 'Elective'")
    void electiveSubjectDisplayTest() {
        ElectiveSubject subject = new ElectiveSubject("Music", "MUSC01");
        String output = captureStdOut(subject::displaySubjectDetails);
        assertTrue(output.contains("Elective Subject"));
        assertTrue(output.contains("Music"));
        assertTrue(output.contains("MUSC01"));
    }

    private String captureStdOut(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));
        try {
            action.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString();
    }
}
