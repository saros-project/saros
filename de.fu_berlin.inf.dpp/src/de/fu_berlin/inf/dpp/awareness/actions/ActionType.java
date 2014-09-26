package de.fu_berlin.inf.dpp.awareness.actions;

/**
 * Represents the type of action the user executed, like running a test,
 * executing a refactoring, creating a file or interacting with an ide element
 * (dialog or view).
 * */
public enum ActionType {
    /** The user created a file. */
    ADD_CREATEDFILE,
    /**
     * The user interacted with an ide element, like opening a dialog or
     * activating a view.
     */
    ADD_IDEELEMENT,
    /** The user has executed a refactoring. */
    ADD_REFACTORING,
    /** The user started or has run a test. */
    ADD_TESTRUN
}