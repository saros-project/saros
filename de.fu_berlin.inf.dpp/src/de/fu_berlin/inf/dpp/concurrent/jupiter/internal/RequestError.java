package de.fu_berlin.inf.dpp.concurrent.jupiter.internal;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * Error object of an transformation request.
 * 
 * @author orieger
 * 
 */
public class RequestError implements Request {

    private IPath path;

    public RequestError(IPath path) {
        this.path = path;
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = 3111067620344018430L;

    public IPath getEditorPath() {
        return this.path;
    }

    public JID getJID() {

        return null;
    }

    public Operation getOperation() {

        return null;
    }

    public int getSiteId() {

        return 0;
    }

    public Timestamp getTimestamp() {

        return null;
    }

    public void setEditorPath(IPath editor) {
        this.path = editor;
    }

    public void setJID(JID jid) {

    }

}
