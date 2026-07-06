import exceptions.ContactNotFoundException;
import exceptions.ContactValidationException;
import model.Contact;
import repository.ContactRepository;
import repository.InMemoryContactRepository;
import service.ContactService;
import service.ContactServiceImpl;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final ContactRepository contactRepository = new InMemoryContactRepository();
    private static final ContactService contactService = new ContactServiceImpl(contactRepository);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n===== Contacts Management Menu =====");
            System.out.println("1. Add Contact");
            System.out.println("2. List All Contacts");
            System.out.println("3. Get Contact by ID");
            System.out.println("4. Update Contact");
            System.out.println("5. Delete Contact");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            try {
                switch (choice) {
                    case 1 -> addContact();
                    case 2 -> listAllContacts();
                    case 3 -> getContactById();
                    case 4 -> updateContact();
                    case 5 -> deleteContact();
                    case 6 -> {
                        System.out.println("Exiting...");
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (ContactValidationException | ContactNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void addContact() {
        System.out.print("Enter name: ");
        String name = scanner.nextLine();

        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        System.out.print("Enter phone: ");
        String phone = scanner.nextLine();

        Contact contact = contactService.addContact(name, email, phone);
        System.out.println("\n✓ Contact added successfully!");
        System.out.println(contact);
    }

    private static void listAllContacts() {
        List<Contact> contacts = contactService.getAllContacts();
        if (contacts.isEmpty()) {
            System.out.println("No contacts found.");
            return;
        }
        System.out.println("\n===== All Contacts (" + contacts.size() + ") =====");
        for (Contact contact : contacts) {
            System.out.println(contact);
        }
    }

    private static void getContactById() {
        System.out.print("Enter contact ID: ");
        String id = scanner.nextLine();

        Contact contact = contactService.getContactById(id);
        System.out.println(contact);
    }

    private static void updateContact() {
        System.out.print("Enter contact ID to update: ");
        String id = scanner.nextLine();

        Contact existing = contactService.getContactById(id);

        System.out.print("Enter new name (" + existing.getName() + "): ");
        String name = scanner.nextLine();

        System.out.print("Enter new email (" + existing.getEmail() + "): ");
        String email = scanner.nextLine();

        System.out.print("Enter new phone (" + existing.getPhone() + "): ");
        String phone = scanner.nextLine();

        Contact updated = contactService.updateContact(id, name, email, phone);
        System.out.println("\n✓ Contact updated successfully!");
        System.out.println(updated);
    }

    private static void deleteContact() {
        System.out.print("Enter contact ID to delete: ");
        String id = scanner.nextLine();

        contactService.deleteContact(id);
        System.out.println("✓ Contact deleted successfully.");
    }
}
