package de.fu_berlin.inf.dpp.test.fakes.net;

import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.PacketCollector;
import de.fu_berlin.inf.dpp.net.PacketCollector.CancelHook;
import de.fu_berlin.inf.dpp.net.internal.BinaryXMPPExtension;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

class NonThreadedReceiver implements IReceiver {

  private Map<PacketListener, PacketFilter> listeners =
      new ConcurrentHashMap<PacketListener, PacketFilter>();

  @Override
  public void addPacketListener(PacketListener listener, PacketFilter filter) {
    listeners.put(listener, filter);
  }

  @Override
  public void removePacketListener(PacketListener listener) {
    listeners.remove(listener);
  }

  @Override
  public void processPacket(Packet packet) {
    for (Entry<PacketListener, PacketFilter> entry : listeners.entrySet()) {
      PacketListener listener = entry.getKey();
      PacketFilter filter = entry.getValue();

      if (filter == null || filter.accept(packet)) {
        listener.processPacket(packet);
      }
    }
  }

  @Override
  public PacketCollector createCollector(PacketFilter filter) {
    final PacketCollector collector =
        new PacketCollector(
            new CancelHook() {
              @Override
              public void cancelPacketCollector(PacketCollector collector) {
                removePacketListener(collector);
              }
            },
            filter);
    addPacketListener(collector, filter);

    return collector;
  }

  @Override
  public void processBinaryXMPPExtension(final BinaryXMPPExtension extension) {
    throw new UnsupportedOperationException();
  }
}
