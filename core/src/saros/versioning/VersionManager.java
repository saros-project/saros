package saros.versioning;

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
 * Component for figuring out whether two Saros plug-in instances with known Version are compatible
 * and to exchange Infos outside of Sessions.
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
        log.debug("received info from " + packet.getFrom());
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
   * Determines the version compatibility with the given contact.
   *
   * @param contact the remote party
   * @return the {@link VersionCompatibilityResult VersionCompatibilityResult}
   */
  public VersionCompatibilityResult determineVersionCompatibility(XMPPContact contact) {
    Objects.requireNonNull(contact, "contact is needed");

    ClientInfo contactInfo = remoteInfo.get(contact);
    if (contactInfo == null) {
      log.warn("contact: " + contact + ", remote version string not found in info data");
      return new VersionCompatibilityResult(Compatibility.UNKNOWN, localVersion, Version.INVALID);
    }

    String versionString = contactInfo.getInfo(VERSION_KEY);
    if (versionString == null) {
      log.warn("remote version not found for: " + contact);
      return new VersionCompatibilityResult(Compatibility.UNKNOWN, localVersion, Version.INVALID);
    }

    Version remoteVersion = Version.parseVersion(versionString);
    if (remoteVersion == Version.INVALID) {
      log.warn("contact: " + contact + ", remote version string is invalid: " + versionString);
    }

    Compatibility compatibility = determineCompatibility(localVersion, remoteVersion);
    return new VersionCompatibilityResult(compatibility, localVersion, remoteVersion);
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

  /**
   * If available get Information received by other Contacts.
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

    log.debug("received: " + versionInfo.getData());
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
