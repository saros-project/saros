package de.fu_berlin.inf.dpp.ui.widgets.viewer.project.events;

import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.ResourceSelectionComposite;

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
