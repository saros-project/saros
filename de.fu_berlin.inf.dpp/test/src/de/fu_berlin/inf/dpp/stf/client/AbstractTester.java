package de.fu_berlin.inf.dpp.stf.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.noFinder.NoBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.SarosBot;

/**
 * SuperClass of {@link DummyTester} and {@link RealTester}. It define all
 * methods supported by {@link AbstractTester}
 */
public abstract class AbstractTester {

    public AbstractTester() {
        //
    }

    /**
     * get remote registered objects bot {@link STFBot} and superBot
     * {@link SarosBot}
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
     * @return the simple {@link STFBot}, with which tester can remotely access
     *         widgets of saros-instance
     */
    public abstract STFWorkbenchBot bot();

    public abstract NoBot noBot();

    /**
     * 
     * @return the super {@link SarosBot}, which encapsulate some often used
     *         actions e.g. shareProject, connect, leaveSession.., which can be
     *         also done with {@link STFBot}
     * @throws RemoteException
     */
    public abstract SarosBot sarosBot() throws RemoteException;

}