package de.fu_berlin.inf.dpp.activities.business;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
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
    protected final User target;

    public StartFollowingActivity(User source, User target) {
        super(source);
        this.target = target;
    }

    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    public IActivityDataObject getActivityDataObject(ISarosSession sarosSession) {
        return new StartFollowingActivityDataObject(source.getJID(),
            target.getJID());
    }

    @Override
    public String toString() {
        return "StartFollowingActivity(" + source + " > " + target + ")";
    }

    public User getTarget() {
        return target;
    }

    public SPath getPath() {
        return null;
    }
}
