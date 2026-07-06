package service;

import exceptions.ContactNotFoundException;
import exceptions.ContactValidationException;
import model.Contact;
import repository.ContactRepository;
import validation.ContactValidator;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class ContactServiceImpl implements ContactService {
    private static final Logger LOGGER = Logger.getLogger(ContactServiceImpl.class.getName());

    private final ContactRepository contactRepository;
    private final AtomicInteger idCounter = new AtomicInteger(0);

    public ContactServiceImpl(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Override
    public Contact addContact(String name, String email, String phone) {
        try {
            ContactValidator.validateName(name);
            ContactValidator.validateEmail(email);
        } catch (ContactValidationException e) {
            LOGGER.warning("Rejected addContact: " + e.getMessage());
            throw e;
        }

        String id = String.format("C%03d", idCounter.incrementAndGet());
        Contact contact = new Contact(id, name, email, phone);
        contactRepository.save(contact);
        LOGGER.info("Added contact " + id);
        return contact;
    }

    @Override
    public Contact getContactById(String id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.warning("Contact not found: " + id);
                    return new ContactNotFoundException("Contact with ID " + id + " not found.");
                });
    }

    @Override
    public List<Contact> getAllContacts() {
        return contactRepository.findAll();
    }

    @Override
    public Contact updateContact(String id, String name, String email, String phone) {
        throw new UnsupportedOperationException("updateContact is not implemented yet (see PBI-4)");
    }

    @Override
    public void deleteContact(String id) {
        contactRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.warning("Contact not found: " + id);
                    return new ContactNotFoundException("Contact with ID " + id + " not found.");
                });

        contactRepository.deleteById(id);
        LOGGER.info("Deleted contact " + id);
    }
}
