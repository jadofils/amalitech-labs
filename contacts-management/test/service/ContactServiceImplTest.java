package service;

import exceptions.ContactNotFoundException;
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

    @Test
    void getContactByIdForAnExistingIdReturnsThatContact() {
        Contact added = contactService.addContact("Ada Lovelace", "ada@example.com", "555-0100");

        Contact found = contactService.getContactById(added.getId());

        assertEquals(added.getId(), found.getId());
        assertEquals("Ada Lovelace", found.getName());
    }

    @Test
    void getContactByIdForAnUnknownIdThrowsNotFoundException() {
        assertThrows(ContactNotFoundException.class, () -> contactService.getContactById("does-not-exist"));
    }

    @Test
    void updateContactWithValidDataChangesFieldsButKeepsTheSameId() {
        Contact added = contactService.addContact("Ada Lovelace", "ada@example.com", "555-0100");

        Contact updated = contactService.updateContact(added.getId(), "Ada King", "ada.king@example.com", "555-0200");

        assertEquals(added.getId(), updated.getId());
        assertEquals("Ada King", updated.getName());
        assertEquals("ada.king@example.com", updated.getEmail());
        assertEquals("555-0200", updated.getPhone());
    }

    @Test
    void updateContactForAnUnknownIdThrowsNotFoundException() {
        assertThrows(ContactNotFoundException.class,
                () -> contactService.updateContact("does-not-exist", "Ada King", "ada.king@example.com", "555-0200"));
    }

    @Test
    void updateContactWithInvalidDataLeavesTheOriginalContactUnchanged() {
        Contact added = contactService.addContact("Ada Lovelace", "ada@example.com", "555-0100");

        assertThrows(ContactValidationException.class,
                () -> contactService.updateContact(added.getId(), "Ada Lovelace", "not-an-email", "555-0200"));

        Contact stillOriginal = contactService.getContactById(added.getId());
        assertEquals("ada@example.com", stillOriginal.getEmail());
        assertEquals("555-0100", stillOriginal.getPhone());
    }
}
