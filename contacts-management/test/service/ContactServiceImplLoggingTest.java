package service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.ContactRepository;
import repository.InMemoryContactRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ContactServiceImplLoggingTest {

    private ContactService contactService;
    private Logger logger;
    private CapturingHandler handler;

    @BeforeEach
    void setUp() {
        ContactRepository contactRepository = new InMemoryContactRepository();
        contactService = new ContactServiceImpl(contactRepository);

        logger = Logger.getLogger(ContactServiceImpl.class.getName());
        handler = new CapturingHandler();
        logger.setLevel(Level.ALL);
        logger.addHandler(handler);
    }

    @AfterEach
    void tearDown() {
        logger.removeHandler(handler);
    }

    @Test
    void addContactSuccessLogsAnInfoRecord() {
        contactService.addContact("Ada Lovelace", "ada@example.com", "555-0100");

        assertTrue(handler.records.stream().anyMatch(r -> r.getLevel() == Level.INFO));
    }

    @Test
    void addContactValidationFailureLogsAWarningRecord() {
        try {
            contactService.addContact("", "ada@example.com", "555-0100");
        } catch (RuntimeException expected) {
            // acceptance criteria for the validation itself are covered by ContactServiceImplTest;
            // this test only asserts the logging side-effect.
        }

        assertTrue(handler.records.stream().anyMatch(r -> r.getLevel() == Level.WARNING));
    }

    @Test
    void getContactByIdNotFoundLogsAWarningRecord() {
        try {
            contactService.getContactById("does-not-exist");
        } catch (RuntimeException expected) {
            // acceptance criteria for not-found itself are covered by ContactServiceImplTest;
            // this test only asserts the logging side-effect.
        }

        assertTrue(handler.records.stream().anyMatch(r -> r.getLevel() == Level.WARNING));
    }

    private static class CapturingHandler extends Handler {
        final List<LogRecord> records = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    }
}
