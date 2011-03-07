/**
 * 
 */
package de.fu_berlin.inf.dpp.net.business;

import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.invitation.InvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.XMPPReceiver;
import de.fu_berlin.inf.dpp.net.internal.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.util.Utils;

@Component(module = "net")
public class CancelInviteHandler extends CancelInviteExtension {

    private static Logger log = Logger.getLogger(CancelInviteHandler.class
        .getName());

    @Inject
    protected InvitationProcessObservable invitationProcesses;

    public CancelInviteHandler(SessionIDObservable sessionID,
        XMPPReceiver receiver) {
        super(sessionID);
        receiver.addPacketListener(this, getFilter());
    }

    @Override
    public void invitationCanceledReceived(JID sender, String errorMsg) {
        InvitationProcess process = invitationProcesses
            .getInvitationProcess(sender);
        if (process != null) {
            log.debug("Inv" + Utils.prefix(sender)
                + ": Received invitation cancel message");
            process.remoteCancel(errorMsg);
        } else {
            log.warn("Inv[unkown buddy]: Received invitation cancel message for unknown invitation process. Ignoring...");
        }
    }
}