package de.fu_berlin.inf.dpp.net.business;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatReceiver;
import de.fu_berlin.inf.dpp.net.internal.extensions.JoinExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensionUtils;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.observables.SharedProjectObservable;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.util.Util;

@Component(module = "net")
public class JoinHandler {

    private static Logger log = Logger.getLogger(JoinHandler.class.getName());

    @Inject
    protected InvitationProcessObservable invitationProcesses;

    @Inject
    protected SharedProjectObservable sharedProject;

    protected Handler handler;

    public JoinHandler(XMPPChatReceiver receiver, SessionIDObservable sessionID) {

        this.handler = new Handler(sessionID);

        receiver.addPacketListener(handler, handler.getFilter());
    }

    protected class Handler extends JoinExtension {

        protected Handler(SessionIDObservable sessionIDObservable) {
            super(sessionIDObservable);
        }

        @Override
        public PacketFilter getFilter() {
            return new AndFilter(super.getFilter(), PacketExtensionUtils
                .getSessionIDPacketFilter(sessionID));
        }

        @Override
        public void joinReceived(final JID sender, final int colorID) {

            log.debug("[" + sender.getName() + "] Join: ColorID=" + colorID);

            Util.runSafeAsync("XMPPChatTransmitter-RequestForFileList", log,
                new Runnable() {
                    public void run() {
                        IInvitationProcess process = invitationProcesses
                            .getInvitationProcess(sender);
                        if (process != null) {
                            process.joinReceived(sender);
                            return;
                        }

                        ISharedProject project = sharedProject.getValue();

                        if (project != null) {
                            // a new user joined this session
                            project.addUser(new User(project, sender, colorID));
                        }
                    }
                });
        }
    }
}