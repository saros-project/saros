package de.fu_berlin.inf.dpp.net.business;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatReceiver;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensionUtils;
import de.fu_berlin.inf.dpp.net.internal.extensions.RequestForFileListExtension;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.util.Util;

@Component(module = "net")
public class RequestForFileListHandler {

    private static Logger log = Logger
        .getLogger(RequestForFileListHandler.class.getName());

    @Inject
    protected InvitationProcessObservable invitationProcesses;

    protected Handler handler;

    public RequestForFileListHandler(XMPPChatReceiver receiver,
        SessionIDObservable sessionID) {

        this.handler = new Handler(sessionID);

        receiver.addPacketListener(handler, handler.getFilter());
    }

    protected class Handler extends RequestForFileListExtension {

        protected Handler(SessionIDObservable sessionIDObservable) {
            super(sessionIDObservable);
        }

        @Override
        public PacketFilter getFilter() {
            return new AndFilter(super.getFilter(), PacketExtensionUtils
                .getSessionIDPacketFilter(sessionID));
        }

        @Override
        public void requestForFileListReceived(final JID sender) {

            log.debug("[" + sender.getName() + "] Request for FileList");

            Util.runSafeAsync("XMPPChatTransmitter-RequestForFileList", log,
                new Runnable() {

                    public void run() {
                        IInvitationProcess process = invitationProcesses
                            .getInvitationProcess(sender);
                        if (process != null) {
                            process.invitationAccepted(sender);
                        } else {
                            log.warn("Rcvd Invitation Acceptance"
                                + " from unknown user " + Util.prefix(sender));
                        }
                    }
                });
        }
    }
}