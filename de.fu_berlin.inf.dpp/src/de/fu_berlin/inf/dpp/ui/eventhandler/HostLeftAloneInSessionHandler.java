package de.fu_berlin.inf.dpp.ui.eventhandler;

import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.views.SarosView;

/**
 * Checks if the host remains alone after a user left the session. If so, ask if
 * the session should be closed (optionally remember choice for workspace...)
 * 
 * @author Alexander Waldmann (contact@net-corps.de)
 */
public class HostLeftAloneInSessionHandler {

    private static Logger log = Logger
        .getLogger(HostLeftAloneInSessionHandler.class);

    @Inject
    Saros saros;

    protected final ISarosSessionManager sessionManager;
    private ISharedProjectListener projectListener;

    public HostLeftAloneInSessionHandler(ISarosSessionManager sManager,
        final ProjectNegotiationObservable processes) {
        sessionManager = sManager;
        projectListener = new AbstractSharedProjectListener() {
            @Override
            public void userLeft(User user) {
                log.debug("sessionManager.userLeft");
                ISarosSession session = sessionManager.getSarosSession();
                if (session != null && session.isHost()
                    && session.getUsers().size() == 1) {
                    /*
                     * only ask to close session if there are no running
                     * negotiation processes because if there are, and the last
                     * user "left", it was because he cancelled an
                     * IncomingProjectNegotiation, and the session will be
                     * closed anyway.
                     */

                    /*
                     * Stefan Rossbach: Welcome to global state programming,
                     * threading and network latency.
                     * 
                     * This currently does NOT work. It is possible the the
                     * project negotiation is about to finish (last cancellation
                     * check passed). As we do not have a final synchronization
                     * packet in this negotiation process it is possible that
                     * the user left packet arrives but cancellation packet
                     * arrives lately or never. See stack trace below were the
                     * negotiation process passes although Bob leaves the
                     * session during negotiation.
                     */

                    /**
                     * Alice side:
                     * 
                     * <pre>
                     * DEBUG 16:49:08,878 [main] (HostLeftAloneInSessionHandler.java:44) sessionManager.userLeft
                     * INFO  16:49:08,888 [main] (SarosSession.java:612) Buddy [jenkins_bob_stf@saros-con.imp.fu-berlin.de/Saros]  left session
                     * DEBUG 16:49:08,888 [Thread-217] (BinaryChannelConnection.java:51) SOCKS5 (direct) [jenkins_bob_stf@saros-con.imp.fu-berlin.de/Saros] Connection was closed by me. socket closed
                     * TRACE 16:49:09,060 [main] (EditorManager.java:1027) .partActivated invoked
                     * TRACE 16:49:09,060 [main] (SharedProjectFileDecorator.java:133) User: jenkins_alice_stf activated an editor -> SPath [editorType=txt, path=src/de/alice/HelloWorld.java, project=java]
                     * TRACE 16:49:09,070 [Worker-10] (SharedProjectFileDecorator.java:301) No Deco: L/java/src/de/alice/HelloWorld.java
                     * TRACE 16:49:09,083 [Worker-10] (SharedProjectFileDecorator.java:301) No Deco: P/java
                     * TRACE 16:49:09,122 [RMI TCP Connection(5)-127.0.0.1] (RemoteWorkbenchBot.java:75) opening view with id: de.fu_berlin.inf.dpp.ui.views.SarosView
                     * DEBUG 16:49:09,422 [Worker-9] (OutgoingProjectNegotiation.java:433) OPN [remote side: jenkins_bob_stf@saros-con.imp.fu-berlin.de/Saros] : archive send
                     * DEBUG 16:49:09,422 [Worker-9] (CancelableProcess.java:280) process OPN [remote side: jenkins_bob_stf@saros-con.imp.fu-berlin.de/Saros] exit status: OK
                     * </pre>
                     * 
                     * Bob side:
                     * 
                     * <pre>
                     * 
                     * DEBUG 16:49:09,402 [Worker-6] (FileUtils.java:205) File written to disk: .project
                     * DEBUG 16:49:09,402 [Worker-6] (FileUtils.java:209) Unpacked archive in 0 s
                     * TRACE 16:49:09,402 [Worker-6] (ChecksumCacheImpl.java:74) invalidating checksum for existing file: /java/.classpath [0x8F53373DCA77CBCA9383FE23E0132271]
                     * TRACE 16:49:09,402 [Worker-6] (ChecksumCacheImpl.java:74) invalidating checksum for existing file: /java/.project [0x5A01062F55D09CDA404237A34113627E]
                     * TRACE 16:49:09,402 [Worker-6] (ChecksumCacheImpl.java:74) invalidating checksum for existing file: /java/src/de/alice/HelloWorld.java [0xCC20B91A3A971472D6482719A2540DA5]
                     * TRACE 16:49:09,453 [main] (EditorPool.java:278) EditorPool.getAllEditors invoked
                     * DEBUG 16:49:09,455 [Worker-6] (SharedProjectDecorator.java:80) PROJECT ADDED: 1212967801
                     * ERROR 16:49:09,455 [Worker-6] (SarosSessionManager.java:782) Internal error in notifying listener of an added project: java.lang.NullPointerException
                     * at de.fu_berlin.inf.dpp.ui.decorators.SharedProjectDecorator$1.projectAdded(SharedProjectDecorator.java:81)
                     * at de.fu_berlin.inf.dpp.project.SarosSessionManager.projectAdded(SarosSessionManager.java:780)
                     * at de.fu_berlin.inf.dpp.invitation.IncomingProjectNegotiation.accept(IncomingProjectNegotiation.java:241)
                     * at de.fu_berlin.inf.dpp.ui.wizards.AddProjectToSessionWizard$2.run(AddProjectToSessionWizard.java:267)
                     * at org.eclipse.core.internal.jobs.Worker.run(Worker.java:54)
                     * DEBUG 16:49:09,456 [Worker-6] (CancelableProcess.java:280) process IPN [remote side: jenkins_alice_stf@saros-con.imp.fu-berlin.de/Saros] exit status: OK
                     * </pre>
                     */

                    // if (processes.getProcesses().size() == 0) {
                    handleHostLeftAlone();
                    // }
                }
            }
        };

        // register our sharedProjectListener when a session is started..
        sessionManager
            .addSarosSessionListener(new AbstractSarosSessionListener() {
                @Override
                public void sessionEnded(ISarosSession oldSarosSession) {
                    oldSarosSession.removeListener(projectListener);
                    /*
                     * we need to clear any open notifications because there
                     * might be stuff left, like follow mode notifications, or
                     * "buddy joined" notification in case a buddy joined the
                     * session but aborted the incoming project negotiation...
                     */
                    SarosView.clearNotifications();
                }

                @Override
                public void sessionStarted(ISarosSession newSarosSession) {
                    newSarosSession.addListener(projectListener);
                }
            });
    }

    public void handleHostLeftAlone() {
        String stopSessionPreference = saros.getPreferenceStore().getString(
            PreferenceConstants.STOP_EMPTY_SESSIONS);

        boolean stopSession = true;

        // if user did not save a decision in preferences yet: ask!
        if (!stopSessionPreference.equals("false")
            && !stopSessionPreference.equals("true")) {
            stopSession = DialogUtils.popUpRememberDecisionDialog(
                Messages.HostLeftAloneInSessionDialog_title,
                Messages.HostLeftAloneInSessionDialog_message, saros,
                PreferenceConstants.STOP_EMPTY_SESSIONS);
        } else {
            stopSession = stopSessionPreference.equals("true");
        }

        if (stopSession) {
            SWTUtils.runSafeSWTAsync(log, new Runnable() {
                @Override
                public void run() {
                    CollaborationUtils.leaveSession(sessionManager);
                }
            });

        }
    }
}
