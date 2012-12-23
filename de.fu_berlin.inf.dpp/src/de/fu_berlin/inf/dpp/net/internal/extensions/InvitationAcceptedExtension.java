package de.fu_berlin.inf.dpp.net.internal.extensions;

public class InvitationAcceptedExtension extends InvitationExtension {

    public InvitationAcceptedExtension(String sessionID, String invitationID) {
        super(sessionID, invitationID);
    }

    public static class Provider extends
        XStreamExtensionProvider<InvitationAcceptedExtension> {
        public Provider() {
            super("invitationAccepted", InvitationAcceptedExtension.class);
        }
    }
}
