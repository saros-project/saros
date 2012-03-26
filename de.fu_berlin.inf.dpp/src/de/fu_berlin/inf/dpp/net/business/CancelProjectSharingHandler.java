package de.fu_berlin.inf.dpp.net.business;

import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.invitation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.XMPPReceiver;
import de.fu_berlin.inf.dpp.net.internal.extensions.CancelProjectSharingExtension;
import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.util.Utils;

public class CancelProjectSharingHandler extends CancelProjectSharingExtension {

    private static Logger log = Logger
        .getLogger(CancelProjectSharingHandler.class.getName());

    @Inject
    protected ProjectNegotiationObservable invitationProcesses;

    public CancelProjectSharingHandler(SessionIDObservable sessionID,
        XMPPReceiver receiver) {
        super(sessionID);
        receiver.addPacketListener(this, getFilter());
    }

    @Override
    public void projectSharingCanceledReceived(JID sender, String errorMsg) {
        ProjectNegotiation process = invitationProcesses
            .getProjectExchangeProcess(sender);
        if (process != null) {
            log.debug("Inv" + Utils.prefix(sender)
                + ": Received invitation cancel message");
            process.remoteCancel(errorMsg);
        } else {
            log.warn("Inv[unkown user]: Received invitation cancel message");
        }
    }
}
