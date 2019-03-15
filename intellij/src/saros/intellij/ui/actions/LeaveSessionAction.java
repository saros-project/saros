package saros.intellij.ui.actions;

import com.intellij.openapi.project.Project;
import saros.core.ui.util.CollaborationUtils;

/** Action to leave session */
public class LeaveSessionAction extends AbstractSarosAction {
  private static final String NAME = "leave";

  private final Project project;

  public LeaveSessionAction(Project project) {
    super();

    this.project = project;
  }

  @Override
  public String getActionName() {
    return NAME;
  }

  @Override
  public void execute() {
    CollaborationUtils.leaveSession(project);
    actionPerformed();
  }
}
