package de.fu_berlin.inf.dpp.net.internal;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

/**
 * SarosPacketCollector is a special version of a Packet Collector and does not
 * depend on a PacketReader for registration.
 */
public class SarosPacketCollector implements PacketListener {

    public static interface CancelHook {
        public void cancelPacketCollector(SarosPacketCollector collector);
    }

    /**
     * Max number of packets that any one collector can hold. After the max is
     * reached, older packets will be automatically dropped from the queue as
     * new packets are added.
     */
    private static final int MAX_PACKETS = 65536;

    private PacketFilter packetFilter;
    private LinkedBlockingQueue<Packet> resultQueue = new LinkedBlockingQueue<Packet>();
    private CancelHook cancelHook;
    private boolean cancelled = false;

    /**
     * Creates a new packet collector. If the packet filter is <tt>null</tt>,
     * then all packets will match this collector.
     * 
     * @param packetFilter
     *            determines which packets will be returned by this collector.
     */
    public SarosPacketCollector(CancelHook cancelHook, PacketFilter packetFilter) {
        this.cancelHook = cancelHook;
        this.packetFilter = packetFilter;
    }

    /**
     * Explicitly cancels the packet collector so that no more results are
     * queued up. Once a packet collector has been cancelled, it cannot be
     * re-enabled. Instead, a new packet collector must be created.
     */
    public void cancel() {
        // If the packet collector has already been cancelled, do nothing.
        if (!cancelled) {
            cancelled = true;
            if (cancelHook != null)
                cancelHook.cancelPacketCollector(this);
        }
    }

    /**
     * Returns the packet filter associated with this packet collector. The
     * packet filter is used to determine what packets are queued as results.
     * 
     * @return the packet filter.
     */
    public PacketFilter getPacketFilter() {
        return packetFilter;
    }

    /**
     * Polls to see if a packet is currently available and returns it, or
     * immediately returns <tt>null</tt> if no packets are currently in the
     * result queue.
     * 
     * @return the next packet result, or <tt>null</tt> if there are no more
     *         results.
     */
    public synchronized Packet pollResult() {
        if (resultQueue.isEmpty()) {
            return null;
        } else {
            return resultQueue.poll();
        }
    }

    /**
     * Returns the next available packet. The method call will block (not
     * return) until a packet is available.
     * 
     * @return the next available packet.
     */
    public synchronized Packet nextResult() throws InterruptedException {
        return resultQueue.take();
    }

    /**
     * Returns the next available packet. The method call will block (not
     * return) until a packet is available or the <tt>timeout</tt> has elapased.
     * If the timeout elapses without a result (or the thread is interrupted),
     * <tt>null</tt> will be returned.
     * 
     * @param timeout
     *            the amount of time in milliseconds to wait for the next
     *            packet.
     * @return the next available packet.
     */
    /*
     * Note: Don't make this method synchronized. ProcessPacket() will block
     * then and resultQueue.poll() will time out at least once if empty.
     */
    public Packet nextResult(long timeout) {
        try {
            return resultQueue.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            // Ignore
            return null;
        }
    }

    /**
     * Processes a packet to see if it meets the criteria for this packet
     * collector. If so, the packet is added to the result queue.
     * 
     * @param packet
     *            the packet to process.
     */
    public synchronized void processPacket(Packet packet) {
        if (packet == null)
            return;

        if (packetFilter == null || packetFilter.accept(packet)) {
            // If the max number of packets has been reached, remove the oldest
            // one.
            if (resultQueue.size() >= MAX_PACKETS) {
                resultQueue.remove();
            }
            resultQueue.add(packet);
        }
    }
}
