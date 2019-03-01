package de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events;

import org.eclipse.core.resources.IResource;

public class ResourceSelectionChangedEvent {
  private IResource resources;
  private boolean isSelected;

  /**
   * @param resources {@link IResource} who's selection changed
   * @param isSelected new selection state
   */
  public ResourceSelectionChangedEvent(IResource resources, boolean isSelected) {
    super();
    this.resources = resources;
    this.isSelected = isSelected;
  }

  public IResource getProjectFiles() {
    return resources;
  }

  public boolean isSelected() {
    return isSelected;
  }
}
