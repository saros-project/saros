package de.fu_berlin.inf.dpp.activities.serializable;

import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.StopFollowingActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * DataObject for the event that a user stopped following another user
 */
public class StopFollowingActivityDataObject extends AbstractActivityDataObject {

    public StopFollowingActivityDataObject(JID source) {
        super(source);
    }

    public IActivity getActivity(ISarosSession sharedProject) {
        return new StopFollowingActivity(sharedProject.getUser(source));
    }

    @Override
    public String toString() {
        return "StopFollowingActivityDataObject(" + source + ")";
    }
}
