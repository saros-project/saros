package de.fu_berlin.inf.dpp.concurrent;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.RequestForwarder;
import de.fu_berlin.inf.dpp.net.IActivitySequencer;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;

/**
 * Interface for management controller class of all jupiter document server.
 * 
 * @author orieger
 * 
 */
public interface ConcurrentManager extends IRequestManager,
	ISharedProjectListener {

    public static enum Side {
	CLIENT_SIDE, HOST_SIDE
    }

    public IActivity activityCreated(IActivity activity);

    public IActivity exec(IActivity activity);

    public RequestForwarder getRequestForwarder();

    public boolean isHost(JID jid);

    public boolean isHostSide();

    /**
     * reset jupter state vector an init with [0,0]
     * 
     * @param path
     *            of document under jupiter control.
     */
    public void resetJupiterDocument(IPath path);

    public void setActivitySequencer(IActivitySequencer sequencer);

    public void setHost(JID jid);

    public void setRequestForwarder(RequestForwarder f);
}
