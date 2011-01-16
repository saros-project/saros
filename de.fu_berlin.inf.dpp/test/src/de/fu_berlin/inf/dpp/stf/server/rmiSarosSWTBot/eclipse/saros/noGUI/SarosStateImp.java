package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.saros.noGUI;

import java.rmi.RemoteException;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.limewire.collection.Tuple;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipsePart;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews.ChatViewImp;

public class SarosStateImp extends EclipsePart implements SarosState {

    private transient static final Logger log = Logger
        .getLogger(SarosStateImp.class);

    private static transient SarosStateImp self;

    /**
     * {@link ChatViewImp} is a singleton, but inheritance is possible.
     */
    public static SarosStateImp getInstance() {
        if (self != null)
            return self;
        self = new SarosStateImp();
        return self;
    }

    /**********************************************
     * 
     * gather state and perform actions using {@link DataTransferManager}
     * 
     **********************************************/
    private Tuple<NetTransferMode, NetTransferMode> getConnection(JID destJid) {
        NetTransferMode outgoingMode = dataTransferManager
            .getTransferMode(destJid);
        NetTransferMode incomingMode = dataTransferManager
            .getTransferMode(destJid);
        return new Tuple<NetTransferMode, NetTransferMode>(incomingMode,
            outgoingMode);
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

    public void debug(Object message) {
        log.debug(message);
    }

    public void debug(Object message, Throwable t) {
        log.debug(message, t);
    }
}
