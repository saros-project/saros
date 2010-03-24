package de.fu_berlin.inf.dpp.net.jingle;

import de.fu_berlin.inf.dpp.net.JID;

/**
 * this exception is throw if jingle session request is failed.
 * 
 * @author orieger
 * 
 */
public class JingleSessionException extends Exception {

    /**
	 * 
	 */
    private static final long serialVersionUID = 4395402562918748941L;

    private String message;
    private JID jid;

    public JingleSessionException(String message) {
        super();
        this.message = message;
    }

    public JingleSessionException(String message, JID jid) {
        super();
        this.message = message;
        this.jid = jid;
    }

    public JingleSessionException(String message, Throwable cause) {
        super();
        this.message = message;
    }

    public JID getJID() {
        return jid;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
