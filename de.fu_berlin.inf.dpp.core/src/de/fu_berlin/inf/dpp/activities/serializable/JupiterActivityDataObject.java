package de.fu_berlin.inf.dpp.activities.serializable;

import org.apache.commons.lang.ObjectUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;

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
    @XStreamAlias("t")
    protected final Timestamp timestamp;

    @XStreamAlias("o")
    protected final Operation operation;

    public JupiterActivityDataObject(Timestamp timestamp, Operation operation,
        JID source, SPathDataObject path) {

        super(source, path);

        this.timestamp = timestamp;
        this.operation = operation;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof JupiterActivityDataObject))
            return false;

        JupiterActivityDataObject other = (JupiterActivityDataObject) obj;

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
        result = prime * result + ObjectUtils.hashCode(operation);
        result = prime * result + ObjectUtils.hashCode(timestamp);
        return result;
    }

    @Override
    public String toString() {
        return "JupiterActivityDO(timestamp: " + timestamp + ", operation: "
            + operation + ", path: " + getPath() + ", source: " + getSource()
            + ")";
    }

    @Override
    public IActivity getActivity(ISarosSession sarosSession,
        IPathFactory pathFactory) {
        return new JupiterActivity(timestamp, operation,
            sarosSession.getUser(getSource()), getPath() != null ? getPath()
                .toSPath(sarosSession, pathFactory) : null);
    }
}
