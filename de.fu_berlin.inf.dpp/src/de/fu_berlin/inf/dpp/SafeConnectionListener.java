package de.fu_berlin.inf.dpp;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.ConnectionListener;

import de.fu_berlin.inf.dpp.util.Util;

/**
 * A connection listener which forwards calls to a another ConnectionListener,
 * but catches all exception which might have occur in the forwarded to
 * ConnectionListener and prints them to the log given in the constructor.
 * 
 * @pattern Proxy which adds the aspect of "safety"
 */
public class SafeConnectionListener implements ConnectionListener {

    /**
     * The {@link ConnectionListener} to forward all call to which are received
     * by this {@link ConnectionListener}
     */
    protected ConnectionListener toForwardTo;

    /**
     * The {@link Logger} to use for printing an error message when a
     * RuntimeException occurs when calling the {@link #toForwardTo}
     * {@link ConnectionListener}.
     */
    protected Logger logger;

    public SafeConnectionListener(Logger logger, ConnectionListener toForwardTo) {
        this.toForwardTo = toForwardTo;
        this.logger = logger;
    }

    public void connectionClosed() {
        Util.runSafeSync(logger, new Runnable() {
            public void run() {
                toForwardTo.connectionClosed();
            }
        });
    }

    public void connectionClosedOnError(final Exception e) {
        Util.runSafeSync(logger, new Runnable() {
            public void run() {
                toForwardTo.connectionClosedOnError(e);
            }
        });
    }

    public void reconnectingIn(final int seconds) {
        Util.runSafeSync(logger, new Runnable() {
            public void run() {
                toForwardTo.reconnectingIn(seconds);
            }
        });

    }

    public void reconnectionFailed(final Exception e) {
        Util.runSafeSync(logger, new Runnable() {
            public void run() {
                toForwardTo.reconnectionFailed(e);
            }
        });
    }

    public void reconnectionSuccessful() {
        Util.runSafeSync(logger, new Runnable() {
            public void run() {
                toForwardTo.reconnectionSuccessful();
            }
        });
    }
}