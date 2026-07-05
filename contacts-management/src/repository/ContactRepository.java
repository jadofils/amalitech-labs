package repository;

import model.Contact;

import java.util.List;
import java.util.Optional;

public interface ContactRepository {
    Contact save(Contact contact);
    Optional<Contact> findById(String id);
    List<Contact> findAll();
    Contact update(Contact contact);
    void deleteById(String id);
    boolean existsByEmail(String email);
}
