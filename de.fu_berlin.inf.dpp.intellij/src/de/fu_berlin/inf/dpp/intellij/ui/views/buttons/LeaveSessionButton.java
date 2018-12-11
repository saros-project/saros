package de.fu_berlin.inf.dpp.intellij.ui.views.buttons;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.intellij.ui.actions.LeaveSessionAction;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import org.picocontainer.annotations.Inject;

public class LeaveSessionButton extends SimpleButton {

  public static final String LEAVE_SESSION_ICON_PATH = "/icons/famfamfam/session_leave_tsk.png";

  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
          setEnabledFromUIThread(true);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession, SessionEndReason reason) {

          setEnabledFromUIThread(false);
        }
      };

  @Inject private ISarosSessionManager sessionManager;

  /**
   * Creates a LeaveSessionButton and registers the sessionListener.
   *
   * <p>LeaveSessionButton is created as disabled.
   */
  public LeaveSessionButton() {
    super(new LeaveSessionAction(), "Leave session", LEAVE_SESSION_ICON_PATH, "leave");
    SarosPluginContext.initComponent(this);
    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
    setEnabled(false);
  }
}
