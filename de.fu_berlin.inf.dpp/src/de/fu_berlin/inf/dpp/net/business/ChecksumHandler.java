/**
 * 
 */
package de.fu_berlin.inf.dpp.net.business;

import java.util.List;

import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.concurrent.management.DocumentChecksum;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatReceiver;
import de.fu_berlin.inf.dpp.net.internal.extensions.ChecksumExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensionUtils;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.observables.SharedProjectObservable;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;

/**
 * This class is responsible for processing a Checksum sent to us.
 */
@Component(module = "net")
public class ChecksumHandler {

    /* Dependencies */
    @Inject
    protected ConsistencyWatchdogClient watchdogClient;

    @Inject
    protected SharedProjectObservable project;

    protected SessionManager sessionManager;

    /* Fields */
    protected Handler handler;

    public ChecksumHandler(SessionManager sessionManager,
        XMPPChatReceiver receiver, SessionIDObservable sessionIDObservable) {

        this.sessionManager = sessionManager;
        this.handler = new Handler(sessionIDObservable);

        receiver.addPacketListener(handler, handler.getFilter());
    }

    protected class Handler extends ChecksumExtension {

        public Handler(SessionIDObservable sessionID) {
            super(sessionID);
        }

        @Override
        public PacketFilter getFilter() {
            return new AndFilter(super.getFilter(), PacketExtensionUtils
                .getInSessionFilter(sessionManager));
        }

        @Override
        public void checksumsReceived(JID sender,
            List<DocumentChecksum> checksums) {
            ISharedProject currentProject = project.getValue();

            assert currentProject != null;

            watchdogClient.setChecksums(checksums);
            watchdogClient.checkConsistency();
        }
    }

}