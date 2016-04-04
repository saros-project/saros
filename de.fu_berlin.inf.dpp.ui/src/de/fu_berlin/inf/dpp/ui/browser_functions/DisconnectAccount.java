package de.fu_berlin.inf.dpp.ui.browser_functions;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.ui.browser_functions.BrowserFunction.Policy;
import de.fu_berlin.inf.dpp.ui.core_facades.StateFacade;

/**
 * Disconnect the active account.
 */
public class DisconnectAccount extends TypedJavascriptFunction {

    public static final String JS_NAME = "disconnect";

    private final StateFacade stateFacade;

    /**
     * Created by PicoContainer
     * 
     * @param stateFacade
     * @see HTMLUIContextFactory
     */
    public DisconnectAccount(StateFacade stateFacade) {
        super(JS_NAME);
        this.stateFacade = stateFacade;
    }

    /**
     * Disconnect the active account.
     */
    @BrowserFunction(Policy.ASYNC)
    public void disconnect() {
        stateFacade.disconnect();
    }
}
