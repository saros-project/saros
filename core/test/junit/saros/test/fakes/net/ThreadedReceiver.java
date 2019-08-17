package saros.test.fakes.net;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import saros.net.IReceiver;
import saros.net.PacketCollector;
import saros.net.PacketCollector.CancelHook;
import saros.util.NamedThreadFactory;

public class ThreadedReceiver implements IReceiver {
  private Map<PacketListener, PacketFilter> listeners =
      new ConcurrentHashMap<PacketListener, PacketFilter>();

  private final ExecutorService executor =
      Executors.newSingleThreadExecutor(new NamedThreadFactory("ThreadedReceiver", false));

  public void stop() {
    executor.shutdown();
  }

  @Override
  public void addPacketListener(PacketListener listener, PacketFilter filter) {
    listeners.put(listener, filter);
  }

  @Override
  public void removePacketListener(PacketListener listener) {
    listeners.remove(listener);
  }

  @Override
  public void processPacket(final Packet packet) {
    for (Entry<PacketListener, PacketFilter> entry : listeners.entrySet()) {
      final PacketListener listener = entry.getKey();
      final PacketFilter filter = entry.getValue();

      if (filter == null || filter.accept(packet)) {
        listener.processPacket(packet);

        executor.submit(
            new Runnable() {

              @Override
              public void run() {
                try {
                  listener.processPacket(packet);
                } catch (Throwable t) {
                  t.printStackTrace();
                }
              }
            });
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
}
