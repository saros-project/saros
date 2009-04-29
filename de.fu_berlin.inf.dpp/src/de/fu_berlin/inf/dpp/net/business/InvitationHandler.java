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
public class InvitationHandler {

    private static final Logger log = Logger.getLogger(InvitationHandler.class
        .getName());

    @Inject
    protected IXMPPTransmitter transmitter;

    @Inject
    protected SessionManager sessionManager;

    @Inject
    protected CancelInviteExtension cancelInviteExtension;

    protected SessionIDObservable sessionIDObservable;

    protected Handler handler;

    public InvitationHandler(XMPPChatReceiver receiver,
        SessionIDObservable sessionIDObservable) {
        this.sessionIDObservable = sessionIDObservable;

        this.handler = new Handler(sessionIDObservable);

        receiver.addPacketListener(handler, handler.getFilter());
    }

    protected class Handler extends InviteExtension {

        public Handler(SessionIDObservable sessionID) {
            super(sessionID);
        }

        @Override
        public void invitationReceived(JID sender, String sessionID,
            String projectName, String description, int colorID) {
            if (sessionIDObservable.getValue().equals(
                SessionIDObservable.NOT_IN_SESSION)) {
                log.debug("Received invitation with session id " + sessionID
                    + " and ColorID: " + colorID);
                sessionManager.invitationReceived(sender, sessionID,
                    projectName, description, colorID);
            } else {
                transmitter
                    .sendMessage(
                        sender,
                        cancelInviteExtension
                            .create(sessionID,
                                "I am already in a Saros-Session, try to contact me by chat first."));
            }
        }
    }
}