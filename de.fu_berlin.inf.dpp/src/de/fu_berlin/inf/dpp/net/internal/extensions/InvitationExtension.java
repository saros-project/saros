package de.fu_berlin.inf.dpp.net.internal.extensions;

import de.fu_berlin.inf.dpp.annotations.Component;

@Component(module = "net")
public abstract class InvitationExtension extends SarosSessionPacketExtension {

    final protected String invitationID;

    public InvitationExtension(String sessionID, String invitationID) {
        super(sessionID);
        this.invitationID = invitationID;
    }

    public String getInvitationID() {
        return invitationID;
    }
}
