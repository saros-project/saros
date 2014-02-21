package de.fu_berlin.inf.dpp.activities.business;

import org.apache.commons.lang.ObjectUtils;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.JupiterActivityDataObject;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * A JupiterActivity is an Activity that can be handled by the Jupiter
 * Algorithm.
 */
public class JupiterActivity extends AbstractActivity implements
    IResourceActivity {

    /**
     * Timestamp that specifies the definition context of the enclosed
     * operation.
     */
    private final Timestamp timestamp;
    private final Operation operation;
    private final SPath path;

    public JupiterActivity(Timestamp timestamp, Operation operation,
        User source, SPath path) {

        super(source);

        this.timestamp = timestamp;
        this.operation = operation;
        this.path = path;
    }

    public Operation getOperation() {
        return this.operation;
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof JupiterActivity))
            return false;

        JupiterActivity other = (JupiterActivity) obj;

        if (!ObjectUtils.equals(this.path, other.path))
            return false;
        if (!ObjectUtils.equals(this.operation, other.operation))
            return false;
        if (!ObjectUtils.equals(this.timestamp, other.timestamp))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ObjectUtils.hashCode(path);
        result = prime * result + ObjectUtils.hashCode(operation);
        result = prime * result + ObjectUtils.hashCode(timestamp);
        return result;
    }

    @Override
    public String toString() {
        return "JupiterActivity(timestamp: " + timestamp + ", operation: "
            + operation + ", source: " + getSource() + ")";
    }

    @Override
    public SPath getPath() {
        return this.path;
    }

    @Override
    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    @Override
    public IActivityDataObject getActivityDataObject(
        ISarosSession sarosSession, IPathFactory pathFactory) {
        return new JupiterActivityDataObject(timestamp, operation, getSource()
            .getJID(), (path != null ? path.toSPathDataObject(sarosSession,
            pathFactory) : null));
    }
}
