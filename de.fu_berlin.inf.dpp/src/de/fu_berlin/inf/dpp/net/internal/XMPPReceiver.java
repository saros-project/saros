package de.fu_berlin.inf.dpp.net.internal;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParserException;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject.IncomingTransferObjectExtensionProvider;
import de.fu_berlin.inf.dpp.net.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.SarosPacketCollector.CancelHook;
import de.fu_berlin.inf.dpp.net.business.DispatchThreadContext;

/**
 * Facade for receiving XMPP Packages.
 * 
 * XMPPReceiver implements addPacketListener and removePacketListener just like
 * a XMPPConnection but hides the complexity of dealing with new connection
 * objects appearing and old one's disappearing. Users can just register with
 * the XMPPReceiver for the whole application life-cycle.
 * 
 */
@Component(module = "net")
public class XMPPReceiver implements IReceiver {

    private static final Logger log = Logger.getLogger(XMPPReceiver.class);

    private IncomingTransferObjectExtensionProvider incomingExtProv;

    private DispatchThreadContext dispatchThreadContext;

    private Map<PacketListener, PacketFilter> listeners = Collections
        .synchronizedMap(new HashMap<PacketListener, PacketFilter>());

    public XMPPReceiver(DispatchThreadContext dispatchThreadContext,
        IncomingTransferObjectExtensionProvider incomingExtProv) {

        this.dispatchThreadContext = dispatchThreadContext;
        this.incomingExtProv = incomingExtProv;
    }

    /**
     * Adds the given listener to the list of listeners notified when a new
     * packet arrives.
     * 
     * Will only pass those packets to the listener that are accepted by the
     * given filter or all Packets if no filter is given.
     * 
     * @param listener
     *            The listener to pass packets to.
     * @param filter
     *            The filter to use when trying to identify Packets to send to
     *            the listener. may be null, in which case all Packets are sent.
     */
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
        dispatchThreadContext.executeAsDispatch(new Runnable() {
            @Override
            public void run() {
                forwardPacket(packet);
            }
        });
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
    public void processIncomingTransferObject(
        final TransferDescription description,
        final IncomingTransferObject incomingTransferObject) {
        final Packet packet = new Message();
        packet.setPacketID(Packet.ID_NOT_AVAILABLE);
        packet.setFrom(description.getSender().toString());
        packet.addExtension(incomingExtProv.create(incomingTransferObject));

        dispatchThreadContext.executeAsDispatch(new Runnable() {

            @Override
            public void run() {

                // StreamServiceManager forward
                if (processIncomingTransferDescription(packet))
                    return;

                processTransferObjectToPacket(description,
                    incomingTransferObject);
            }
        });
    }

    /**
     * This is called from the XMPPConnection for each incoming Packet and will
     * dispatch these to the registered listeners.
     * 
     * @sarosThread must be called from the Dispatch Thread
     */
    private void forwardPacket(Packet packet) {
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

    /**
     * <p>
     * Informs the the first listener about the received packet that has to
     * contain an IncomingTransferObject packet extension and let it process it.
     * <p>
     * 
     * <p>
     * In difference to usual packets, IncomingTransferObjects can only by
     * processed by ONE listener, because it accesses the DataTransferManager to
     * receive a bytestream that can only be done once.
     * </p>
     * 
     * @param packet
     *            that contains an IncomingTransferObject packet extension
     * @return if the packet containing an IncomingTransferObject was processed
     * 
     * @sarosThread must be called from the Dispatch Thread
     */
    /*
     * Note: left as a separate method because of different functionality.
     * Furthermore the next refactoring step is to incorporate an
     * IncomingTransferObject listener. It does not make sense to convert it to
     * a packet first.
     */
    private boolean processIncomingTransferDescription(Packet packet) {
        Map<PacketListener, PacketFilter> copy;

        synchronized (listeners) {
            copy = new HashMap<PacketListener, PacketFilter>(listeners);
        }
        for (Entry<PacketListener, PacketFilter> entry : copy.entrySet()) {
            PacketListener listener = entry.getKey();
            PacketFilter filter = entry.getValue();

            if (filter == null || filter.accept(packet)) {
                listener.processPacket(packet);
                /*
                 * A stream can only be accepted once. Else an exception is
                 * thrown:
                 * 
                 * java.lang.IllegalStateException: This IncomingTransferObject
                 * has already been accepted or rejected
                 */
                return true;
            }
        }

        return false;
    }

    /**
     * This method receives the bytestream message from the incoming transfer
     * object and parsed it to the respective Smack {@link PacketExtension}
     * 
     * @sarosThread must be called from the Dispatch Thread
     */
    private void processTransferObjectToPacket(TransferDescription description,
        IncomingTransferObject transferObject) {

        String name = description.getType();
        String namespace = description.getNamespace();
        // IQ provider?

        PacketExtensionProvider provider = (PacketExtensionProvider) ProviderManager
            .getInstance().getExtensionProvider(name, namespace);

        byte[] data;

        if (provider == null)
            return;

        data = transferObject.getPayload();

        /*
         * TODO: check how expensive it is to create an new MXParser for every
         * packet
         */
        MXParser parser = new MXParser();
        PacketExtension extension = null;

        try {
            parser.setInput(new ByteArrayInputStream(data), "UTF-8");
            /*
             * We have to skip the empty start tag because Smack expects a
             * parser that already has started parsing.
             */
            parser.next();
            extension = provider.parseExtension(parser);

        } catch (XmlPullParserException e) {
            log.error("Unexpected encoding error:", e);
            return;
        } catch (Exception e) {
            log.error(
                "Could not parse packet extension from bytestream. Maybe a wrong transfer description is used?",
                e);
            return;
        }

        final Packet packet = new Message();
        packet.setPacketID(description.getExtensionVersion());
        packet.setFrom(description.getSender().toString());
        packet.setTo(description.getRecipient().toString());
        packet.addExtension(extension);

        forwardPacket(packet);
    }
}
