package de.fu_berlin.inf.dpp.net.internal.extensions;

public class InvitationAcceptedExtension extends InvitationExtension {

    public static final Provider PROVIDER = new Provider();

    public InvitationAcceptedExtension(String invitationID) {
        super(invitationID);
    }

    public static class Provider extends
        InvitationExtension.Provider<InvitationAcceptedExtension> {
        private Provider() {
            super("invitationAccepted", InvitationAcceptedExtension.class);
        }
    }
}
