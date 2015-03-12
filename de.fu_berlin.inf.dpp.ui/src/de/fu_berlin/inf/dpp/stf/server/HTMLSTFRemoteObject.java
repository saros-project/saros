package de.fu_berlin.inf.dpp.stf.server;

import de.fu_berlin.inf.dpp.ISarosContext;
import de.fu_berlin.inf.dpp.ui.manager.BrowserManager;

/**
 * This is the base class for all remote object of the HTML GUI test framework.
 * It contains methods to acquire components from the {@link ISarosContext}
 */
public abstract class HTMLSTFRemoteObject {

    private static ISarosContext context;

    /**
     * This method must be called at the start of the test framework
     * in order for future lookups to succeed.
     *
     * @param context the Saros context to use for the GUI tests
     */
    static void setContext(ISarosContext context) {
        HTMLSTFRemoteObject.context = context;
    }

    protected ISarosContext getContext() {
        return context;
    }

    /**
     * @return the {@link BrowserManager} which is required to get a certain browser
     * and execute test code in it
     */
    protected BrowserManager getBrowserManager() {
        return getContext().getComponent(BrowserManager.class);
    }
}