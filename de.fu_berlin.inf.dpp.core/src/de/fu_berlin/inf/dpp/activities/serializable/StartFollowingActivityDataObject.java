package de.fu_berlin.inf.dpp.activities.serializable;

import org.apache.commons.lang.ObjectUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.StartFollowingActivity;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

/**
 * DataObject for the event that a user started following another user
 */
@XStreamAlias("startFollowingActivity")
public class StartFollowingActivityDataObject extends
    AbstractActivityDataObject {

    @XStreamAsAttribute
    protected final User target;

    public StartFollowingActivityDataObject(User source, User target) {
        super(source);

        this.target = target;
    }

    @Override
    public IActivity getActivity(ISarosSession session, IPathFactory pathFactory) {
        return new StartFollowingActivity(getSource(), target);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ObjectUtils.hashCode(target);
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

        if (!ObjectUtils.equals(this.target, other.target))
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "StartFollowingActivityDO(" + getSource() + " > " + target + ")";
    }
}
