package de.fu_berlin.inf.dpp.concurrent.jupiter;

public interface SynchronizedQueue {

    /**
     * add request to synchronized queue.
     * 
     * @param request
     *            the new request.
     */
    public void addRequest(Request request);

    /**
     * Gets first request in queue.
     * 
     * @return
     * @throws InterruptedException
     */
    public Request getNextRequestInSynchronizedQueue()
            throws InterruptedException;
}
