package de.fu_berlin.inf.dpp.whiteboard;

import org.eclipse.ui.IStartup;

import de.fu_berlin.inf.dpp.whiteboard.net.WhiteboardManager;

/**
 * Initializes the whitboard manager singleton
 * 
 * @author jurke
 * 
 */
public class StartupWhiteboard implements IStartup {

	@Override
	public void earlyStartup() {
		WhiteboardManager.getInstance();
	}

}
