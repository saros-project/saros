package saros.intellij.ui.actions;

import saros.core.ui.util.CollaborationUtils;

/** Action to leave session */
public class LeaveSessionAction extends AbstractSarosAction {
  public static final String NAME = "leave";

  @Override
  public String getActionName() {
    return NAME;
  }

  @Override
  public void execute() {
    CollaborationUtils.leaveSession();
    actionPerformed();
  }
}
