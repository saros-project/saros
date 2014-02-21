package de.fu_berlin.inf.dpp.activities.serializable;

import org.apache.commons.lang.ObjectUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.StartFollowingActivity;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.misc.xstream.JIDConverter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * DataObject for the event that a user started following another user
 */
@XStreamAlias("startFollowingActivity")
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
    public IActivity getActivity(ISarosSession sharedProject,
        IPathFactory pathFactory) {
        return new StartFollowingActivity(sharedProject.getUser(getSource()),
            sharedProject.getUser(target));
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
