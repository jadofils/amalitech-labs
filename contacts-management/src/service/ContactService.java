package service;

import model.Contact;

import java.util.List;

public interface ContactService {
    Contact addContact(String name, String email, String phone);
    Contact getContactById(String id);
    List<Contact> getAllContacts();
    Contact updateContact(String id, String name, String email, String phone);
    void deleteContact(String id);
}
