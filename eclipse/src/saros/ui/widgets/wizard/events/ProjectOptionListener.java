package saros.ui.widgets.wizard.events;

import saros.ui.widgets.wizard.ProjectOptionComposite;

public interface ProjectOptionListener {
  public void projectNameChanged(ProjectOptionComposite composite);

  public void projectOptionChanged(ProjectOptionComposite composite);
}
