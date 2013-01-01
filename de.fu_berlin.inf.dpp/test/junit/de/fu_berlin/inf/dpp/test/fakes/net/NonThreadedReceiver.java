package de.fu_berlin.inf.dpp.test.fakes.net;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.SarosPacketCollector.CancelHook;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription;

class NonThreadedReceiver implements IReceiver {

    private Map<PacketListener, PacketFilter> listeners = new ConcurrentHashMap<PacketListener, PacketFilter>();

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
    public SarosPacketCollector createCollector(PacketFilter filter) {
        final SarosPacketCollector collector = new SarosPacketCollector(
            new CancelHook() {
                @Override
                public void cancelPacketCollector(SarosPacketCollector collector) {
                    removePacketListener(collector);
                }
            }, filter);
        addPacketListener(collector, filter);

        return collector;
    }

    @Override
    public void processIncomingTransferObject(TransferDescription description,
        IncomingTransferObject incomingTransferObject) {
        // NOP
    }
}
