package de.fu_berlin.inf.dpp.net.internal.extensions;

import de.fu_berlin.inf.dpp.net.JID;

public class UserFinishedProjectNegotiationExtension extends
    SarosSessionPacketExtension {

    public static final Provider PROVIDER = new Provider();
    private JID jid;

    public UserFinishedProjectNegotiationExtension(String sessionID, JID jid) {
        super(sessionID);
        this.jid = jid;
    }

    public JID getJID() {
        return jid;
    }

    public static class Provider
        extends
        SarosSessionPacketExtension.Provider<UserFinishedProjectNegotiationExtension> {

        private Provider() {
            super("userFinishedProjectNegotiation",
                UserFinishedProjectNegotiationExtension.class);
        }
    }
}
