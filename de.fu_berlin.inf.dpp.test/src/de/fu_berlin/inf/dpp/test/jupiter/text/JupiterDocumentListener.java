package de.fu_berlin.inf.dpp.test.jupiter.text;

import de.fu_berlin.inf.dpp.net.JID;

/**
 * Listener for jupiter document actions.
 * 
 * @author orieger
 * 
 */
public interface JupiterDocumentListener {

    public void documentAction(JID jid);

    public String getID();
}
