package de.fu_berlin.inf.dpp.concurrent;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;

public interface IDriverDocumentManager extends IDriverManager, ISharedProjectListener{

	public void receiveActivity(IActivity activity);
	
	public void addDriverToDocument(IPath path, JID jid);
}
