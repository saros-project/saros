package de.fu_berlin.inf.dpp.net.internal;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.DispatchThreadContext;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.PacketCollector;
import de.fu_berlin.inf.dpp.net.PacketCollector.CancelHook;
import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;

@Component(module = "net")
public class XMPPReceiver implements IReceiver {

  private static final Logger LOG = Logger.getLogger(XMPPReceiver.class);

  private final DispatchThreadContext dispatchThreadContext;

  private Map<PacketListener, PacketFilter> listeners =
      Collections.synchronizedMap(new HashMap<PacketListener, PacketFilter>());

  private XmlPullParser parser;

  private final PacketListener smackPacketListener =
      new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
          XMPPReceiver.this.processPacket(packet);
        }
      };

  private final IConnectionListener connectionListener =
      new IConnectionListener() {

        @Override
        public void connectionStateChanged(Connection connection, ConnectionState state) {

          switch (state) {
            case CONNECTING:
              connection.addPacketListener(smackPacketListener, null);
              // $FALL-THROUGH$
            case CONNECTED:
              break;
            default:
              if (connection != null) connection.removePacketListener(smackPacketListener);
          }
        }
      };

  public XMPPReceiver(
      DispatchThreadContext dispatchThreadContext, XMPPConnectionService connectionService) {

    this.dispatchThreadContext = dispatchThreadContext;
    this.parser = new MXParser();

    connectionService.addListener(connectionListener);
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
    dispatchThreadContext.executeAsDispatch(
        new Runnable() {
          @Override
          public void run() {
            forwardPacket(packet);
          }
        });
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

    dispatchThreadContext.executeAsDispatch(
        new Runnable() {

          @Override
          public void run() {

            Packet packet = convertBinaryXMPPExtension(extension);

            if (packet != null) forwardPacket(packet);
          }
        });
  }

  /**
   * Dispatches the packet to all registered listeners.
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
   * Deserializes the payload of an {@link BinaryXMPPExtension} back to its original {@link
   * PacketExtension} and returns a new packet containing the deserialized packet extension.
   *
   * <p>This method is <b>not</b> thread safe and <b>must not</b> accessed by multiple threads
   * concurrently.
   */
  private Packet convertBinaryXMPPExtension(BinaryXMPPExtension transferObject) {

    TransferDescription description = transferObject.getTransferDescription();

    String name = description.getElementName();
    String namespace = description.getNamespace();
    // IQ provider?

    PacketExtensionProvider provider =
        (PacketExtensionProvider)
            ProviderManager.getInstance().getExtensionProvider(name, namespace);

    if (provider == null) {
      LOG.warn(
          "could not deserialize transfer object because no provider with namespace '"
              + namespace
              + "' and element name '"
              + name
              + "' is installed");
      return null;
    }

    PacketExtension extension = null;

    try {
      parser.setInput(new ByteArrayInputStream(transferObject.getPayload()), "UTF-8");
      /*
       * We have to skip the empty start tag because Smack expects a
       * parser that already has started parsing.
       */
      parser.next();
      extension = provider.parseExtension(parser);
    } catch (Exception e) {
      LOG.error("could not deserialize transfer object payload: " + e.getMessage(), e);

      // just to be safe
      parser = new MXParser();
      return null;
    }

    Packet packet = new Message();
    packet.setPacketID(Packet.ID_NOT_AVAILABLE);
    packet.setFrom(description.getSender().toString());
    packet.setTo(description.getRecipient().toString());
    packet.addExtension(extension);

    return packet;
  }
}
