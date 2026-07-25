package main.console;

/**
 * Shared getOptionNumber()/getLabel() plumbing every {@link MenuAction} implementation
 * otherwise repeats verbatim - each subclass supplies its own fixed option number and
 * label through the constructor instead of overriding both accessors itself.
 */
public abstract class AbstractMenuAction implements MenuAction {

    private final int optionNumber;
    private final String label;

    protected AbstractMenuAction(int optionNumber, String label) {
        this.optionNumber = optionNumber;
        this.label = label;
    }

    @Override
    public final int getOptionNumber() {
        return optionNumber;
    }

    @Override
    public final String getLabel() {
        return label;
    }
}
