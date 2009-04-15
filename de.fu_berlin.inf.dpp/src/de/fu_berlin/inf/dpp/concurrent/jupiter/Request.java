package de.fu_berlin.inf.dpp.concurrent.jupiter;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.activities.AbstractActivity;
import de.fu_berlin.inf.dpp.activities.IActivityReceiver;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * A Request is an Activity that can be handled by the Jupiter Algorithm.
 */
public class Request extends AbstractActivity {

    /**
     * identifier of the sending site
     */
    private final int siteId;

    /**
     * Timestamp that specifies the definition context of the enclosed
     * operation.
     */
    private final Timestamp timestamp;

    private final Operation operation;

    private JID jid;

    private IPath editor;

    public Request(int siteId, Timestamp timestamp, Operation operation,
        JID jid, IPath editor) {
        super(jid.toString());
        this.siteId = siteId;
        this.timestamp = timestamp;
        this.operation = operation;
        this.jid = jid;
        this.editor = editor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.jupiter.Request#getSiteId()
     */
    public int getSiteId() {
        return this.siteId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.jupiter.Request#getOperation()
     */
    public Operation getOperation() {
        return this.operation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.jupiter.Request#getTimestamp()
     */
    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Request other = (Request) obj;
        if (operation == null) {
            if (other.operation != null)
                return false;
        } else if (!operation.equals(other.operation))
            return false;
        if (siteId != other.siteId)
            return false;
        if (timestamp == null) {
            if (other.timestamp != null)
                return false;
        } else if (!timestamp.equals(other.timestamp))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
            + ((operation == null) ? 0 : operation.hashCode());
        result = prime * result + siteId;
        result = prime * result
            + ((timestamp == null) ? 0 : timestamp.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("request(");
        buffer.append(this.siteId);
        buffer.append(",");
        buffer.append(this.timestamp);
        buffer.append(",");
        buffer.append(this.operation);
        buffer.append(",");
        buffer.append(this.jid);
        buffer.append(")");
        return buffer.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.jupiter.Request#getJID()
     */
    public JID getJID() {
        return this.jid;
    }

    public IPath getEditorPath() {
        return this.editor;
    }

    public boolean dispatch(IActivityReceiver receiver) {
        return receiver.receive(this);
    }

}
