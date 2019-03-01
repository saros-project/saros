package saros.ui.widgets.wizard.events;

public class ProjectNameChangedEvent {

  public final String projectID;
  public final String projectName;

  public ProjectNameChangedEvent(String projectID, String projectName) {
    this.projectID = projectID;
    this.projectName = projectName;
  }
}
