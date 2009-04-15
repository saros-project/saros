package de.fu_berlin.inf.dpp.concurrent.jupiter;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.net.JID;

public interface Algorithm {

    /**
     * Gets the site id of this algorithm.
     * 
     * @return the site id
     */
    int getSiteId();

    /**
     * Gets the current timestamp at the local site.
     * 
     * @return the current timestamp
     */
    Timestamp getTimestamp();

    /**
     * Generates a request for the given operation. The operation is a locally
     * generated operation. The returned request must be sent to the other
     * sites.
     * 
     * @param op
     *            the operation for which a request should be generated
     * @return the generated request
     * @see Request
     */
    Request generateRequest(Operation op, JID jid, IPath editor);

    /**
     * Receives a request from a remote site. The request must be transformed
     * and the resulting operation is returned.
     * 
     * @param req
     *            the request to transform and apply
     * @return the transformed Operation
     */
    Operation receiveRequest(Request req) throws TransformationException;

    /**
     * Notifies the algorithm that the site specified by the site id has
     * processed the number of messages in the timestamp.
     * 
     * @param siteId
     *            the site id of the sending site
     * @param timestamp
     *            the timestamp at the other site
     * @throws TransformationException
     */
    void acknowledge(int siteId, Timestamp timestamp)
        throws TransformationException;

    /**
     * Transform the array of indices from the state indicated by the timestamp
     * to the current timestamp at the local site. The transformed indices are
     * returned to the client.
     * 
     * @param timestamp
     *            the timestamp at which the indices are valid
     * @param indices
     *            the array of integer indices
     * @return the transformed array of indices
     */
    int[] transformIndices(Timestamp timestamp, int[] indices)
        throws TransformationException;

    /**
     * 
     * @param timestamp
     * @throws TransformationException
     */
    void updateVectorTime(Timestamp timestamp) throws TransformationException;
}
