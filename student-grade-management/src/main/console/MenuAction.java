package main.console;

import main.model.enums.Role;

/**
 * One menu option (Add Student, View Grade Report, ...). The main.console entry
 * point only knows how to print a numbered list of these, dispatch a chosen
 * number to one, and gate it by role - it no longer needs to know what any
 * individual action actually does, so adding, removing, or reordering menu
 * options never requires touching the entry point's own code.
 */
public interface MenuAction {

    int getOptionNumber();

    String getLabel();

    void execute();

    /** Whether this action is available to the given role when role-based access is on. */
    default boolean isAuthorizedFor(Role role) {
        return true;
    }

    /** True only for the action that ends the program (Exit). */
    default boolean terminatesLoop() {
        return false;
    }
}
