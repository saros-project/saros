package de.fu_berlin.inf.dpp.concurrent;

import java.util.List;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.RequestForwarder;
import de.fu_berlin.inf.dpp.net.IActivitySequencer;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;

/**
 * Interface for management controller class of all jupiter document server.
 * @author orieger
 *
 */
public interface ConcurrentManager extends IRequestManager, ISharedProjectListener {

	public static enum Side{
		CLIENT_SIDE,
		HOST_SIDE
	}
	
	public void setActivitySequencer(IActivitySequencer sequencer);
	
	public void setRequestForwarder(RequestForwarder f);
	
	public RequestForwarder getRequestForwarder();
	
	public void addDriver(JID jid);
	
	public void removeDriver(JID jid);
	
	public List<JID> getDriver();
	
	public boolean isDriver(JID jid);
	
	public boolean isHostSide();
	
	public boolean isHost(JID jid);
	
	public void setHost(JID jid);
	
	public IActivity activityCreated(IActivity activity);
	
	public IActivity exec(IActivity activity);
}
