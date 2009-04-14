package de.fu_berlin.inf.dpp.net.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.Saros;

/**
 * Facade for receiving XMPP Packages. Kind of like the GodPacketListener!
 * 
 * XMPPChatReceiver implements addPacketListener and removePacketListener just
 * like a XMPPConnection but hides the complexity of dealing with new connection
 * objects appearing and old one's disappearing. Users can just register with
 * the XMPPChatReceiver for the whole application life-cycle.
 * 
 * @component The single instance of this class per application is created by
 *            PicoContainer in the central plug-in class {@link Saros}
 */
public class XMPPChatReceiver {

    protected Map<PacketListener, PacketFilter> listeners = Collections
        .synchronizedMap(new HashMap<PacketListener, PacketFilter>());

    /**
     * Adds the given listener to the list of listeners notified when a new
     * packet arrives.
     * 
     * While only pass those packets to the listener that are accepted by the
     * given filter or all Packets if no-filter is given.
     * 
     * @param listener
     *            The listener to pass Packets to.
     * @param filter
     *            The filter to use when trying to identify Packets to send to
     *            the listener. may be null, in which case all Packets are sent.
     */
    public void addPacketListener(PacketListener listener, PacketFilter filter) {
        listeners.put(listener, filter);
    }

    public void removePacketListener(PacketListener listener) {
        listeners.remove(listener);
    }

    /**
     * This is called from the XMPPConnection for each incoming Packet and will
     * dispatch these to the registered listeners.
     */
    public void processPacket(Packet packet) {

        Map<PacketListener, PacketFilter> copy;

        synchronized (listeners) {
            copy = new HashMap<PacketListener, PacketFilter>(listeners);
        }

        for (Entry<PacketListener, PacketFilter> entry : copy.entrySet()) {
            PacketListener listener = entry.getKey();
            PacketFilter filter = entry.getValue();

            if (filter == null || filter.accept(packet)) {
                listener.processPacket(packet);
            }
        }
    }
}
