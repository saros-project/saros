package saros.ui.widgets.viewer.resources.events;

import saros.ui.widgets.viewer.resources.ResourceSelectionComposite;

/** Listener for {@link ResourceSelectionComposite} events. */
public interface ResourceSelectionListener extends BaseResourceSelectionListener {

  /**
   * Gets called whenever the {@link ResourceSelectionComposite#filterClosedProjects} option
   * changed.
   *
   * @param event
   */
  public void filterClosedProjectsChanged(FilterClosedProjectsChangedEvent event);
}
