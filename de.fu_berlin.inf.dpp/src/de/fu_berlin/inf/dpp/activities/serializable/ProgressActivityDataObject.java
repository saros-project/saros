package de.fu_berlin.inf.dpp.activities.serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.ProgressActivity;
import de.fu_berlin.inf.dpp.activities.business.ProgressActivity.ProgressAction;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.xstream.JIDConverter;

/**
 * A {@link ProgressActivityDataObject} is used for communicating
 * {@link ProgressActivity}s to peers.
 */
@XStreamAlias("ProgressActivity")
public class ProgressActivityDataObject extends AbstractActivityDataObject {

    @XStreamAsAttribute
    protected String progressID;

    @XStreamAsAttribute
    protected int workCurrent;

    @XStreamAsAttribute
    protected int workTotal;

    protected String taskName;

    @XStreamAsAttribute
    protected ProgressAction action;

    @XStreamAsAttribute
    @XStreamConverter(JIDConverter.class)
    protected JID target;

    public ProgressActivityDataObject(JID source, JID target,
        String progressID, int workCurrent, int workTotal, String taskName,
        ProgressAction action) {
        super(source);
        this.target = target;
        this.progressID = progressID;
        this.workCurrent = workCurrent;
        this.workTotal = workTotal;
        this.taskName = taskName;
        this.action = action;
    }

    @Override
    public String toString() {
        return "Progress(source:" + getSource() + ",target:" + target + ",id:"
            + this.progressID + ",work:" + workCurrent + "/" + workTotal
            + ",task:" + taskName + ",action:" + action + ")";
    }

    @Override
    public IActivity getActivity(ISarosSession sarosSession) {

        User source = sarosSession.getUser(getSource());
        if (source == null)
            throw new IllegalArgumentException("user " + getSource()
                + " is not in the current session");

        return new ProgressActivity(source, sarosSession.getUser(target),
            progressID, workCurrent, workTotal, taskName, action);
    }
}
