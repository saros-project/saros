/**
 * 
 */
package de.fu_berlin.inf.dpp.net.business;

import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.invitation.IInvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatReceiver;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatTransmitter;
import de.fu_berlin.inf.dpp.net.internal.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;

public class CancelInviteHandler extends CancelInviteExtension {

    private static final Logger log = Logger
        .getLogger(CancelInviteHandler.class.getName());

    @Inject
    protected XMPPChatTransmitter chatTransmitter;

    public CancelInviteHandler(SessionIDObservable sessionID,
        XMPPChatReceiver receiver) {
        super(sessionID);
        receiver.addPacketListener(this, getFilter());
    }

    @Override
    public void invitationCanceledReceived(JID sender, String errorMsg) {
        IInvitationProcess process = chatTransmitter
            .getInvitationProcess(sender);
        if (process != null) {
            process.cancel(errorMsg, true);
        } else {
            log.warn("Received Invitation Canceled message from unknown user ["
                + sender.getBase() + "]");
        }
    }
}