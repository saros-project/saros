package de.fu_berlin.inf.dpp.invitation;

/**
 * Simple listener interface for signaling termination of
 * {@link SessionNegotiation} and {@link ProjectNegotiation} negotiation
 * processes.
 * 
 * @author srossbach
 */
public interface ProcessListener {

    /**
     * Called when a session negotiation process has been terminated
     * 
     * @param process
     *            the session negotiation process that was terminated
     */
    public void processTerminated(SessionNegotiation process);

    /**
     * Called when a project negotiation process has been terminated
     * 
     * @param process
     *            the project negotiation process that was terminated
     */
    public void processTerminated(ProjectNegotiation process);
}
