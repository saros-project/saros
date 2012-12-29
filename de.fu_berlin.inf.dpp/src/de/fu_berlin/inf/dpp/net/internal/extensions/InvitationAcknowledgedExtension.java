package de.fu_berlin.inf.dpp.net.internal.extensions;

public class InvitationAcknowledgedExtension extends InvitationExtension {

    public static final Provider PROVIDER = new Provider();

    public InvitationAcknowledgedExtension(String invitationID) {
        super(invitationID);
    }

    public static class Provider extends
        InvitationExtension.Provider<InvitationAcknowledgedExtension> {
        private Provider() {
            super("invitationAcknowledged",
                InvitationAcknowledgedExtension.class);
        }
    }
}
