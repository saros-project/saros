package de.fu_berlin.inf.dpp.ui.browser_functions;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.ui.core_facades.StateFacade;
import de.fu_berlin.inf.dpp.util.ThreadUtils;

/**
 * Offers a via Javascript invokable method to disconnect the active Account.
 * <p>
 * JS-signature: "void __java_disconnect();"
 */
public class DisconnectAccount extends JavascriptFunction {
    private static final Logger LOG = Logger.getLogger(DisconnectAccount.class);

    private final StateFacade stateFacade;
    public static final String JS_NAME = "disconnect";

    /**
     * Created by PicoContainer
     * 
     * @param stateFacade
     * @see HTMLUIContextFactory
     */
    public DisconnectAccount(StateFacade stateFacade) {
        super(NameCreator.getConventionName(JS_NAME));
        this.stateFacade = stateFacade;
    }

    @Override
    public Object function(Object[] arguments) {
        ThreadUtils.runSafeAsync(LOG, new Runnable() {
            @Override
            public void run() {
                stateFacade.disconnect();
            }
        });
        return null;
    }
}
