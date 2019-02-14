package de.fu_berlin.inf.dpp.whiteboard;

import de.fu_berlin.inf.dpp.whiteboard.net.WhiteboardManager;
import org.eclipse.ui.IStartup;

/**
 * Initializes the whitboard manager singleton
 *
 * @author jurke
 */
public class StartupWhiteboard implements IStartup {

  @Override
  public void earlyStartup() {
    WhiteboardManager.getInstance();
  }
}
