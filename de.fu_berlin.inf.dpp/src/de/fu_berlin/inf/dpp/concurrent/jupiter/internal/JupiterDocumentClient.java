package de.fu_berlin.inf.dpp.concurrent.jupiter.internal;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterClient;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * The client-side handling for a single document
 */
public class JupiterDocumentClient implements JupiterClient {

    /**
     * JID of remote client
     */
    private final JID jid;

    /**
     * jupiter sync algorithm.
     */
    private final Algorithm jupiter;

    private IPath editor;

    public JupiterDocumentClient(JID jid, IPath editor) {
        this.jid = jid;
        this.jupiter = new Jupiter(true);
        this.editor = editor;
    }

    public Request generateRequest(Operation op) {
        Request req = this.jupiter.generateRequest(op);
        req.setJID(this.jid);
        req.setEditorPath(this.editor);

        return req;
    }

    public Operation receiveRequest(Request req) throws TransformationException {
        return this.jupiter.receiveRequest(req);
    }

    public JID getJID() {
        return this.jid;
    }

    public Timestamp getTimestamp() {
        return this.jupiter.getTimestamp();
    }

    public void updateVectorTime(Timestamp timestamp)
        throws TransformationException {
        this.jupiter.updateVectorTime(timestamp);
    }

}
