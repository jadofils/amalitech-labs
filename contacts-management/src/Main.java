import repository.ContactRepository;
import repository.InMemoryContactRepository;
import service.ContactService;
import service.ContactServiceImpl;

import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final ContactRepository contactRepository = new InMemoryContactRepository();
    private static final ContactService contactService = new ContactServiceImpl(contactRepository);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n===== Contacts Management Menu =====");
            System.out.println("1. Exit");
            System.out.print("Choose an option: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1 -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
}
