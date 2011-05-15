package de.fu_berlin.inf.dpp.stf.client.tester;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.IRemoteBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.IRemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.ISuperBot;

/**
 * SuperClass of {@link DummyTester} and {@link RealTester}. It define all
 * methods supported by {@link AbstractTester}
 */
public abstract class AbstractTester {

    public AbstractTester() {
        //
    }

    /**
     * get remote registered objects bot {@link IRemoteBot} and superBot
     * {@link ISuperBot}
     * 
     * @throws RemoteException
     * @throws NotBoundException
     * @throws AccessException
     */
    public abstract void getRegistriedRmiObject() throws RemoteException,
        NotBoundException, AccessException;

    /**
     * @Return the name segment of {@link JID}.
     */
    public abstract String getName();

    /**
     * @Return the JID without resource qualifier.
     */
    public abstract String getBaseJid();

    /**
     * @Return the resource qualified {@link JID}.
     */
    public abstract String getRQjid();

    /**
     * 
     * @return the domain of {@link JID}
     */
    public abstract String getDomain();

    /**
     * 
     * @return {@link JID}
     */
    public abstract JID getJID();

    /**
     * 
     * @return password of tester account.
     */
    public abstract String getPassword();

    /**
     * 
     * @return the simple {@link IRemoteBot}, with which tester can remotely access
     *         widgets of saros-instance
     */
    public abstract IRemoteWorkbenchBot remoteBot();

    /**
     * 
     * @return the super {@link ISuperBot}, which encapsulate some often used
     *         actions e.g. shareProject, connect, leaveSession.., which can be
     *         also done with {@link IRemoteBot}
     * @throws RemoteException
     */
    public abstract ISuperBot superBot() throws RemoteException;

}