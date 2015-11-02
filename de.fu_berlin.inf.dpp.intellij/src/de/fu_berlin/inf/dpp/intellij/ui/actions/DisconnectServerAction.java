package de.fu_berlin.inf.dpp.intellij.ui.actions;

import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import org.picocontainer.annotations.Inject;

/**
 * Disconnects from XMPP/Jabber server
 */
public class DisconnectServerAction extends AbstractSarosAction {
    public static final String NAME = "disconnect";

    @Inject
    private XMPPConnectionService connectionService;

    @Override
    public String getActionName() {
        return NAME;
    }

    @Override
    public void execute() {
        connectionService.disconnect();
        actionPerformed();
    }
}
