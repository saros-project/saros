package de.fu_berlin.inf.dpp.stf.server.rmi.controlbot.manipulation.impl;

import java.rmi.RemoteException;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.inf.dpp.net.IPacketInterceptor;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription;
import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.controlbot.manipulation.INetworkManipulator;

/**
 * @author Stefan Rossbach
 */
public final class NetworkManipulatorImpl extends StfRemoteObject implements
    INetworkManipulator {

    private static final Logger LOG = Logger
        .getLogger(NetworkManipulatorImpl.class);

    private static final INetworkManipulator INSTANCE = new NetworkManipulatorImpl();

    public static INetworkManipulator getInstance() {
        return NetworkManipulatorImpl.INSTANCE;
    }

    private static class OutgoingPacketHolder {
        public TransferDescription description;
        public byte[] payload;
    }

    private ConcurrentHashMap<JID, Boolean> discardIncomingSessionPackets = new ConcurrentHashMap<JID, Boolean>();
    private ConcurrentHashMap<JID, Boolean> discardOutgoingSessionPackets = new ConcurrentHashMap<JID, Boolean>();

    /**
     * map that contains the current blocking state for incoming packets for the
     * given JID
     **/
    private ConcurrentHashMap<JID, Boolean> blockIncomingSessionPackets = new ConcurrentHashMap<JID, Boolean>();

    /**
     * map that contains the current blocking state for outgoing packets for the
     * given JID
     **/
    private ConcurrentHashMap<JID, Boolean> blockOutgoingSessionPackets = new ConcurrentHashMap<JID, Boolean>();

    /**
     * map that contains the current incoming packets for the given JID that are
     * currently not dispatched
     **/
    private ConcurrentHashMap<JID, ConcurrentLinkedQueue<IncomingTransferObject>> blockedIncomingSessionPackets = new ConcurrentHashMap<JID, ConcurrentLinkedQueue<IncomingTransferObject>>();

    /**
     * map that contains the current incoming packets for the given JID that are
     * currently not send
     **/
    private ConcurrentHashMap<JID, ConcurrentLinkedQueue<OutgoingPacketHolder>> blockedOutgoingSessionPackets = new ConcurrentHashMap<JID, ConcurrentLinkedQueue<OutgoingPacketHolder>>();

    private volatile boolean blockAllOutgoingSessionPackets;
    private volatile boolean blockAllIncomingSessionPackets;

    private IPacketInterceptor sessionPacketInterceptor = new IPacketInterceptor() {

        @Override
        public boolean receivedPacket(IncomingTransferObject object) {

            JID jid = object.getTransferDescription().getSender();
            LOG.trace("intercepting incoming packet from: " + jid);

            discardIncomingSessionPackets.putIfAbsent(jid, false);

            boolean discard = discardIncomingSessionPackets.get(jid);

            if (discard)
                return false;

            blockIncomingSessionPackets.putIfAbsent(jid, false);

            boolean blockIncomingPackets = blockIncomingSessionPackets.get(jid);

            if (blockIncomingPackets || blockAllIncomingSessionPackets) {

                blockedIncomingSessionPackets.putIfAbsent(jid,
                    new ConcurrentLinkedQueue<IncomingTransferObject>());

                blockedIncomingSessionPackets.get(jid).add(object);
                return false;
            }

            return true;
        }

        @Override
        public boolean sendPacket(TransferDescription description,
            byte[] payload) {

            JID jid = description.getRecipient();
            LOG.trace("intercepting outgoing packet to: " + jid);

            discardOutgoingSessionPackets.putIfAbsent(jid, false);

            boolean discard = discardOutgoingSessionPackets.get(jid);

            if (discard)
                return false;

            blockOutgoingSessionPackets.putIfAbsent(jid, false);

            boolean blockIncomingPackets = blockOutgoingSessionPackets.get(jid);

            if (blockIncomingPackets || blockAllOutgoingSessionPackets) {

                blockedOutgoingSessionPackets.putIfAbsent(jid,
                    new ConcurrentLinkedQueue<OutgoingPacketHolder>());

                OutgoingPacketHolder holder = new OutgoingPacketHolder();
                holder.description = description;
                holder.payload = payload;

                blockedOutgoingSessionPackets.get(jid).add(holder);
                return false;
            }

            return true;
        }

    };

    private NetworkManipulatorImpl() {
        getDataTransferManager().addPacketInterceptor(sessionPacketInterceptor);
    }

    @Override
    public void blockIncomingXMPPPackets(JID jid) throws RemoteException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void blockOutgoingXMPPPackets(JID jid) throws RemoteException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void unblockIncomingXMPPPackets(JID jid) throws RemoteException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void unblockOutgoingXMPPPackets(JID jid) throws RemoteException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void unblockIncomingXMPPPackets() throws RemoteException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void unblockOutgoingXMPPPackets() throws RemoteException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void setDiscardIncomingXMPPPackets(JID jid, boolean discard)
        throws RemoteException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void setDiscardOutgoingXMPPPackets(JID jid, boolean discard)
        throws RemoteException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void blockIncomingSessionPackets(JID jid) throws RemoteException {
        blockIncomingSessionPackets.put(jid, true);
    }

    @Override
    public void blockOutgoingSessionPackets(JID jid) throws RemoteException {
        blockOutgoingSessionPackets.put(jid, true);
    }

    @Override
    public void unblockIncomingSessionPackets(JID jid) throws RemoteException {
        LOG.trace("unblocking incoming packet transfer from " + jid);

        if (blockAllIncomingSessionPackets) {
            LOG.warn("cannot unblock incoming packet transfer from " + jid
                + ", because all incoming packet traffic is locked");
            return;
        }

        blockIncomingSessionPackets.put(jid, false);

        blockedIncomingSessionPackets.putIfAbsent(jid,
            new ConcurrentLinkedQueue<IncomingTransferObject>());

        Queue<IncomingTransferObject> pendingIncomingPackets = blockedIncomingSessionPackets
            .get(jid);

        /*
         * HACK: short sleep as it is possible that a packet arrive, thread is
         * suspended, queue is cleared and then the packet is put into the queue
         */

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        if (pendingIncomingPackets.isEmpty()) {
            LOG.trace("no packets where intercepted during blocking state");
            return;
        }

        while (!pendingIncomingPackets.isEmpty()) {
            try {
                IncomingTransferObject object = pendingIncomingPackets.remove();

                LOG.trace("dispatching blocked packet: " + object);

                getDataTransferManager().addIncomingTransferObject(object);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }

    }

    @Override
    public void unblockOutgoingSessionPackets(JID jid) throws RemoteException {
        LOG.trace("unblocking outgoing packet transfer to " + jid);

        if (blockAllOutgoingSessionPackets) {
            LOG.warn("cannot unblock outgoing packet transfer to " + jid
                + ", because all outgoing packet traffic is locked");
            return;
        }

        blockOutgoingSessionPackets.put(jid, false);

        blockedOutgoingSessionPackets.putIfAbsent(jid,
            new ConcurrentLinkedQueue<OutgoingPacketHolder>());

        Queue<OutgoingPacketHolder> pendingOutgoingPackets = blockedOutgoingSessionPackets
            .get(jid);

        /*
         * HACK: short sleep as it is possible that a packet arrive, thread is
         * suspended, queue is cleared and then the packet is put into the queue
         */

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        if (pendingOutgoingPackets.isEmpty()) {
            LOG.trace("no packets where intercepted during blocking state");
            return;
        }

        while (!pendingOutgoingPackets.isEmpty()) {
            try {
                OutgoingPacketHolder holder = pendingOutgoingPackets.remove();

                LOG.trace("sending blocked packet: " + holder.description
                    + ", payload length: " + holder.payload.length);

                getDataTransferManager().sendData(holder.description,
                    holder.payload,
                    SubMonitor.convert(new NullProgressMonitor()));
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void unblockIncomingSessionPackets() throws RemoteException {
        if (!blockAllIncomingSessionPackets)
            return;

        blockAllIncomingSessionPackets = false;

        for (JID jid : blockIncomingSessionPackets.keySet())
            unblockIncomingSessionPackets(jid);
    }

    @Override
    public void unblockOutgoingSessionPackets() throws RemoteException {
        if (!blockAllOutgoingSessionPackets)
            return;

        blockAllOutgoingSessionPackets = false;

        for (JID jid : blockOutgoingSessionPackets.keySet())
            unblockOutgoingSessionPackets(jid);
    }

    @Override
    public void blockIncomingSessionPackets() throws RemoteException {
        blockAllIncomingSessionPackets = true;
    }

    @Override
    public void blockOutgoingSessionPackets() throws RemoteException {
        blockAllOutgoingSessionPackets = true;
    }

    @Override
    public void setDiscardIncomingSessionPackets(JID jid, boolean discard)
        throws RemoteException {
        discardIncomingSessionPackets.put(jid, discard);
    }

    @Override
    public void setDiscardOutgoingSessionPackets(JID jid, boolean discard)
        throws RemoteException {
        discardOutgoingSessionPackets.put(jid, discard);
    }

}
