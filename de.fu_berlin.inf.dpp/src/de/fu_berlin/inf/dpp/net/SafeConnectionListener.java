package de.fu_berlin.inf.dpp.net;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.ConnectionListener;

import de.fu_berlin.inf.dpp.util.ThreadUtils;

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
    protected Logger log;

    public SafeConnectionListener(Logger log, ConnectionListener toForwardTo) {
        this.toForwardTo = toForwardTo;
        this.log = log;
    }

    @Override
    public void connectionClosed() {
        ThreadUtils.runSafeSync(log, new Runnable() {
            @Override
            public void run() {
                toForwardTo.connectionClosed();
            }
        });
    }

    @Override
    public void connectionClosedOnError(final Exception e) {
        ThreadUtils.runSafeSync(log, new Runnable() {
            @Override
            public void run() {
                toForwardTo.connectionClosedOnError(e);
            }
        });
    }

    @Override
    public void reconnectingIn(final int seconds) {
        ThreadUtils.runSafeSync(log, new Runnable() {
            @Override
            public void run() {
                toForwardTo.reconnectingIn(seconds);
            }
        });

    }

    @Override
    public void reconnectionFailed(final Exception e) {
        ThreadUtils.runSafeSync(log, new Runnable() {
            @Override
            public void run() {
                toForwardTo.reconnectionFailed(e);
            }
        });
    }

    @Override
    public void reconnectionSuccessful() {
        ThreadUtils.runSafeSync(log, new Runnable() {
            @Override
            public void run() {
                toForwardTo.reconnectionSuccessful();
            }
        });
    }
}