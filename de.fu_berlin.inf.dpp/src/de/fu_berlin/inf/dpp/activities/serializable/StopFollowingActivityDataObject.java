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

    @Override
    public IActivity getActivity(ISarosSession sharedProject) {
        return new StopFollowingActivity(sharedProject.getUser(source));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof StopFollowingActivityDataObject))
            return false;
        StopFollowingActivityDataObject other = (StopFollowingActivityDataObject) obj;
        if (source == null) {
            if (other.source != null)
                return false;
        } else if (!source.equals(other.source))
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "StopFollowingActivityDataObject(" + source + ")";
    }
}
