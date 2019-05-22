package saros.intellij.ui.views.buttons;

import saros.SarosPluginContext;
import saros.intellij.ui.actions.LeaveSessionAction;
import saros.intellij.ui.util.IconManager;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.SessionEndReason;

public class LeaveSessionButton extends SimpleButton {

  @SuppressWarnings("FieldCanBeLocal")
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
    super(new LeaveSessionAction(), "Leave session", IconManager.LEAVE_SESSION_ICON);
    SarosPluginContext.initComponent(this);
    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
    setEnabled(false);
  }
}
