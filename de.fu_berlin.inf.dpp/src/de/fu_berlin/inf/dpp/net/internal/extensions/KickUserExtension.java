package de.fu_berlin.inf.dpp.net.internal.extensions;

public class KickUserExtension extends SarosSessionPacketExtension {

    public static final Provider PROVIDER = new Provider();

    public KickUserExtension(String sessionID) {
        super(sessionID);
    }

    public static class Provider extends
        SarosSessionPacketExtension.Provider<KickUserExtension> {
        private Provider() {
            super("kickedFromSession", KickUserExtension.class);
        }
    }
}
