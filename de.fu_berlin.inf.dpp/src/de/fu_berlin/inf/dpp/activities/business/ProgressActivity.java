package de.fu_berlin.inf.dpp.activities.business;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ProgressActivityDataObject;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.ui.RemoteProgressManager;

/**
 * A {@link ProgressActivity} is used for controlling a progress bar at a remote
 * peer.
 * 
 * A {@link ProgressActivity} is managed by the {@link RemoteProgressManager}
 */
public class ProgressActivity extends AbstractActivity {

    protected String progressID;

    protected int workCurrent;

    protected int workTotal;

    protected String taskName;

    protected ProgressAction action;

    public enum ProgressAction {
        UPDATE, DONE;
    }

    public ProgressActivity(User source, String progressID, int workCurrent,
        int workTotal, String taskName, ProgressAction action) {
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

    public IActivityDataObject getActivityDataObject(ISarosSession sarosSession) {

        return new ProgressActivityDataObject(getSource().getJID(), progressID,
            workCurrent, workTotal, taskName, action);
    }

    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    /**
     * The unique ID by which local users and remote peers can identify the
     * progress process.
     */
    public String getProgressID() {
        return progressID;
    }

    /**
     * The current position the progress bar should be shown at (in relation to
     * {@link #getWorkTotal()}.
     * 
     * For instance if {@link #workCurrent} == 3 and {@link #workTotal} == 10,
     * then the progress bar should be shown at 30% of the total width of the
     * progress dialog.
     */
    public int getWorkCurrent() {
        return workCurrent;
    }

    /**
     * The total amount of work associated with this progress process.
     */
    public int getWorkTotal() {
        return workTotal;
    }

    /**
     * A human readable description of the work currently being done as part of
     * the process about which this ProgressActivity is informing about.
     * 
     * @return a new human readable description of the work currently being done
     *         or null if the task did not change.
     */
    public String getTaskName() {
        return taskName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result
            + ((progressID == null) ? 0 : progressID.hashCode());
        result = prime * result
            + ((taskName == null) ? 0 : taskName.hashCode());
        result = prime * result + workCurrent;
        result = prime * result + workTotal;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProgressActivity other = (ProgressActivity) obj;
        if (action == null) {
            if (other.action != null)
                return false;
        } else if (!action.equals(other.action))
            return false;
        if (progressID == null) {
            if (other.progressID != null)
                return false;
        } else if (!progressID.equals(other.progressID))
            return false;
        if (taskName == null) {
            if (other.taskName != null)
                return false;
        } else if (!taskName.equals(other.taskName))
            return false;
        if (workCurrent != other.workCurrent)
            return false;
        if (workTotal != other.workTotal)
            return false;
        return true;
    }

    /**
     * The action describes whether the associated progress bar at the remote
     * site should be updated or closed.
     * 
     * Once a remote progress has been closed the peer might discard all
     * updates.
     */
    public ProgressAction getAction() {
        return action;
    }
}
