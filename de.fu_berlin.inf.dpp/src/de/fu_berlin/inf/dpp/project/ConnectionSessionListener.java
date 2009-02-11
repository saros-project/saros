/**
 * 
 */
package de.fu_berlin.inf.dpp.project;

import org.jivesoftware.smack.XMPPConnection;

public interface ConnectionSessionListener {

    public void prepare(XMPPConnection connection);

    public void start();

    public void stop();

    public void dispose();

}