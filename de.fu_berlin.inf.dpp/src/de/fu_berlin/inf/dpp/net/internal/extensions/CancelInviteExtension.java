/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import de.fu_berlin.inf.dpp.annotations.Component;

@Component(module = "net")
public class CancelInviteExtension extends SarosPacketExtension {

    private String errorMessage;
    private String invitationID;

    public CancelInviteExtension(String invitationID, String errorMessage) {
        this.invitationID = invitationID;
        if ((errorMessage != null) && (errorMessage.length() > 0))
            this.errorMessage = errorMessage;
    }

    /**
     * Returns the error message for this cancellation.
     * 
     * @return the error message or <code>null</code> if the remote contact
     *         cancelled the invitation manually
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    public String getInvitationID() {
        return invitationID;
    }

    public static class Provider extends
        XStreamExtensionProvider<CancelInviteExtension> {
        public Provider() {
            super("cancelInvitation", CancelInviteExtension.class);
        }
    }
}