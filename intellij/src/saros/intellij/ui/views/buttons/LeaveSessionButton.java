package saros.intellij.ui.views.buttons;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import saros.intellij.ui.Messages;
import saros.intellij.ui.actions.LeaveSessionAction;
import saros.intellij.ui.util.IconManager;
import saros.session.ISarosSession;
import saros.session.SessionEndReason;

/**
 * Session button to leave the current session.
 *
 * <p><b>NOTE:</b>This component and any component added here must be correctly torn down when the
 * project the components belong to is closed. See {@link AbstractSessionToolbarButton}.
 */
public class LeaveSessionButton extends AbstractSessionToolbarButton {
  /**
   * Creates a LeaveSessionButton and registers the sessionListener.
   *
   * <p>LeaveSessionButton is created as disabled.
   */
  public LeaveSessionButton(@NotNull Project project) {
    super(
        project,
        LeaveSessionAction.NAME,
        Messages.LeaveSessionButton_leave_tooltip,
        IconManager.LEAVE_SESSION_ICON);

    addActionListener(actionEvent -> new LeaveSessionAction(project).execute());
    setEnabled(false);

    setInitialState();
  }

  @Override
  void sessionStarted(ISarosSession newSarosSession) {
    setEnabled(true);

    if (newSarosSession.isHost()) {
      setButtonIcon(IconManager.TERMINATE_SESSION_ICON);
      setToolTipText(Messages.LeaveSessionButton_terminate_tooltip);
    }
  }

  @Override
  void sessionEnded(ISarosSession oldSarosSession, SessionEndReason reason) {
    setEnabled(false);

    if (oldSarosSession.isHost()) {
      setButtonIcon(IconManager.LEAVE_SESSION_ICON);
      setToolTipText(Messages.LeaveSessionButton_leave_tooltip);
    }
  }
}
