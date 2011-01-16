package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.saros.noGUI;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;

/**
 * The goal of this class is to gather state and perform actions using
 * {@link Saros}, {@link SarosSessionManager}, {@link DataTransferManager},
 * {@link EditorManager} and {@link XMPPAccountStore} from the inside and
 * provide an RMI interface for getting internal states from the outside.
 */
public interface SarosState extends Remote {

    /**********************************************
     * 
     * gather state and perform actions using {@link SarosSessionManager}
     * 
     **********************************************/

    /**********************************************
     * 
     * gather state and perform actions using {@link DataTransferManager}
     * 
     **********************************************/
    /**
     * Returns true if the incoming connection from destJid was In-Band
     * Bytestream (XEP-0047).
     */
    public boolean isIncomingConnectionIBB(JID destJid) throws RemoteException;

    /**
     * @return <tt>true</tt>, if the incoming connection from destJid was Jingle
     *         TCP.
     */
    public boolean isIncomingConnectionJingleTCP(JID destJid)
        throws RemoteException;

    /**
     * @return <tt>true</tt>, if the incoming connection from destJid was Jingle
     *         UDP.
     */
    public boolean isIncomingConnectionJingleUDP(JID destJid)
        throws RemoteException;

    /**
     * @return <tt>true</tt>, if the incoming connection from destJid was Socks5
     *         Bytestream (XEP-0065).
     */
    public boolean isIncomingConnectionSocks5ByteStream(JID destJid)
        throws RemoteException;

    /**
     * @return <tt>true</tt>, if the outgoing connection to destJid was In-Band
     *         Bytestream (XEP-0047).
     */
    public boolean isOutgoingConnectionIBB(JID destJid) throws RemoteException;

    /**
     * @return <tt>true</tt>, if the outgoing connection to destJid was Jingle
     *         TCP.
     */
    public boolean isOutgoingConnectionJingleTCP(JID destJid)
        throws RemoteException;

    /**
     * @return <tt>true</tt>,if the outgoing connection to destJid was Jingle
     *         UDP.
     */
    public boolean isOutgoingConnectionJingleUDP(JID destJid)
        throws RemoteException;

    /**
     * @return <tt>true</tt>, if the outgoing connection to destJid was Socks5
     *         Bytestream (XEP-0065).
     */
    public boolean isOutgoingConnectionSocks5ByteStream(JID destJid)
        throws RemoteException;

    /**********************************************
     * 
     * gather state and perform actions using {@link Saros}
     * 
     **********************************************/

    /**
     * Please don't use this method, because {@link Saros#getMyJID()} return
     * null value.
     * 
     * TODO fix the method {@link Saros#getMyJID()}
     * 
     * @return JID of the local user
     * @throws RemoteException
     */
    // public JID getMyJID() throws RemoteException;

    /**********************************************
     * 
     * gather state and perform actions using {@link EditorManager}
     * 
     **********************************************/

    /**********************************************
     * 
     * infos about local user
     * 
     **********************************************/

    /**
     * @see org.apache.log4j.Category#debug(Object)
     */
    public void debug(Object message) throws RemoteException;

    /**
     * @see org.apache.log4j.Category#debug(Object, Throwable)
     */
    public void debug(Object message, Throwable t) throws RemoteException;

}
