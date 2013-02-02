package de.fu_berlin.inf.dpp.net.internal.extensions;

public class InvitationCompletedExtension extends InvitationExtension {
    public static final Provider PROVIDER = new Provider();

    public InvitationCompletedExtension(String invitationID) {
        super(invitationID);
    }

    public static class Provider extends
        InvitationExtension.Provider<InvitationCompletedExtension> {
        private Provider() {
            super("invitationCompleted", InvitationAcknowledgedExtension.class);
        }
    }
}
