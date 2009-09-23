package de.fu_berlin.inf.dpp.net.internal;

import java.util.LinkedList;

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
    private LinkedList<Packet> resultQueue;
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
        this.resultQueue = new LinkedList<Packet>();
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
            return resultQueue.removeLast();
        }
    }

    /**
     * Returns the next available packet. The method call will block (not
     * return) until a packet is available.
     * 
     * @return the next available packet.
     */
    public synchronized Packet nextResult() {
        // Wait indefinitely until there is a result to return.
        while (resultQueue.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException ie) {
                // Ignore.
            }
        }
        return resultQueue.removeLast();
    }

    /**
     * Returns the next available packet. The method call will block (not
     * return) until a packet is available or the <tt>timeout</tt> has elapased.
     * If the timeout elapses without a result, <tt>null</tt> will be returned.
     * 
     * @param timeout
     *            the amount of time to wait for the next packet (in
     *            milleseconds).
     * @return the next available packet.
     */
    public synchronized Packet nextResult(long timeout) {
        // Wait up to the specified amount of time for a result.
        if (resultQueue.isEmpty()) {
            long waitTime = timeout;
            long start = System.currentTimeMillis();
            try {
                // Keep waiting until the specified amount of time has elapsed,
                // or
                // a packet is available to return.
                while (resultQueue.isEmpty()) {
                    if (waitTime <= 0) {
                        break;
                    }
                    wait(waitTime);
                    long now = System.currentTimeMillis();
                    waitTime -= (now - start);
                    start = now;
                }
            } catch (InterruptedException ie) {
                // Ignore.
            }
            // Still haven't found a result, so return null.
            if (resultQueue.isEmpty()) {
                return null;
            }
            // Return the packet that was found.
            else {
                return resultQueue.removeLast();
            }
        }
        // There's already a packet waiting, so return it.
        else {
            return resultQueue.removeLast();
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
        if (packet == null) {
            return;
        }
        if (packetFilter == null || packetFilter.accept(packet)) {
            // If the max number of packets has been reached, remove the oldest
            // one.
            if (resultQueue.size() == MAX_PACKETS) {
                resultQueue.removeLast();
            }
            // Add the new packet.
            resultQueue.addFirst(packet);
            // Notify waiting threads a result is available.
            notifyAll();
        }
    }
}
