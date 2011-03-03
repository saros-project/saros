package de.fu_berlin.inf.dpp.ui.widgets.wizard.events;

public class IsSarosXMPPServerChangedEvent {
    private boolean isSarosXMPPServer;

    /**
     * @param isSarosXMPPServer
     */
    public IsSarosXMPPServerChangedEvent(boolean isSarosXMPPServer) {
        super();
        this.isSarosXMPPServer = isSarosXMPPServer;
    }

    public boolean isSarosXMPPServer() {
        return isSarosXMPPServer;
    }

}
