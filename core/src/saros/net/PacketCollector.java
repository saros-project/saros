package saros.net;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

/**
 * This packet collector is a special version of SMACKs {@link
 * org.jivesoftware.smack.PacketCollector} and does not depend on a {@link
 * org.jivesoftware.smack.Connection} for registration.
 */
public class PacketCollector implements PacketListener {

  public static interface CancelHook {
    public void cancelPacketCollector(PacketCollector collector);
  }

  private static final Logger log = Logger.getLogger(PacketCollector.class);
  private boolean hasReveived = false;

  /**
   * Max number of packets that any one collector can hold. After the max is reached this collector
   * is canceled.
   */
  public static final int MAX_PACKETS = 65536;

  private PacketFilter packetFilter;
  private LinkedBlockingQueue<Packet> resultQueue = new LinkedBlockingQueue<Packet>(MAX_PACKETS);
  private CancelHook cancelHook;
  /** Once canceled is true, it can never become false again. */
  private boolean canceled = false;

  /**
   * Creates a new packet collector. If the packet filter is <tt>null</tt>, then all packets will
   * match this collector.
   *
   * @param packetFilter determines which packets will be returned by this collector.
   */
  public PacketCollector(CancelHook cancelHook, PacketFilter packetFilter) {
    this.cancelHook = cancelHook;
    this.packetFilter = packetFilter;
  }

  /**
   * Explicitly cancels the packet collector so that no more results are queued up. Once a packet
   * collector has been canceled, it cannot be re-enabled. Instead, a new packet collector must be
   * created.
   */
  public synchronized void cancel() {
    // If the packet collector has already been canceled, do nothing.
    if (!canceled) {
      canceled = true;
      if (cancelHook != null) cancelHook.cancelPacketCollector(this);
    }
  }

  /**
   * Returns the packet filter associated with this packet collector. The packet filter is used to
   * determine what packets are queued as results.
   *
   * @return the packet filter.
   */
  public PacketFilter getPacketFilter() {
    return packetFilter;
  }

  /**
   * Returns the next available packet. The method call will block (not return) until a packet is
   * available or the <tt>timeout</tt> has elapsed. If the timeout elapses without a result (or the
   * thread is interrupted), <tt>null</tt> will be returned.
   *
   * @param timeout the amount of time in milliseconds to wait for the next packet.
   * @throws IllegalStateException if this collector is canceled
   * @return the next available packet.
   */
  public Packet nextResult(long timeout) {
    if (canceled && resultQueue.isEmpty())
      throw new IllegalStateException("Canceled packet collector");
    try {
      return resultQueue.poll(timeout, TimeUnit.MILLISECONDS);
    } catch (InterruptedException ie) {
      // Ignore
      return null;
    }
  }

  /** Returns whether this collector has already received a packet. */
  public boolean hasReceived() {
    return hasReveived;
  }

  /**
   * Processes a packet to see if it meets the criteria for this packet collector. If so, the packet
   * is added to the result queue.
   *
   * <p>If the result queue's capacity {@link #MAX_PACKETS} is reached, the collector is canceled
   * using the proved {@link CancelHook}.
   *
   * @param packet the packet to process
   * @singleThreaded should only be accessed by the single threaded executer of {@link
   *     saros.net.DispatchThreadContext}
   */
  @Override
  public void processPacket(Packet packet) {
    if (packet == null) return;

    if (packetFilter == null || packetFilter.accept(packet)) {
      hasReveived = true;
      try {
        resultQueue.add(packet);
      } catch (IllegalStateException e) {
        log.warn("Queue has reached capacity, collector canceled.");
        cancel();
      }
    }
  }
}
