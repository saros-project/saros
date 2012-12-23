package de.fu_berlin.inf.dpp.net.internal.extensions;

public class InvitationAcknowledgedExtension extends InvitationExtension {

    public InvitationAcknowledgedExtension(String sessionID, String invitationID) {
        super(sessionID, invitationID);
    }

    public static class Provider extends
        XStreamExtensionProvider<InvitationAcknowledgedExtension> {
        public Provider() {
            super("invitationAcknowledged",
                InvitationAcknowledgedExtension.class);
        }
    }
}
