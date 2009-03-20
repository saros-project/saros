package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import de.fu_berlin.inf.dpp.net.JID;

/**
 * interface for testing document state and content.
 * 
 * @author troll
 * 
 */
public interface DocumentTestChecker {

    public JID getJID();

    public String getDocument();
}
