package saros.ui.eventhandler;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import saros.preferences.EclipsePreferenceConstants;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.ISessionListener;
import saros.session.SessionEndReason;
import saros.session.User;
import saros.ui.Messages;
import saros.ui.util.CollaborationUtils;
import saros.ui.util.SWTUtils;
import saros.ui.views.SarosView;

/**
 * Checks if the host remains alone after a user left the session. If so, ask if the session should
 * be closed (optionally remember choice for workspace...)
 *
 * @author Alexander Waldmann (contact@net-corps.de)
 */
public class HostLeftAloneInSessionHandler {

  private static Logger LOG = Logger.getLogger(HostLeftAloneInSessionHandler.class);

  private final ISarosSessionManager sessionManager;

  private final IPreferenceStore preferenceStore;

  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionEnded(ISarosSession session, SessionEndReason reason) {
          session.removeListener(sessionListener);
          /*
           * we need to clear any open notifications because there might be
           * stuff left, like follow mode notifications, or "user joined"
           * notification in case a user joined the session but aborted the
           * incoming project negotiation...
           */
          SarosView.clearNotifications();
        }

        @Override
        public void sessionStarted(ISarosSession session) {
          session.addListener(sessionListener);
        }
      };

  private final ISessionListener sessionListener =
      new ISessionListener() {

        @Override
        public void userLeft(User user) {

          ISarosSession session = sessionManager.getSession();

          if (session == null || !session.isHost() || !session.getRemoteUsers().isEmpty()) return;

          /*
           * only ask to close session if there are no running negotiations
           * because if there are, and the last user "left", it was because he
           * canceled an IncomingProjectNegotiation, and the session will be
           * closed anyway.
           */

          /*
           * Stefan Rossbach: Welcome to global state programming, threading
           * and network latency.
           *
           * This currently does NOT work. It is possible the the project
           * negotiation is about to finish (last cancellation check passed).
           * As we do not have a final synchronization packet in this
           * negotiation it is possible that the user left packet arrives but
           * cancellation packet arrives lately or never. See stack trace
           * below were the negotiation process passes although Bob leaves the
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
           * TRACE 16:49:09,122 [RMI TCP Connection(5)-127.0.0.1] (RemoteWorkbenchBot.java:75) opening view with id: saros.ui.views.SarosView
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
           * TRACE 16:49:09,402 [Worker-6] (FileSystemChecksumCache.java:74) invalidating checksum for existing file: /java/.classpath [0x8F53373DCA77CBCA9383FE23E0132271]
           * TRACE 16:49:09,402 [Worker-6] (FileSystemChecksumCache.java:74) invalidating checksum for existing file: /java/.project [0x5A01062F55D09CDA404237A34113627E]
           * TRACE 16:49:09,402 [Worker-6] (FileSystemChecksumCache.java:74) invalidating checksum for existing file: /java/src/de/alice/HelloWorld.java [0xCC20B91A3A971472D6482719A2540DA5]
           * TRACE 16:49:09,453 [main] (EditorPool.java:278) EditorPool.getAllEditors invoked
           * DEBUG 16:49:09,455 [Worker-6] (SharedProjectDecorator.java:80) PROJECT ADDED: 1212967801
           * ERROR 16:49:09,455 [Worker-6] (SarosSessionManager.java:782) Internal error in notifying listener of an added project: java.lang.NullPointerException
           * at saros.ui.decorators.SharedProjectDecorator$1.projectAdded(SharedProjectDecorator.java:81)
           * at saros.project.SarosSessionManager.projectAdded(SarosSessionManager.java:780)
           * at saros.negotiation.IncomingProjectNegotiation.accept(IncomingProjectNegotiation.java:241)
           * at saros.ui.wizards.AddProjectToSessionWizard$2.run(AddProjectToSessionWizard.java:267)
           * at org.eclipse.core.internal.jobs.Worker.run(Worker.java:54)
           * DEBUG 16:49:09,456 [Worker-6] (CancelableProcess.java:280) process IPN [remote side: jenkins_alice_stf@saros-con.imp.fu-berlin.de/Saros] exit status: OK
           * </pre>
           */

          // if (negotiations.getProcesses().size() == 0) {
          handleHostLeftAlone();
          // }
        }
      };

  public HostLeftAloneInSessionHandler(
      final ISarosSessionManager sessionManager, final IPreferenceStore preferenceStore) {
    this.sessionManager = sessionManager;
    this.preferenceStore = preferenceStore;
    this.sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
  }

  private void handleHostLeftAlone() {
    String stopSessionPreference =
        preferenceStore.getString(EclipsePreferenceConstants.AUTO_STOP_EMPTY_SESSION);

    boolean prompt = MessageDialogWithToggle.PROMPT.equals(stopSessionPreference);

    boolean stopSession = MessageDialogWithToggle.ALWAYS.equals(stopSessionPreference);

    if (prompt) {
      MessageDialogWithToggle dialog =
          MessageDialogWithToggle.openYesNoQuestion(
              SWTUtils.getShell(),
              Messages.HostLeftAloneInSessionDialog_title,
              Messages.HostLeftAloneInSessionDialog_message,
              "Remember decision",
              false,
              preferenceStore,
              EclipsePreferenceConstants.AUTO_STOP_EMPTY_SESSION);

      stopSession = dialog.getReturnCode() == IDialogConstants.YES_ID;
    }

    if (stopSession) {
      SWTUtils.runSafeSWTAsync(
          LOG,
          new Runnable() {
            @Override
            public void run() {
              CollaborationUtils.leaveSession();
            }
          });
    }
  }
}
