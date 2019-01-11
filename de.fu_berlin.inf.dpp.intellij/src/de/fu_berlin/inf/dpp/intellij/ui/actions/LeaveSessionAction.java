package de.fu_berlin.inf.dpp.intellij.ui.actions;

import de.fu_berlin.inf.dpp.core.ui.util.CollaborationUtils;

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
