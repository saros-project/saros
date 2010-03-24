/**
 * 
 */
package de.fu_berlin.inf.dpp.project;

import org.jivesoftware.smack.XMPPConnection;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;

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
 * @component All implementors need to be added to the PicoContainer in the
 *            central plug-in class {@link Saros} to be automatically injected
 *            into the {@link ConnectionSessionManager}
 */
@Component(module = "net")
public interface ConnectionSessionListener {

    /**
     * Called when a new XMPPConnection is created and users of the connection
     * may now add themselves as listeners to the connection. After being called
     * with {@link #prepareConnection(XMPPConnection)} listeners should be ready
     * to receive data, but should not send data themselves until
     * {@link #startConnection()} has been called.
     */
    public void prepareConnection(XMPPConnection connection);

    /**
     * Called when the listener to the connection may now start sending data.
     */
    public void startConnection();

    /**
     * Called when the listener to the connection should not send any more data
     * to the connection. They still should be prepared to receive data.
     */
    public void stopConnection();

    /**
     * This marks the end of the life-cycle of the current XMPPConnection (set
     * via {@link #prepareConnection(XMPPConnection)}.
     * 
     * After the call to {@link #disposeConnection()}, the next call will be to
     * {@link #prepareConnection(XMPPConnection)}.
     */
    public void disposeConnection();

}