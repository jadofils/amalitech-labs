package service;

import exceptions.ContactValidationException;
import model.Contact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.ContactRepository;
import repository.InMemoryContactRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContactServiceImplTest {

    private ContactService contactService;

    @BeforeEach
    void setUp() {
        ContactRepository contactRepository = new InMemoryContactRepository();
        contactService = new ContactServiceImpl(contactRepository);
    }

    @Test
    void addContactWithValidDataReturnsAStoredContactWithAGeneratedId() {
        Contact contact = contactService.addContact("Ada Lovelace", "ada@example.com", "555-0100");

        assertNotNull(contact.getId());
        assertEquals("Ada Lovelace", contact.getName());
        assertEquals("ada@example.com", contact.getEmail());
        assertEquals("555-0100", contact.getPhone());
    }

    @Test
    void addContactGeneratesADifferentIdForEachContact() {
        Contact first = contactService.addContact("Ada Lovelace", "ada@example.com", "555-0100");
        Contact second = contactService.addContact("Bob Smith", "bob@example.com", "555-0200");

        assertEquals(false, first.getId().equals(second.getId()));
    }

    @Test
    void addContactWithBlankNameThrowsValidationException() {
        assertThrows(ContactValidationException.class,
                () -> contactService.addContact("", "ada@example.com", "555-0100"));
    }

    @Test
    void addContactWithMalformedEmailThrowsValidationException() {
        assertThrows(ContactValidationException.class,
                () -> contactService.addContact("Ada Lovelace", "not-an-email", "555-0100"));
    }

    @Test
    void getAllContactsOnAFreshServiceReturnsAnEmptyList() {
        List<Contact> all = contactService.getAllContacts();

        assertTrue(all.isEmpty());
    }

    @Test
    void getAllContactsReturnsExactlyEveryAddedContact() {
        contactService.addContact("Ada Lovelace", "ada@example.com", "555-0100");
        contactService.addContact("Bob Smith", "bob@example.com", "555-0200");

        List<Contact> all = contactService.getAllContacts();

        assertEquals(2, all.size());
    }
}
