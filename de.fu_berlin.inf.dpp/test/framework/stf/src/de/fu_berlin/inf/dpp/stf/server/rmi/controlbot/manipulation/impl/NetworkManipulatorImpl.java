package de.fu_berlin.inf.dpp.stf.server.rmi.controlbot.manipulation.impl;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.controlbot.manipulation.INetworkManipulator;

public final class NetworkManipulatorImpl extends StfRemoteObject implements
    INetworkManipulator {

    private static final INetworkManipulator INSTANCE = new NetworkManipulatorImpl();

    public static INetworkManipulator getInstance() {
        return NetworkManipulatorImpl.INSTANCE;
    }

    @Override
    public void blockIncomingXMPPPackets(JID jid) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void blockOutgoingXMPPPackets(JID jid) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void unblockIncomingXMPPPackets(JID jid) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void unblockOutgoingXMPPPackets(JID jid) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void unblockIncomingXMPPPackets() throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void unblockOutgoingXMPPPackets() throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDiscardIncomingXMPPPackets(JID jid, boolean discard)
        throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDiscardOutgoingXMPPPackets(JID jid, boolean discard)
        throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void blockIncomingSessionPackets(JID jid) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void blockOutgoingSessionPackets(JID jid) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void unblockIncomingSessionPackets(JID jid) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void unblockOutgoingSessionPackets(JID jid) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void unblockIncomingSessionPackets() throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void unblockOutgoingSessionPackets() throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDiscardIncomingSessionPackets(JID jid, boolean discard)
        throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDiscardOutgoingSessionPackets(JID jid, boolean discard)
        throws RemoteException {
        // TODO Auto-generated method stub

    }

}
