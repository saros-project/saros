package saros.net;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import saros.net.internal.BinaryXMPPExtension;

public interface IReceiver {

  /**
   * @JTourBusStop 5, Architecture Overview, Network Layer - Receiver:
   *
   * <p>(...) And this Interface is the main entrance point for receiving them.
   *
   * <p>For a more detailed look on how Activities work see the "Activity sending"-Tour.
   */

  /**
   * Adds the given listener to the list of listeners notified when a new packet arrives.
   *
   * <p>Will only pass those packets to the listener that are accepted by the given filter or all
   * packets if no filter is given.
   *
   * @param listener The listener to pass packets to.
   * @param filter The filter to use when trying to identify packets that should be passed to the
   *     listener. If <code>null</code> all packets are passed to the listener.
   */
  public void addPacketListener(PacketListener listener, PacketFilter filter);

  /**
   * Removes the given listener from the list of listeners.
   *
   * @param listener the listener to remove
   */
  public void removePacketListener(PacketListener listener);

  /**
   * Dispatches the given packet to all registered packet listeners.
   *
   * @param packet the packet to dispatch
   */
  public void processPacket(Packet packet);

  /**
   * Installs a {@linkplain PacketCollector collector}. Use this method instead of {@link
   * #addPacketListener} if the logic is using a polling mechanism.
   *
   * @param filter a filter that packets must match to be added to the collector.
   * @return a {@linkplain PacketCollector collector} which <b>must</b> be canceled if it is no
   *     longer used
   * @see PacketCollector#cancel()
   */
  public PacketCollector createCollector(PacketFilter filter);

  /** FOR INTERNAL USE */
  public void processBinaryXMPPExtension(BinaryXMPPExtension extension);
}
