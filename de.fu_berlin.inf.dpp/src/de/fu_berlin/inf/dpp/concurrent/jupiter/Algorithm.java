package de.fu_berlin.inf.dpp.concurrent.jupiter;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.activities.business.JupiterActivity;
import de.fu_berlin.inf.dpp.net.JID;

public interface Algorithm {

    /**
     * Gets the current timestamp at the local site.
     * 
     * @return the current timestamp
     */
    Timestamp getTimestamp();

    /**
     * Generates a JupiterActivity for the given operation. The operation is a
     * locally generated operation. The returned JupiterActivity must be sent to
     * the other sites.
     * 
     * @param op
     *            the operation for which a JupiterActivity should be generated
     * @return the generated JupiterActivity
     * @see JupiterActivity
     */
    JupiterActivity generateJupiterActivity(Operation op, JID jid, IPath editor);

    /**
     * Receives a JupiterActivity from a remote site. The JupiterActivity must
     * be transformed and the resulting operation is returned.
     * 
     * @param jupiterActivity
     *            the JupiterActivity to transform and apply
     * @return the transformed Operation
     */
    Operation receiveJupiterActivity(JupiterActivity jupiterActivity)
        throws TransformationException;

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
