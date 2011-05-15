package de.fu_berlin.inf.dpp.stf.client.tester;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.IRemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.ISuperBot;

/**
 * DummyTester is responsible to check if a tester is already initialized.
 */
public class DummyTester extends AbstractTester {
    private String name;

    public DummyTester(String name) {
        this.name = name;
    }

    @Override
    public void getRegistriedRmiObject() throws RemoteException,
        NotBoundException, AccessException {
        throwException();
    }

    /**
     * @Return the name segment of {@link JID}.
     */
    @Override
    public String getName() {
        throwException();
        return null;
    }

    /**
     * @Return the JID without resource qualifier.
     */
    @Override
    public String getBaseJid() {
        throwException();
        return null;
    }

    /**
     * @Return the resource qualified {@link JID}.
     */
    @Override
    public String getRQjid() {
        throwException();
        return null;
    }

    @Override
    public String getDomain() {
        throwException();
        return null;
    }

    @Override
    public IRemoteWorkbenchBot remoteBot() {
        throwException();
        return null;
    }

    @Override
    public ISuperBot superBot() throws RemoteException {
        throwException();
        return null;
    }

    @Override
    public JID getJID() {
        throwException();
        return null;
    }

    @Override
    public String getPassword() {
        throwException();
        return null;
    }

    private void throwException() {
        throw new RuntimeException("Tester " + name + " is not initialized");
    }

}