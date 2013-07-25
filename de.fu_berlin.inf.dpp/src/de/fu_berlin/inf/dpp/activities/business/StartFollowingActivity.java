package de.fu_berlin.inf.dpp.activities.business;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.StartFollowingActivityDataObject;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * This activity notifies the recipient that the local user is following someone
 * in the running session
 * 
 * @author Alexander Waldmann (contact@net-corps.de)
 */
public class StartFollowingActivity extends AbstractActivity {
    protected final User followedUser;

    public StartFollowingActivity(User source, User followedUser) {
        super(source);
        this.followedUser = followedUser;
    }

    @Override
    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    @Override
    public IActivityDataObject getActivityDataObject(ISarosSession sarosSession) {
        return new StartFollowingActivityDataObject(source.getJID(),
            followedUser.getJID());
    }

    @Override
    public String toString() {
        return "StartFollowingActivity(" + source + " > " + followedUser + ")";
    }

    public User getFollowedUser() {
        return followedUser;
    }

}
