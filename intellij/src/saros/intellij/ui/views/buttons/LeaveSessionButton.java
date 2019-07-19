package saros.intellij.ui.views.buttons;

import com.intellij.openapi.project.Project;
import saros.intellij.ui.Messages;
import saros.intellij.ui.actions.LeaveSessionAction;
import saros.intellij.ui.util.IconManager;
import saros.session.ISarosSession;
import saros.session.SessionEndReason;

public class LeaveSessionButton extends AbstractSessionToolbarButton {
  /**
   * Creates a LeaveSessionButton and registers the sessionListener.
   *
   * <p>LeaveSessionButton is created as disabled.
   */
  public LeaveSessionButton(Project project) {
    super(
        project,
        LeaveSessionAction.NAME,
        Messages.LeaveSessionButton_tooltip,
        IconManager.LEAVE_SESSION_ICON);

    addActionListener(actionEvent -> new LeaveSessionAction(project).execute());
    setEnabled(false);

    setInitialState();
  }

  @Override
  void disposeComponents() {
    // NOP
  }

  @Override
  void sessionStarted(ISarosSession newSarosSession) {
    setEnabledFromUIThread(true);
  }

  @Override
  void sessionEnded(ISarosSession oldSarosSession, SessionEndReason reason) {
    setEnabledFromUIThread(false);
  }
}
