package de.fu_berlin.inf.dpp.ui.browser_functions;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.ui.browser_functions.BrowserFunction.Policy;
import de.fu_berlin.inf.dpp.ui.core_facades.StateFacade;
import de.fu_berlin.inf.dpp.ui.model.Account;

/**
 * Connect with given Account.
 */
public class ConnectAccount extends TypedJavascriptFunction {

    public static final String JS_NAME = "connect";

    private final StateFacade stateFacade;

    /**
     * Created by PicoContainer
     * 
     * @param stateFacade
     * @see HTMLUIContextFactory
     */
    public ConnectAccount(StateFacade stateFacade) {
        super(JS_NAME);
        this.stateFacade = stateFacade;
    }

    /**
     * Connect with given Account.
     * 
     * @param account
     */
    @BrowserFunction(Policy.ASYNC)
    public void connect(final Account account) {
        stateFacade.connect(account);
    }
}
