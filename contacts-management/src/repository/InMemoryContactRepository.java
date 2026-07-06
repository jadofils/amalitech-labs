package repository;

import model.Contact;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryContactRepository implements ContactRepository {
    private final Map<String, Contact> contacts = new ConcurrentHashMap<>();

    @Override
    public Contact save(Contact contact) {
        contacts.put(contact.getId(), contact);
        return contact;
    }

    @Override
    public Optional<Contact> findById(String id) {
        return Optional.ofNullable(contacts.get(id));
    }

    @Override
    public List<Contact> findAll() {
        return new ArrayList<>(contacts.values());
    }

    @Override
    public Contact update(Contact contact) {
        contacts.put(contact.getId(), contact);
        return contact;
    }

    @Override
    public void deleteById(String id) {
        contacts.remove(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return contacts.values().stream()
                .anyMatch(c -> c.getEmail() != null && c.getEmail().equalsIgnoreCase(email));
    }
}
