package de.fu_berlin.inf.dpp.concurrent;

import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;

/**
 * received request from other clients over network.
 * @author orieger
 *
 */
public interface IRequestManager {

	public void receiveRequest(Request request);
}
