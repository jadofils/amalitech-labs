package repository;

import model.Contact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryContactRepositoryTest {

    private ContactRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryContactRepository();
    }

    @Test
    void findAllOnEmptyRepositoryReturnsEmptyList() {
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void saveThenFindByIdReturnsTheSameContact() {
        Contact saved = repository.save(new Contact("C001", "Ada", "ada@example.com", "555-0100"));

        Optional<Contact> found = repository.findById("C001");

        assertTrue(found.isPresent());
        assertEquals(saved.getEmail(), found.get().getEmail());
    }

    @Test
    void findByIdForUnknownIdReturnsEmptyOptional() {
        assertTrue(repository.findById("does-not-exist").isEmpty());
    }

    @Test
    void findAllReturnsExactlyEverySavedContact() {
        repository.save(new Contact("C001", "Ada", "ada@example.com", "555-0100"));
        repository.save(new Contact("C002", "Bob", "bob@example.com", "555-0200"));

        List<Contact> all = repository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void updateOverwritesTheStoredContact() {
        repository.save(new Contact("C001", "Ada", "ada@example.com", "555-0100"));

        Contact updated = new Contact("C001", "Ada King", "ada.king@example.com", "555-0999");
        repository.update(updated);

        Optional<Contact> found = repository.findById("C001");
        assertTrue(found.isPresent());
        assertEquals("Ada King", found.get().getName());
    }

    @Test
    void deleteByIdRemovesTheContact() {
        repository.save(new Contact("C001", "Ada", "ada@example.com", "555-0100"));

        repository.deleteById("C001");

        assertTrue(repository.findById("C001").isEmpty());
    }

    @Test
    void deleteByIdForUnknownIdIsANoOp() {
        repository.deleteById("does-not-exist");

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void existsByEmailIsCaseInsensitiveAndTrueOnlyForStoredEmails() {
        repository.save(new Contact("C001", "Ada", "Ada@Example.com", "555-0100"));

        assertTrue(repository.existsByEmail("ada@example.com"));
        assertFalse(repository.existsByEmail("nobody@example.com"));
    }
}
