package de.fu_berlin.inf.dpp.stf.client.tester;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.IRemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.ISuperBot;

/**
 * DummyTester is responsible to check if a tester is already initialized.
 */
class InvalidTester implements AbstractTester {

    RuntimeException exception;

    public InvalidTester(RuntimeException exception) {
        this.exception = exception;
    }

    /**
     * @Return the name segment of {@link JID}.
     */

    public String getName() {
        throw exception;
    }

    /**
     * @Return the JID without resource qualifier.
     */

    public String getBaseJid() {
        throw exception;
    }

    /**
     * @Return the resource qualified {@link JID}.
     */

    public String getRqJid() {
        throw exception;
    }

    public String getDomain() {
        throw exception;
    }

    public IRemoteWorkbenchBot remoteBot() {
        throw exception;
    }

    public ISuperBot superBot() throws RemoteException {
        throw exception;
    }

    public JID getJID() {
        throw exception;
    }

    public String getPassword() {
        throw exception;
    }
}