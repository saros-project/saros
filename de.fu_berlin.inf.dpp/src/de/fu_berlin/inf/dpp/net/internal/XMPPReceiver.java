package de.fu_berlin.inf.dpp.net.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.picocontainer.annotations.Inject;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParserException;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject.IncomingTransferObjectExtensionProvider;
import de.fu_berlin.inf.dpp.net.business.DispatchThreadContext;
import de.fu_berlin.inf.dpp.net.internal.SarosPacketCollector.CancelHook;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Facade for receiving XMPP Packages. Kind of like the GodPacketListener!
 * 
 * XMPPReceiver implements addPacketListener and removePacketListener just like
 * a XMPPConnection but hides the complexity of dealing with new connection
 * objects appearing and old one's disappearing. Users can just register with
 * the XMPPReceiver for the whole application life-cycle.
 * 
 */
@Component(module = "net")
public class XMPPReceiver {

    private static Logger log = Logger.getLogger(XMPPReceiver.class);

    @Inject
    protected IncomingTransferObjectExtensionProvider incomingExtProv;

    @Inject
    protected DispatchThreadContext dispatchThreadContext;

    protected Map<PacketListener, PacketFilter> listeners = Collections
        .synchronizedMap(new HashMap<PacketListener, PacketFilter>());

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
    public void addPacketListener(PacketListener listener, PacketFilter filter) {
        listeners.put(listener, filter);
    }

    public void removePacketListener(PacketListener listener) {
        listeners.remove(listener);
    }

    /**
     * This is called from the XMPPConnection for each incoming Packet and will
     * dispatch these to the registered listeners.
     * 
     * @sarosThread must be called from the Dispatch Thread
     */
    protected void processPacket(Packet packet) {
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
     */
    /*
     * Note: left as a separate method because of different functionality.
     * Furthermore the next refactoring step is to incorporate an
     * IncomingTransferObject listener. It does not make sense to convert it to
     * a packet first.
     */
    protected boolean processIncomingTransferDescription(Packet packet) {
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

    public SarosPacketCollector createCollector(PacketFilter filter) {
        final SarosPacketCollector collector = new SarosPacketCollector(
            new CancelHook() {
                public void cancelPacketCollector(SarosPacketCollector collector) {
                    removePacketListener(collector);
                }
            }, filter);
        addPacketListener(collector, filter);

        return collector;
    }

    /**
     * <p>
     * Extensions sent by bytestreams can by arbitrary long, thus they are
     * received in a separate thread.
     * </p>
     * 
     * <p>
     * Note, as we have two completely different communication ways sequencing
     * is not ensured in neither case, if done in the dispatch thread or not.
     * </p>
     */
    protected ExecutorService extensionDownloadThreadPool = Executors
        .newCachedThreadPool(new NamedThreadFactory(
            "Bytestream-Extension-receiver-"));

    /**
     * This is method is used by the DataTransferManager to inform the upper
     * Layer about incoming Packet based Objects.
     * 
     * @sarosThread must be called from the Dispatch Thread
     */
    public void processIncomingTransferObject(
        final TransferDescription description,
        final IncomingTransferObject incomingTransferObject) {
        final Packet packet = new Message();
        packet.setPacketID(Packet.ID_NOT_AVAILABLE);
        packet.setFrom(description.sender.toString());
        packet.addExtension(incomingExtProv.create(incomingTransferObject));
        if (processIncomingTransferDescription(packet)) {
            return;
        }

        extensionDownloadThreadPool.execute(Utils.wrapSafe(log, new Runnable() {
            public void run() {

                processTransferObjectToPacket(description,
                    incomingTransferObject);
            }
        }));
    }

    /**
     * This method receives the bytestream message from the incoming transfer
     * object and parsed it to the respective Smack {@link PacketExtension}
     * 
     * @sarosThread must be called by the extensionDownloadThreadPool
     */
    protected void processTransferObjectToPacket(
        TransferDescription description, IncomingTransferObject transferObject) {
        SubMonitor monitor = SubMonitor.convert(new NullProgressMonitor());

        String name = description.type;
        String namespace = description.namespace;
        // IQ provider?

        PacketExtensionProvider provider = (PacketExtensionProvider) ProviderManager
            .getInstance().getExtensionProvider(name, namespace);

        byte[] data;
        try {

            if (provider == null) {
                /*
                 * We MUST reject. Else the peer will remain in a endless cycle
                 * in BinaryChannel.sendDirect()
                 */
                transferObject.reject();
                return;
            }

            data = transferObject.accept(monitor);

        } catch (SarosCancellationException e) {
            log.error("User canceled. This is unexpected", e);
            return;
        } catch (IOException e) {
            log.error("Could not deserialize incoming "
                + "transfer object or a connection error occurred", e);
            return;
        }

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
        packet.setPacketID(Packet.ID_NOT_AVAILABLE);
        packet.setFrom(description.sender.toString());
        packet.setTo(description.recipient.toString());
        packet.addExtension(extension);

        dispatchThreadContext.executeAsDispatch(new Runnable() {
            public void run() {
                processPacket(packet);
            }
        });
    }
}
