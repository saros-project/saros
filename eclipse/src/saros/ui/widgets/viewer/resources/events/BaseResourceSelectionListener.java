package saros.ui.widgets.viewer.resources.events;

import org.eclipse.core.resources.IFile;
import saros.ui.widgets.viewer.resources.BaseResourceSelectionComposite;

/** Listener for {@link BaseResourceSelectionComposite} events. */
public interface BaseResourceSelectionListener {

  /**
   * Gets called whenever a {@link IFile} selection changed.
   *
   * @param event
   */
  public void resourceSelectionChanged(ResourceSelectionChangedEvent event);
}
