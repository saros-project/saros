package de.fu_berlin.inf.dpp.stf.sarosswtbot;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.limewire.collection.Tuple;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SessionManager;

/**
 * The goal of this class is to gather state and provide an RMI interface for
 * getting internal states from the outside.
 */
public class SarosState implements ISarosState {

    private transient static final Logger log = Logger
        .getLogger(SarosState.class);

    public SarosState() {
        // Default constructor needed for RMI
    }

    public SarosState(Saros saros, SessionManager sessionManager,
        DataTransferManager dataTransferManager) {
        this.saros = saros;
        this.sessionManager = sessionManager;
        this.dataTransferManager = dataTransferManager;
    }

    protected transient Saros saros;

    protected transient SessionManager sessionManager;

    protected transient DataTransferManager dataTransferManager;

    public boolean areDrivers(List<JID> jids) {
        boolean result = true;
        for (JID jid : jids) {
            try {
                ISarosSession sarosSession = sessionManager.getSarosSession();
                User user = sarosSession.getUser(jid);
                result &= sarosSession.getDrivers().contains(user);
            } catch (Exception e) {
                return false;
            }
        }
        return result;
    }

    public boolean areObservers(List<JID> jids) {
        boolean result = true;
        for (JID jid : jids) {
            try {
                ISarosSession sarosSession = sessionManager.getSarosSession();
                User user = sarosSession.getUser(jid);
                result &= sarosSession.getObservers().contains(user);
            } catch (Exception e) {
                return false;
            }
        }
        return result;
    }

    public boolean areParticipants(List<JID> jids) {
        boolean result = true;
        for (JID jid : jids) {
            try {
                ISarosSession sarosSession = sessionManager.getSarosSession();
                result &= sarosSession.getParticipants().contains(
                    sarosSession.getUser(jid));
            } catch (Exception e) {
                return false;
            }
        }
        return result;
    }

    public Tuple<NetTransferMode, NetTransferMode> getConnection(JID destJid) {
        NetTransferMode outgoingMode = dataTransferManager
            .getTransferMode(destJid);
        NetTransferMode incomingMode = dataTransferManager
            .getTransferMode(destJid);
        return new Tuple<NetTransferMode, NetTransferMode>(incomingMode,
            outgoingMode);
    }

    public ConnectionState getXmppConnectionState() {
        return saros.getConnectionState();
    }

    public boolean hasContact(JID jid) throws RemoteException {
        return saros.getRoster().contains(jid.getBase());
    }

    public boolean isConnectedByXMPP() {
        return saros.isConnected();
    }

    public boolean isDriver(JID jid) throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        User user = sarosSession.getUser(jid);
        log.debug("isDriver(" + jid.toString() + ") == "
            + sarosSession.getDrivers().contains(user));
        return sarosSession.getDrivers().contains(user);
    }

    public boolean isIncomingConnectionIBB(JID destJid) throws RemoteException {
        return dataTransferManager.getTransferMode(destJid).equals(
            NetTransferMode.IBB);
    }

    public boolean isIncomingConnectionJingleTCP(JID destJid)
        throws RemoteException {
        return dataTransferManager.getTransferMode(destJid).equals(
            NetTransferMode.JINGLETCP);
    }

    public boolean isIncomingConnectionJingleUDP(JID destJid)
        throws RemoteException {
        return dataTransferManager.getTransferMode(destJid).equals(
            NetTransferMode.JINGLEUDP);
    }

    public boolean isIncomingConnectionSocks5ByteStream(JID destJid)
        throws RemoteException {
        throw new NotImplementedException(
            "We can not get NetTransferMode Socks5ByteStream connection in Saros yet.");
    }

    public boolean isObserver(JID jid) throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        User user = sarosSession.getUser(jid);
        log.debug("isObserver(" + jid.toString() + ") == "
            + sarosSession.getObservers().contains(user));
        return sarosSession.getObservers().contains(user);
    }

    public boolean isOutgoingConnectionIBB(JID destJid) throws RemoteException {
        return dataTransferManager.getTransferMode(destJid).equals(
            NetTransferMode.IBB);
    }

    public boolean isOutgoingConnectionJingleTCP(JID destJid)
        throws RemoteException {
        return dataTransferManager.getTransferMode(destJid).equals(
            NetTransferMode.JINGLETCP);
    }

    public boolean isOutgoingConnectionJingleUDP(JID destJid)
        throws RemoteException {
        return dataTransferManager.getTransferMode(destJid).equals(
            NetTransferMode.JINGLEUDP);
    }

    public boolean isOutgoingConnectionSocks5ByteStream(JID destJid)
        throws RemoteException {
        throw new NotImplementedException(
            "We can not get NetTransferMode Socks5ByteStream connection in Saros yet.");
    }

    public boolean isParticipant(JID jid) throws RemoteException {
        try {
            ISarosSession sarosSession = sessionManager.getSarosSession();
            log.debug("isParticipant(" + jid.toString() + ") == "
                + sarosSession.getParticipants().contains(sarosSession.getUser(jid)));
            return sarosSession.getParticipants().contains(sarosSession.getUser(jid));
        } catch (Exception e) {
            return false;
        }
    }

}
