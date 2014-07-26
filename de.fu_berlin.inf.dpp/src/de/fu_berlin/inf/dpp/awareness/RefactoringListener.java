package de.fu_berlin.inf.dpp.awareness;

/**
 * A listener for refactoring activities
 */
public interface RefactoringListener {

    /**
     * Is fired, when a user performed a refactoring.
     * */
    public void refactoringActivityChanged();

}