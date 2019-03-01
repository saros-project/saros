package saros.ui.widgets.viewer.project.events;

public class FilterClosedProjectsChangedEvent {
  private boolean filterClosedProjects;

  /** @param filterClosedProjects */
  public FilterClosedProjectsChangedEvent(boolean filterClosedProjects) {
    super();
    this.filterClosedProjects = filterClosedProjects;
  }

  public boolean isFilterClosedProjects() {
    return filterClosedProjects;
  }
}
