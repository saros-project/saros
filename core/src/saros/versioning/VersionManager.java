package saros.versioning;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import saros.annotations.Component;
import saros.communication.extensions.VersionExchangeExtension;
import saros.context.IContextKeyBindings.SarosVersion;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.IContactsUpdate;
import saros.net.xmpp.contact.IContactsUpdate.UpdateType;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;

/**
 * Component for figuring out whether two Saros plug-in instances with known Version are compatible.
 *
 * <p>This class compares if local and remote version (not checking qualifier) are the same.
 */
@Component(module = "core")
public class VersionManager {
  /* If you want to implement backward compatibility in a later version, as a suggestion:
   * acknowledge same major.minor version as compatible. Alternatively add a `backwards_compatibility`
   * key with the last working version as value and add a check for it.
   */

  static final String VERSION_KEY = "version";

  private class ClientInfo {
    private final Map<String, String> infos;

    private ClientInfo(Map<String, String> infos) {
      this.infos = new HashMap<>(infos);
    }

    String getInfo(String key) {
      return infos.get(key);
    }
  }

  private static final Logger log = Logger.getLogger(VersionManager.class);

  private final Version localVersion;
  private final ITransmitter transmitter;
  private final XMPPContactsService contactsService;

  private final ConcurrentHashMap<String, String> localInfo = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<XMPPContact, ClientInfo> remoteInfo = new ConcurrentHashMap<>();

  private final PacketListener versionInfoListener =
      packet -> {
        log.debug("received version info from " + packet.getFrom());
        handleInfo(packet);
      };

  private final IContactsUpdate contactsUpdateListener =
      (contact, updateType) -> {
        if (updateType == UpdateType.STATUS || updateType == UpdateType.FEATURE_SUPPORT) {
          if (contact.map(c -> c.getStatus().isOnline()).orElse(false)) sendInfo(contact.get());
          else contact.ifPresent(remoteInfo::remove);
        } else if (updateType == UpdateType.REMOVED) {
          contact.ifPresent(remoteInfo::remove);
        } else if (updateType == UpdateType.NOT_CONNECTED) {
          remoteInfo.clear();
        }
      };

  public VersionManager(
      @SarosVersion String version,
      IReceiver receiver,
      ITransmitter transmitter,
      XMPPContactsService contactsService) {
    this.localVersion = Version.parseVersion(version);
    if (this.localVersion == Version.INVALID)
      throw new IllegalArgumentException("version string is malformed: " + version);

    localInfo.put(VERSION_KEY, localVersion.toString());

    this.transmitter = transmitter;
    this.contactsService = contactsService;
    contactsService.addListener(contactsUpdateListener);

    receiver.addPacketListener(
        versionInfoListener,
        new AndFilter(
            VersionExchangeExtension.PROVIDER.getIQFilter(),
            packet -> ((IQ) packet).getType() == IQ.Type.SET));
  }

  /**
   * Determines the version compatibility with the given peer.
   *
   * @param jid the JID of the peer
   * @return the {@link VersionCompatibilityResult VersionCompatibilityResult}
   */
  public VersionCompatibilityResult determineVersionCompatibility(JID jid) {
    Optional<ClientInfo> contactInfo =
        contactsService.getContact(jid.getRAW()).map(remoteInfo::get);
    if (!contactInfo.isPresent())
      log.warn("contact: " + jid + ", remote version string not found in info data");

    Optional<String> versionString = contactInfo.map(clientInfo -> clientInfo.getInfo(VERSION_KEY));
    Version remoteVersion = versionString.map(Version::parseVersion).orElse(null);
    if (remoteVersion == null) {
      log.warn("remote version not found for: " + jid);
      return new VersionCompatibilityResult(Compatibility.UNKNOWN, localVersion, Version.INVALID);
    }

    if (remoteVersion == Version.INVALID)
      log.warn("contact: " + jid + ", remote version string is invalid: " + versionString.get());

    Compatibility compatibility = determineCompatibility(localVersion, remoteVersion);
    return new VersionCompatibilityResult(compatibility, localVersion, remoteVersion);
  }

  /**
   * Sends Info Packet to a Contact if it has Saros Support.
   *
   * @param contact
   */
  private void sendInfo(XMPPContact contact) {
    String sarosJid = contact.getSarosJid().map(JID::toString).orElse(null);
    if (sarosJid == null) return;

    IQ packet = VersionExchangeExtension.PROVIDER.createIQ(new VersionExchangeExtension(localInfo));
    packet.setType(IQ.Type.SET);
    packet.setTo(sarosJid);
    try {
      transmitter.sendPacket(packet);
    } catch (IOException e) {
      log.error("could not send version response to " + sarosJid, e);
    }
  }

  private void handleInfo(Packet packet) {
    Optional<XMPPContact> contact = contactsService.getContact(packet.getFrom());
    if (!contact.isPresent()) return;

    VersionExchangeExtension versionInfo = VersionExchangeExtension.PROVIDER.getPayload(packet);
    if (versionInfo == null) {
      log.warn("contact: " + contact + ", VersionExchangeExtension packet is malformed");
      return;
    }

    remoteInfo.put(contact.get(), new ClientInfo(versionInfo.getData()));
  }

  /**
   * Compares the two given versions for compatibility. The result indicates whether the local
   * version is compatible with the remote version.
   */
  private Compatibility determineCompatibility(Version localVersion, Version remoteVersion) {
    Compatibility compatibility = Compatibility.valueOf(localVersion.compareTo(remoteVersion));

    return compatibility;
  }
}
