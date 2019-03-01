package saros.whiteboard;

import org.eclipse.ui.IStartup;
import saros.whiteboard.net.WhiteboardManager;

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
