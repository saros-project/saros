package de.fu_berlin.inf.dpp.concurrent.jupiter;

public interface RequestForwarder {

	/**
	 * Add generate request for transfering via network.
	 * @param req
	 */
	public void forwardOutgoingRequest(Request req);
	
	/**
	 * get next request for transfer.
	 * @return
	 * @throws InterruptedException 
	 */
	public Request getNextOutgoingRequest() throws InterruptedException;
}
