package de.fu_berlin.inf.dpp.activities.business;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.StopFollowingActivityDataObject;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

/**
 * This activity notifies the recipient that the local user is following someone
 * in the running session
 * 
 * TODO Consider treating {@link StartFollowingActivity} and
 * {@link StopFollowingActivity} as different types of the same class (since
 * this class here has no logic of its own).
 * 
 * @author Alexander Waldmann (contact@net-corps.de)
 */
public class StopFollowingActivity extends AbstractActivity {
    public StopFollowingActivity(User source) {
        super(source);
    }

    @Override
    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    @Override
    public IActivityDataObject getActivityDataObject(
        ISarosSession sarosSession, IPathFactory pathFactory) {
        return new StopFollowingActivityDataObject(getSource());
    }

    @Override
    public String toString() {
        return "StopFollowingActivity(" + getSource() + ")";
    }

    public SPath getPath() {
        return null;
    }
}
