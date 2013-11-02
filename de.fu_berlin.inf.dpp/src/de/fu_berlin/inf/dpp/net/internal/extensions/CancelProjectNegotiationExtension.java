package de.fu_berlin.inf.dpp.net.internal.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias(/* ProjectNegotiationCancel */"PNCL")
public class CancelProjectNegotiationExtension extends
    SarosSessionPacketExtension {

    public static final Provider PROVIDER = new Provider();

    @XStreamAlias("error")
    private String errorMessage;

    public CancelProjectNegotiationExtension(String sessionID,
        String errorMessage) {
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
        private Provider() {
            super("pncl", CancelProjectNegotiationExtension.class);
        }
    }
}
