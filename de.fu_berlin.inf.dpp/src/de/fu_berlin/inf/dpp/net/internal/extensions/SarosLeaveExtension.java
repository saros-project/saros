package de.fu_berlin.inf.dpp.net.internal.extensions;


public class SarosLeaveExtension extends SarosSessionPacketExtension {

    public static final Provider PROVIDER = new Provider();

    public SarosLeaveExtension(String sessionID) {
        super(sessionID);
    }

    public static class Provider extends
        SarosSessionPacketExtension.Provider<SarosLeaveExtension> {
        private Provider() {
            super("leaveSession", SarosLeaveExtension.class);
        }
    }
}
