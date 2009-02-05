package de.fu_berlin.inf.dpp.concurrent;

import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.RequestForwarder;
import de.fu_berlin.inf.dpp.net.IActivitySequencer;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;

/**
 * Interface for management controller class of all jupiter document server.
 * 
 * TODO [CO] IConcurrentManager should not implement ISharedProjectListener and IRequestManager.
 * 
 * @author orieger
 * 
 */
public interface IConcurrentManager extends IRequestManager,
        ISharedProjectListener {

    public static enum Side {
        CLIENT_SIDE, HOST_SIDE
    }

    public void setActivitySequencer(IActivitySequencer sequencer);

    public void setRequestForwarder(RequestForwarder f);

    public RequestForwarder getRequestForwarder();

    public boolean isHostSide();

    public boolean isHost(JID jid);

    public void setHost(JID jid);

    public IActivity activityCreated(IActivity activity);

    public IActivity exec(IActivity activity);

}
