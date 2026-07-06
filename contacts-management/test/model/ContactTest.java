package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContactTest {

    @Test
    void constructorStoresAllFields() {
        Contact contact = new Contact("C001", "Ada Lovelace", "ada@example.com", "+1-555-0100");

        assertEquals("C001", contact.getId());
        assertEquals("Ada Lovelace", contact.getName());
        assertEquals("ada@example.com", contact.getEmail());
        assertEquals("+1-555-0100", contact.getPhone());
    }

    @Test
    void settersUpdateMutableFieldsButNotId() {
        Contact contact = new Contact("C001", "Ada Lovelace", "ada@example.com", "+1-555-0100");

        contact.setName("Ada King");
        contact.setEmail("ada.king@example.com");
        contact.setPhone("+1-555-0200");

        assertEquals("C001", contact.getId());
        assertEquals("Ada King", contact.getName());
        assertEquals("ada.king@example.com", contact.getEmail());
        assertEquals("+1-555-0200", contact.getPhone());
    }
}
