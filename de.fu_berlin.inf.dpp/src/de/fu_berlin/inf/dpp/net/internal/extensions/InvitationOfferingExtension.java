package de.fu_berlin.inf.dpp.net.internal.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

@XStreamAlias(/* SessionNegotiationOffering */"SNOF")
public class InvitationOfferingExtension extends InvitationExtension {

    public static final Provider PROVIDER = new Provider();

    @XStreamAlias("sid")
    @XStreamAsAttribute
    private String sessionID;

    @XStreamAlias("version")
    private VersionInfo versionInfo;

    @XStreamAlias("description")
    private String description;

    public InvitationOfferingExtension(String invitationID, String sessionID,
        VersionInfo versionInfo, String description) {
        super(invitationID);

        this.sessionID = sessionID;
        this.versionInfo = versionInfo;
        this.description = description;
    }

    /**
     * Returns the remote session ID of the inviter.
     * 
     * @return
     */
    public String getSessionID() {
        return sessionID;
    }

    /**
     * Returns the remote version of the inviter.
     * 
     * @return
     */
    public VersionInfo getVersionInfo() {
        return versionInfo;
    }

    /**
     * Returns a description why this invitation was offered.
     * 
     * @return a user generated description or <code>null</code> if no
     *         description is available
     */
    public String getDescription() {
        return description;
    }

    public static class Provider extends
        InvitationExtension.Provider<InvitationOfferingExtension> {

        private Provider() {
            super("snof", InvitationOfferingExtension.class);
        }
    }
}
