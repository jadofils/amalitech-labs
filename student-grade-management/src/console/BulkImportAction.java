package console;

import exceptions.ImportException;
import imports.BulkImportService;

import java.util.Scanner;

/** Menu option 7: Bulk Import Grades. */
public class BulkImportAction implements MenuAction {

    private final Scanner scanner;
    private final BulkImportService bulkImportService;

    public BulkImportAction(Scanner scanner, BulkImportService bulkImportService) {
        this.scanner = scanner;
        this.bulkImportService = bulkImportService;
    }

    @Override
    public int getOptionNumber() {
        return 7;
    }

    @Override
    public String getLabel() {
        return "Bulk Import Grades";
    }

    @Override
    public void execute() {
        System.out.println("\nBULK IMPORT GRADES");
        System.out.println("─────────────────────────");

        System.out.println("\nPlace your CSV file in: ./imports/");
        System.out.println("\nCSV Format Required:");
        System.out.println("StudentID,SubjectName,SubjectType,Grade");
        System.out.println("Example: STU001,Mathematics,Core,85");

        System.out.print("\nEnter filename (without extension): ");
        String filename = scanner.nextLine().trim();

        if (filename.isEmpty()) {
            System.out.println("Filename cannot be empty.");
            return;
        }

        System.out.println("Validating file... ✓");
        System.out.println("Processing grades...");

        try {
            BulkImportService.ImportResult result = bulkImportService.importFromFile(filename);

            System.out.println("\nIMPORT SUMMARY");
            System.out.println("─────────────────────────");
            System.out.println("Total Rows: " + (result.getSuccessCount() + result.getFailedCount()));
            System.out.println("Successfully Imported: " + result.getSuccessCount());
            System.out.println("Failed: " + result.getFailedCount());

            if (result.getFailedCount() > 0) {
                System.out.println("\nFailed Records:");
                for (String reason : result.getFailReasons()) {
                    System.out.println("  " + reason);
                }
            }

            System.out.println("\n✓ Import completed!");
            System.out.println("  " + result.getSuccessCount() + " grades added to system");
            System.out.println("  See " + result.getLogFilename() + " for details");
        } catch (ImportException e) {
            System.out.println("\n✗ ERROR: " + e.getMessage());
            if (e.getFilePath() != null) {
                System.out.println("  File: " + e.getFilePath());
            }
        }

        ConsoleUtils.promptEnter(scanner);
    }
}
