/**
 * 
 */
package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.internal.BinaryPacketProto.BinaryPacket;
import de.fu_berlin.inf.dpp.net.internal.BinaryPacketProto.BinaryPacket.PacketType;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.util.Utils;

public class BinaryChannelTransferObject implements IncomingTransferObject {

    /**
     * 
     */
    private BinaryChannel binaryChannel;

    private static final Logger log = Logger
        .getLogger(BinaryChannelTransferObject.class);

    protected final TransferDescription transferDescription;

    protected final int objectid;

    protected long transferredSize;
    protected long uncompressedSize;

    protected AtomicBoolean acceptedOrRejected = new AtomicBoolean(false);

    public BinaryChannelTransferObject(BinaryChannel binaryChannel,
        TransferDescription transferDescription, int objectid) {

        this.binaryChannel = binaryChannel;
        this.transferDescription = transferDescription;
        this.objectid = objectid;
        transferredSize = 0;
        uncompressedSize = 0;
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
                if (!this.binaryChannel.isConnected())
                    throw new LocalCancellationException(
                        "Data connection lost.", CancelOption.NOTIFY_PEER);
                if (progress.isCanceled()) {
                    // reject();
                    /*
                     * @TODO: For sending, the BinaryChannel actually also
                     * expects a Reject packet to detect the cancel; see
                     * BinaryChannel.sendDirect() and the confirmation packet
                     * where else an IOException is thrown.
                     */

                    throw new LocalCancellationException(
                        "Data reception was manually cancelled.",
                        CancelOption.NOTIFY_PEER);
                }

                BinaryPacket packet;
                try {
                    packet = myPackets.poll(5, TimeUnit.SECONDS);
                    if (packet == null)
                        continue;

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
                    progress.beginTask(
                        "Receiving",
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

            transferredSize = data.length;

            if (transferDescription.compressInDataTransferManager())
                data = Utils.inflate(data, progress.newChild(1));

            uncompressedSize = data.length;

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

        this.binaryChannel.send(BinaryChannel.buildPacket(PacketType.REJECT,
            objectid));
    }

    public NetTransferMode getTransferMode() {
        return this.binaryChannel.transferMode;
    }

    public long getTransferredSize() {
        return transferredSize;
    }

    public long getUncompressedSize() {
        return uncompressedSize;
    }

}