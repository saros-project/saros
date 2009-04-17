package de.fu_berlin.inf.dpp.net.business;

import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.IXMPPTransmitter;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatReceiver;
import de.fu_berlin.inf.dpp.net.internal.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InviteExtension;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.SessionManager;

/**
 * Business Logic for handling Invitation requests
 * 
 * @component The single instance of this class per application is created by
 *            PicoContainer in the central plug-in class {@link Saros}
 */
public class InvitationHandler extends InviteExtension {

    private static final Logger log = Logger.getLogger(InvitationHandler.class
        .getName());

    @Inject
    protected IXMPPTransmitter transmitter;

    @Inject
    protected SessionManager sessionManager;

    @Inject
    protected SessionIDObservable sessionID;

    public InvitationHandler(XMPPChatReceiver receiver) {
        receiver.addPacketListener(this, this.getFilter());
    }

    @Override
    public void invitationReceived(JID sender, String sessionID,
        String projectName, String description, int colorID) {
        if (this.sessionID.getValue().equals(SessionIDObservable.NOT_IN_SESSION)) {
            log.debug("Received invitation with session id " + sessionID);
            log.debug("and ColorID: " + colorID + ", i'm "
                + Saros.getDefault().getMyJID());
            sessionManager.invitationReceived(sender, sessionID, projectName,
                description, colorID);
        } else {
            transmitter
                .sendMessage(
                    sender,
                    CancelInviteExtension
                        .getDefault()
                        .create(sessionID,
                            "I am already in a Saros-Session, try to contact me by chat first."));
        }
    }
}