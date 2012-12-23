package de.fu_berlin.inf.dpp.net.internal.extensions;

public class SarosLeaveExtension extends SarosSessionPacketExtension {

    public SarosLeaveExtension(String sessionID) {
        super(sessionID);
    }

    public static class Provider extends
        SarosSessionPacketExtension.Provider<SarosLeaveExtension> {
        public Provider() {
            super("leaveSession", SarosLeaveExtension.class);
        }
    }
}
