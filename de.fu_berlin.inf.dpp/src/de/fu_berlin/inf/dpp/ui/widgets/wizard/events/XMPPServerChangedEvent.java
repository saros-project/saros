package de.fu_berlin.inf.dpp.ui.widgets.wizard.events;

public class XMPPServerChangedEvent {
    private String xmppServer;
    private boolean xmppServerValid;

    /**
     * @param xmppServerValid
     */
    public XMPPServerChangedEvent(String xmppServer,
        boolean xmppServerValid) {
        super();
        this.xmppServer = xmppServer;
        this.xmppServerValid = xmppServerValid;
    }

    public String getXMPPServer() {
        return this.xmppServer;
    }

    public boolean isXMPPServerValid() {
        return xmppServerValid;
    }

}
