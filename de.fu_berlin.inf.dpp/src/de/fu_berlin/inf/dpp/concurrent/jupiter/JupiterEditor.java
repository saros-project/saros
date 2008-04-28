package de.fu_berlin.inf.dpp.concurrent.jupiter;

import org.eclipse.core.runtime.IPath;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * interface for jupiter editor settings.
 * @author orieger
 *
 */
public interface JupiterEditor {
	
	public void setEditor(IPath path);
	
	public IPath getEditor();
}
