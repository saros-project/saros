package de.fu_berlin.inf.dpp.concurrent.jupiter.internal;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * Default implementation of the Request interface.
 */
public class RequestImpl implements Request {

    private static final long serialVersionUID = 2843724349387550074L;

    /**
     * The site id of the request.
     */
    private final int siteId;

    /**
     * The timestamp of the request.
     */
    private final Timestamp timestamp;

    /**
     * The operation of the request.
     */
    private final Operation operation;

    /**
     * JID of appropriate client.
     */
    private JID jid;

    /**
     * Path of the appropriate editor.
     */
    private IPath editor;

    /**
     * Creates a new instance of the RequestImpl class.
     * 
     * @param siteId
     *            the site id
     * @param timestamp
     *            the timestamp
     * @param operation
     *            the operation
     */
    public RequestImpl(int siteId, Timestamp timestamp, Operation operation) {
        this.siteId = siteId;
        this.timestamp = timestamp;
        this.operation = operation;
        this.jid = null;
    }

    /**
     * Creates a new instance of the RequestImpl class.
     * 
     * @param siteId
     *            the site id
     * @param timestamp
     *            the timestamp
     * @param operation
     *            the operation
     */
    public RequestImpl(int siteId, Timestamp timestamp, Operation operation,
        JID jid) {
        this.siteId = siteId;
        this.timestamp = timestamp;
        this.operation = operation;
        this.jid = jid;
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
        if (this == obj) {
            return true;
        } else if (obj instanceof Request) {
            Request request = (Request) obj;
            return (this.siteId == request.getSiteId())
                && ObjectUtils.equals(this.timestamp, request.getTimestamp())
                && ObjectUtils.equals(this.operation, request.getOperation());
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hashCode = 13 * this.siteId;
        hashCode += (this.timestamp != null) ? 17 * this.timestamp.hashCode()
            : 0;
        hashCode += (this.operation != null) ? 29 * this.operation.hashCode()
            : 0;
        return hashCode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
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

    public void setJID(JID jid) {
        this.jid = jid;
    }

    public IPath getEditorPath() {
        return this.editor;
    }

    public void setEditorPath(IPath editor) {
        this.editor = editor;
    }

}
