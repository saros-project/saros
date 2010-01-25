/**
 * 
 */
package de.fu_berlin.inf.dpp.net.jingle.protocol;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.net.jingle.protocol.BinaryPacketProto.BinaryPacket;
import de.fu_berlin.inf.dpp.net.jingle.protocol.BinaryPacketProto.BinaryPacket.PacketType;
import de.fu_berlin.inf.dpp.util.Util;

public class BinaryChannelTransferObject implements IncomingTransferObject {

    /**
     * 
     */
    private BinaryChannel binaryChannel;

    private static final Logger log = Logger
        .getLogger(BinaryChannelTransferObject.class);

    protected final TransferDescription transferDescription;

    protected final int objectid;

    protected AtomicBoolean acceptedOrRejected = new AtomicBoolean(false);

    protected BinaryChannelTransferObject(BinaryChannel binaryChannel,
        TransferDescription transferDescription, int objectid) {

        this.binaryChannel = binaryChannel;
        this.transferDescription = transferDescription;
        this.objectid = objectid;
    }

    public byte[] accept(SubMonitor progress)
        throws SarosCancellationException, IOException {

        try {

            if (!acceptedOrRejected.compareAndSet(false, true))
                throw new IllegalStateException(
                    "This IncomingTransferObject has already"
                        + " been accepted or rejected");

            BlockingQueue<BinaryPacket> myPackets = this.binaryChannel.incomingPackets
                .get(objectid);

            boolean first = true;

            LinkedList<BinaryPacket> resultList = new LinkedList<BinaryPacket>();

            while (true) {
                if (progress.isCanceled()) {
                    reject();
                    throw new LocalCancellationException();
                }

                BinaryPacket packet;
                try {
                    packet = myPackets.take();
                } catch (InterruptedException e) {
                    log.error("Code not designed to be interrupted");
                    Thread.currentThread().interrupt();
                    return null;
                }

                if (packet.getType() == PacketType.CANCEL) {
                    assert packet.getObjectid() == objectid;

                    throw new RemoteCancellationException();
                }

                if (first) {
                    progress.beginTask("Receiving",
                        packet.getRemaining()
                            + (transferDescription
                                .compressInDataTransferManager() ? 1 : 0));
                    first = false;
                }

                resultList.add(packet);
                progress.worked(1);
                if (packet.getRemaining() == 0)
                    break;
            }

            this.binaryChannel.send(BinaryChannel.buildPacket(
                PacketType.FINISHED, objectid));
            byte[] data = BinaryChannel.getData(resultList);

            if (transferDescription.compressInDataTransferManager())
                data = Util.inflate(data, progress.newChild(1));

            return data;
        } finally {
            this.binaryChannel.incomingPackets.remove(objectid);
        }
    }

    public TransferDescription getTransferDescription() {
        return transferDescription;
    }

    public void reject() throws IOException {
        if (!acceptedOrRejected.compareAndSet(false, true))
            throw new IllegalStateException(
                "This IncomingTransferObject has already"
                    + " been accepted or rejected");

        this.binaryChannel.send(this.binaryChannel.buildPacket(
            PacketType.REJECT, objectid));
    }

    public NetTransferMode getTransferMode() {
        return this.binaryChannel.transferMode;
    }
}