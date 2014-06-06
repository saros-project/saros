package de.fu_berlin.inf.dpp.ui.expressions;

import org.eclipse.core.expressions.PropertyTester;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandler;
import de.fu_berlin.inf.dpp.session.ISarosSession;

/**
 * Adds tests to the {@link ISarosSession}. <br/>
 * Currently only tests whether the given {@link ISarosSession}'s participant is
 * the host.
 */
public class SarosPropertyTester extends PropertyTester {

    private static final boolean MDNS_MODE = Boolean
        .getBoolean("de.fu_berlin.inf.dpp.net.ENABLE_MDNS");

    @Inject
    private ConnectionHandler connectionHandler;

    public SarosPropertyTester() {
        super();
        SarosPluginContext.initComponent(this);
    }

    @Override
    public boolean test(Object receiver, String property, Object[] args,
        Object expectedValue) {
        if (receiver instanceof Saros) {

            if ("isConnected".equals(property)) {
                return connectionHandler.isConnected();
            }

            if ("isXMPPEnabled".equals(property)) {
                return !MDNS_MODE;
            }
        }
        return false;
    }

}
