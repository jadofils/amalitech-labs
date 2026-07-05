package service;

import model.Contact;
import repository.ContactRepository;

import java.util.List;

public class ContactServiceImpl implements ContactService {
    private final ContactRepository contactRepository;

    public ContactServiceImpl(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Override
    public Contact addContact(String name, String email, String phone) {
        throw new UnsupportedOperationException("addContact is not implemented yet (see PBI-1)");
    }

    @Override
    public Contact getContactById(String id) {
        throw new UnsupportedOperationException("getContactById is not implemented yet (see PBI-3)");
    }

    @Override
    public List<Contact> getAllContacts() {
        throw new UnsupportedOperationException("getAllContacts is not implemented yet (see PBI-2)");
    }

    @Override
    public Contact updateContact(String id, String name, String email, String phone) {
        throw new UnsupportedOperationException("updateContact is not implemented yet (see PBI-4)");
    }

    @Override
    public void deleteContact(String id) {
        throw new UnsupportedOperationException("deleteContact is not implemented yet (see PBI-5)");
    }
}
