package console;

/** Menu option 10: Exit. */
public class ExitAction implements MenuAction {

    @Override
    public int getOptionNumber() {
        return 10;
    }

    @Override
    public String getLabel() {
        return "Exit";
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
