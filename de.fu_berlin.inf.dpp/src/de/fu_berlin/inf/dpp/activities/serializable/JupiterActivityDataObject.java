package de.fu_berlin.inf.dpp.activities.serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * A JupiterActivityDataObject is an Activity that can be handled by the Jupiter
 * Algorithm.
 */
@XStreamAlias("jupiterActivity")
public class JupiterActivityDataObject extends
    AbstractProjectActivityDataObject {

    /**
     * Timestamp that specifies the definition context of the enclosed
     * operation.
     */
    protected final Timestamp timestamp;

    protected final Operation operation;

    public JupiterActivityDataObject(Timestamp timestamp, Operation operation,
        JID source, SPathDataObject path) {
        super(source, path);
        this.timestamp = timestamp;
        this.operation = operation;
        this.path = path;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("JupiterActivityDataObject(");
        buffer.append(this.timestamp);
        buffer.append(",");
        buffer.append(this.operation);
        buffer.append(",");
        buffer.append(this.getSource());
        buffer.append(")");
        return buffer.toString();
    }

    public IActivity getActivity(ISarosSession sarosSession) {
        return new JupiterActivity(timestamp, operation,
            sarosSession.getUser(source), path.toSPath(sarosSession));
    }
}
