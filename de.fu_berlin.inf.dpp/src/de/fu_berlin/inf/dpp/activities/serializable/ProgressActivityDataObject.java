package de.fu_berlin.inf.dpp.activities.serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.ProgressActivity;
import de.fu_berlin.inf.dpp.activities.business.ProgressActivity.ProgressAction;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;

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

    public ProgressActivityDataObject(JID source, String progressID,
        int workCurrent, int workTotal, String taskName, ProgressAction action) {
        super(source);
        this.progressID = progressID;
        this.workCurrent = workCurrent;
        this.workTotal = workTotal;
        this.taskName = taskName;
        this.action = action;
    }

    @Override
    public String toString() {
        return "Progress(source:" + getSource() + ", id:" + this.progressID
            + ",work:" + workCurrent + "/" + workTotal + ",task:" + taskName
            + ",action:" + action + ")";
    }

    public IActivity getActivity(ISarosSession sarosSession) {

        User user = sarosSession.getUser(source);
        if (user == null)
            throw new IllegalArgumentException("Buddy is not in shared project");

        return new ProgressActivity(user, progressID, workCurrent, workTotal,
            taskName, action);
    }
}
