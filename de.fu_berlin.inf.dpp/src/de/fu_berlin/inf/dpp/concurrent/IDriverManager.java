package de.fu_berlin.inf.dpp.concurrent;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * This interface handles the driver event and the appropriate documents.
 * @author orieger
 *
 */
public interface IDriverManager {

	public void addDriver(JID jid);
	
	public void removeDriver(JID jid);
	
	public boolean isDriver(JID jid);
		
}
