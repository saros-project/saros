package de.fu_berlin.inf.dpp.net.internal.extensions;

public class PingExtension extends SarosSessionPacketExtension {

    public static final Provider PROVIDER = new Provider();

    public PingExtension(String sessionID) {
        super(sessionID);
    }

    public static class Provider extends
        SarosSessionPacketExtension.Provider<PingExtension> {

        private Provider() {
            super("ping", PingExtension.class);
        }
    }
}
