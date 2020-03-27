package saros.communication;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import saros.communication.extensions.InfoExchangeExtension;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.IContactsUpdate;
import saros.net.xmpp.contact.IContactsUpdate.UpdateType;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;

/**
 * This Component exchanges information in a key-value form with other contacts outside of Sessions.
 *
 * <p>Every info set via {@link #setLocalInfo(String, String)} is send / updated to available
 * contacts with Saros support. Retrieval of available data with {@link #getRemoteInfo(XMPPContact,
 * String)}.
 */
public class InfoManager {
  private static final Logger log = Logger.getLogger(InfoManager.class);

  private final ITransmitter transmitter;
  private final XMPPContactsService contactsService;

  private final ConcurrentHashMap<String, String> localInfo = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<XMPPContact, ClientInfo> remoteInfo = new ConcurrentHashMap<>();

  private final PacketListener infoListener =
      packet -> {
        log.debug("received info from " + packet.getFrom());
        handleInfo(packet);
      };

  private final IContactsUpdate contactsUpdateListener =
      (contact, updateType) -> {
        if (updateType == UpdateType.STATUS) {
          if (contact.map(c -> c.getStatus().isOnline()).orElse(false)) sendInfo(contact.get());
          else contact.ifPresent(remoteInfo::remove);
        } else if (updateType == UpdateType.REMOVED) {
          contact.ifPresent(remoteInfo::remove);
        } else if (updateType == UpdateType.NOT_CONNECTED) {
          remoteInfo.clear();
        }
      };

  private final class ClientInfo {
    private final Map<String, String> infos;

    private ClientInfo(Map<String, String> infos) {
      this.infos = new HashMap<>(infos);
    }

    private String getInfo(String key) {
      return infos.get(key);
    }
  }

  public InfoManager(
      IReceiver receiver, ITransmitter transmitter, XMPPContactsService contactsService) {
    this.transmitter = transmitter;
    this.contactsService = contactsService;
    contactsService.addListener(contactsUpdateListener);

    receiver.addPacketListener(
        infoListener,
        new AndFilter(
            InfoExchangeExtension.PROVIDER.getIQFilter(),
            packet -> ((IQ) packet).getType() == IQ.Type.SET));
  }

  /**
   * If available get Information received from other Contacts.
   *
   * @param contact
   * @param key
   * @return Optional the String associated with this contact and key
   */
  public Optional<String> getRemoteInfo(XMPPContact contact, String key) {
    ClientInfo clientInfo = remoteInfo.get(contact);
    if (clientInfo == null) return Optional.empty();

    return Optional.ofNullable(clientInfo.getInfo(key));
  }

  /**
   * Set a Information that should be broadcasted to other Contacts.
   *
   * @param key
   * @param value if null key is removed from local info
   */
  public void setLocalInfo(String key, String value) {
    Objects.requireNonNull(key, "key is null");

    if (value == null) localInfo.remove(key);
    else localInfo.put(key, value);

    // send new info to all known online contacts
    for (XMPPContact contact : remoteInfo.keySet()) sendInfo(contact);
  }

  private void handleInfo(Packet packet) {
    Optional<XMPPContact> contact = contactsService.getContact(packet.getFrom());
    if (!contact.isPresent()) return;

    InfoExchangeExtension info = InfoExchangeExtension.PROVIDER.getPayload(packet);
    if (info == null) {
      log.warn("contact: " + contact + ", InfoExchangeExtension packet is malformed");
      return;
    }

    log.debug("received: " + info.getData());
    remoteInfo.put(contact.get(), new ClientInfo(info.getData()));
  }

  /**
   * Sends Info Packet to a Contact if it has Saros Support.
   *
   * @param contact
   */
  private void sendInfo(XMPPContact contact) {
    String sarosJid = contact.getSarosJid().map(JID::toString).orElse(null);
    if (sarosJid == null) return;

    IQ packet = InfoExchangeExtension.PROVIDER.createIQ(new InfoExchangeExtension(localInfo));
    packet.setType(IQ.Type.SET);
    packet.setTo(sarosJid);
    try {
      transmitter.sendPacket(packet);
    } catch (IOException e) {
      log.error("could not send info packet to " + sarosJid, e);
    }
  }
}
