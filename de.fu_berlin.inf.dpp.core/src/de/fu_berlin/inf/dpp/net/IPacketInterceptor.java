package de.fu_berlin.inf.dpp.net;

import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription;

/**
 * An interface for intercepting packages that are about to send or dispatched
 * via the {@link DataTransferManager}
 */
public interface IPacketInterceptor {

    /**
     * This method is called before the {@link DataTransferManager} is
     * dispatching the packet
     * 
     * @param object
     * @return <code>true</code> if the packet should be dispatched,
     *         <code>false</code> if the packet should be dropped
     */
    public boolean receivedPacket(IncomingTransferObject object);

    /**
     * This method is called before the {@link DataTransferManager} is sending
     * the packet
     * 
     * @param description
     * @param payload
     * @return <code>true</code> if the packet should be send,
     *         <code>false</code> if the packet should be dropped
     */
    public boolean sendPacket(TransferDescription description, byte[] payload);
}
