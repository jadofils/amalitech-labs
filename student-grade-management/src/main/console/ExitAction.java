package main.console;

/** Menu option 10: Exit. */
public class ExitAction extends AbstractMenuAction {

    public ExitAction() {
        super(10, "Exit");
    }

    @Override
    public boolean terminatesLoop() {
        return true;
    }

    @Override
    public void execute() {
        System.out.println("Thank you for using Student Grade Management System!");
        System.out.println("Goodbye!");
    }
}
