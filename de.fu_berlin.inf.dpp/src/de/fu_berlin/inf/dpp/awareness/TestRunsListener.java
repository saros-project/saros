package de.fu_berlin.inf.dpp.awareness;

/**
 * A listener for test run activities, like starting and finishing a test run.
 */
public interface TestRunsListener {

    /**
     * Is fired, when a user started or finished a test run.
     * */
    public void testRunChanged();

}
