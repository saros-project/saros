package de.fu_berlin.inf.dpp.net.internal.extensions;

public class CancelProjectNegotiationExtension extends SarosSessionPacketExtension {

    private String errorMessage;

    public CancelProjectNegotiationExtension(String sessionID, String errorMessage) {
        super(sessionID);
        if ((errorMessage != null) && (errorMessage.length() > 0))
            this.errorMessage = errorMessage;
    }

    /**
     * Returns the error message for this cancellation.
     * 
     * @return the error message or <code>null</code> if the remote contact
     *         cancelled the project negotiation manually
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    public static class Provider extends
        SarosSessionPacketExtension.Provider<CancelProjectNegotiationExtension> {
        public Provider() {
            super("cancelProjectSharing", CancelProjectNegotiationExtension.class);
        }
    }
}
