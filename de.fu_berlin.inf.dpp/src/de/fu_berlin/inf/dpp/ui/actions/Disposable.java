package de.fu_berlin.inf.dpp.ui.actions;

import de.fu_berlin.inf.dpp.ui.views.SarosView;

public interface Disposable {

  /**
   * Gets called when the {@link SarosView} is about to being disposed. Actions implementing this
   * interface should release all resources and remove all installed listeners that were allocated
   * and installed during the lifetime of the action.
   */
  public void dispose();
}
