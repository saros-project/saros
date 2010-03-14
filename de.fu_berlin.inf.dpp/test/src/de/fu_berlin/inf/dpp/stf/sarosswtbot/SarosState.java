package de.fu_berlin.inf.dpp.stf.sarosswtbot;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.limewire.collection.Tuple;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;

/**
 * The goal of this class is to gather state and provide an RMI interface for
 * getting internal states from the outside.
 */
public class SarosState implements ISarosState {
    private transient static final Logger log = Logger
        .getLogger(SarosState.class);

    @Inject
    protected transient Saros saros;

    @Inject
    protected transient SessionManager sessionManager;

    @Inject
    protected transient DataTransferManager dataTransferManager;

    public boolean areDrivers(List<JID> jids) {
        boolean result = true;
        for (JID jid : jids) {
            try {
                ISharedProject project = sessionManager.getSharedProject();
                User user = project.getUser(jid);
                result &= project.getDrivers().contains(user);
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
                ISharedProject project = sessionManager.getSharedProject();
                User user = project.getUser(jid);
                result &= project.getObservers().contains(user);
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
                ISharedProject project = sessionManager.getSharedProject();
                result &= project.getParticipants().contains(
                    project.getUser(jid));
            } catch (Exception e) {
                return false;
            }
        }
        return result;
    }

    public Tuple<NetTransferMode, NetTransferMode> getConnection(JID destJid) {
        NetTransferMode outgoingMode = dataTransferManager
            .getOutgoingTransferMode(destJid);
        NetTransferMode incomingMode = dataTransferManager
            .getIncomingTransferMode(destJid);
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
        ISharedProject project = sessionManager.getSharedProject();
        User user = project.getUser(jid);
        log.debug("isDriver(" + jid.toString() + ") == "
            + project.getDrivers().contains(user));
        return project.getDrivers().contains(user);
    }

    public boolean isIncomingConnectionIBB(JID destJid) throws RemoteException {
        return dataTransferManager.getIncomingTransferMode(destJid).equals(
            NetTransferMode.IBB);
    }

    public boolean isIncomingConnectionJingleTCP(JID destJid)
        throws RemoteException {
        return dataTransferManager.getIncomingTransferMode(destJid).equals(
            NetTransferMode.JINGLETCP);
    }

    public boolean isIncomingConnectionJingleUDP(JID destJid)
        throws RemoteException {
        return dataTransferManager.getIncomingTransferMode(destJid).equals(
            NetTransferMode.JINGLEUDP);
    }

    public boolean isIncomingConnectionSocks5ByteStream(JID destJid)
        throws RemoteException {
        throw new NotImplementedException(
            "We can not get NetTransferMode Socks5ByteStream connection in Saros yet.");
    }

    public boolean isObserver(JID jid) throws RemoteException {
        ISharedProject project = sessionManager.getSharedProject();
        User user = project.getUser(jid);
        log.debug("isObserver(" + jid.toString() + ") == "
            + project.getObservers().contains(user));
        return project.getObservers().contains(user);
    }

    public boolean isOutgoingConnectionIBB(JID destJid) throws RemoteException {
        return dataTransferManager.getOutgoingTransferMode(destJid).equals(
            NetTransferMode.IBB);
    }

    public boolean isOutgoingConnectionJingleTCP(JID destJid)
        throws RemoteException {
        return dataTransferManager.getOutgoingTransferMode(destJid).equals(
            NetTransferMode.JINGLETCP);
    }

    public boolean isOutgoingConnectionJingleUDP(JID destJid)
        throws RemoteException {
        return dataTransferManager.getOutgoingTransferMode(destJid).equals(
            NetTransferMode.JINGLEUDP);
    }

    public boolean isOutgoingConnectionSocks5ByteStream(JID destJid)
        throws RemoteException {
        throw new NotImplementedException(
            "We can not get NetTransferMode Socks5ByteStream connection in Saros yet.");
    }

    public boolean isParticipant(JID jid) throws RemoteException {
        try {
            ISharedProject project = sessionManager.getSharedProject();
            log.debug("isParticipant(" + jid.toString() + ") == "
                + project.getParticipants().contains(project.getUser(jid)));
            return project.getParticipants().contains(project.getUser(jid));
        } catch (Exception e) {
            return false;
        }
    }

}
