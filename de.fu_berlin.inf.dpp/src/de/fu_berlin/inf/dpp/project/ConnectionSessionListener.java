/**
 * 
 */
package de.fu_berlin.inf.dpp.project;

import org.jivesoftware.smack.XMPPConnection;

/**
 * The general contract of this listener is that the order of calls to these
 * methods will always be in the following order:
 * 
 * prepare() -> start() <-> stop() -> dispose() -> prepare() ->...
 * 
 * So first is always prepare followed by start.
 * 
 * Then start() may lead to stop() from which the session may be disposed() or
 * started again.
 * 
 * Once the session has been disposed it can only be prepared again.
 * 
 */
public interface ConnectionSessionListener {

    public void prepare(XMPPConnection connection);

    public void start();

    public void stop();

    public void dispose();

}