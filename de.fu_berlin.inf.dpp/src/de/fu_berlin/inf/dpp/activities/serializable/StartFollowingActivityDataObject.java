package de.fu_berlin.inf.dpp.activities.serializable;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.StartFollowingActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.xstream.JIDConverter;

/**
 * DataObject for the event that a user started following another user
 */
public class StartFollowingActivityDataObject extends
    AbstractActivityDataObject {

    @XStreamAsAttribute
    @XStreamConverter(JIDConverter.class)
    protected final JID target;

    public StartFollowingActivityDataObject(JID source, JID target) {
        super(source);
        this.target = target;
    }

    @Override
    public IActivity getActivity(ISarosSession sharedProject) {
        return new StartFollowingActivity(sharedProject.getUser(source),
            sharedProject.getUser(target));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof StartFollowingActivityDataObject))
            return false;
        StartFollowingActivityDataObject other = (StartFollowingActivityDataObject) obj;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "StartFollowingActivityDataObject(" + source + " > " + target
            + ")";
    }
}
