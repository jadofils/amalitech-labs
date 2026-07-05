package service;

import model.Contact;
import repository.ContactRepository;
import validation.ContactValidator;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ContactServiceImpl implements ContactService {
    private final ContactRepository contactRepository;
    private final AtomicInteger idCounter = new AtomicInteger(0);

    public ContactServiceImpl(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Override
    public Contact addContact(String name, String email, String phone) {
        ContactValidator.validateName(name);
        ContactValidator.validateEmail(email);

        String id = String.format("C%03d", idCounter.incrementAndGet());
        Contact contact = new Contact(id, name, email, phone);
        return contactRepository.save(contact);
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
