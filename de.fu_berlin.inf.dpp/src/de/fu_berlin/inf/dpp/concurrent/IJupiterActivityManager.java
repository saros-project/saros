package de.fu_berlin.inf.dpp.concurrent;

import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterActivity;

/**
 * received JupiterActivities from other clients over network.
 * 
 * @author orieger
 * 
 */
public interface IJupiterActivityManager {

    public void receiveRequest(JupiterActivity jupiterActivity);
}