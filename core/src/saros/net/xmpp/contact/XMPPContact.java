package saros.net.xmpp.contact;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.net.xmpp.contact.ContactStatus.Type;

/**
 * An XMPPContact represents a contact on the XMPP roster. It contains the current presence state
 * and extended informations like Saros Support. Objects of this class are thread-safe for all
 * public methods and informations are updated by {@link XMPPConnectionService}.
 *
 * <p>All package private methods should be called single-threaded or synchronized to guarantee
 * thread-safety.
 *
 * <p>This class is likely to implement a generic Contact Interface in the future.
 */
public class XMPPContact {

  /** Bundles information about a resource of a client. */
  private static final class Resource {
    static final Comparator<Resource> BEST_RESOURCE_FIRST =
        Comparator.comparing((Resource r) -> !r.sarosSupport)
            .thenComparing(r -> r.status)
            .thenComparing(r -> r.fullJid.getRAW());

    final JID fullJid;
    final ContactStatus status;
    final boolean sarosSupport;

    Resource(JID fullJid, ContactStatus status, boolean sarosSupport) {
      this.fullJid = Objects.requireNonNull(fullJid, "fullJid is null");
      if (fullJid.isBareJID()) throw new IllegalArgumentException("fullJid is a bare JID");
      this.status = Objects.requireNonNull(status, "status is null");
      this.sarosSupport = sarosSupport;
    }

    @Override
    public String toString() {
      return String.format(
          "Resource [fullJid=%s, status=%s, sarosSupport=%s]", fullJid, status, sarosSupport);
    }
  }

  /**
   * Create a new XMPPContact from RosterEntry.
   *
   * @param entry RosterEntry
   * @return new XMPPContact
   */
  static XMPPContact from(RosterEntry entry) {
    ContactStatus baseStatus;
    if (entry.getStatus() == RosterPacket.ItemStatus.SUBSCRIPTION_PENDING) {
      baseStatus = ContactStatus.TYPE_SUBSCRIPTION_PENDING;
    } else if (entry.getType() == ItemType.none || entry.getType() == ItemType.from) {
      /* see http://xmpp.org/rfcs/rfc3921.html chapter 8.2.1, 8.3.1 and 8.6 */
      baseStatus = ContactStatus.TYPE_SUBSCRIPTION_CANCELED;
    } else {
      baseStatus = ContactStatus.TYPE_OFFLINE;
    }

    return new XMPPContact(new JID(entry.getUser()), baseStatus, entry.getName());
  }

  /** JID of this contact without resource information. */
  private final JID bareJid;
  /** Basic Status of this Contact, either offline, removed or subscription related */
  private volatile ContactStatus baseStatus;
  /** Roster provided nickname if available */
  private volatile Optional<String> nickname;

  /** Set of all known resources. Methods update bestResource after modification. */
  private final Set<Resource> resources = new HashSet<>();
  /** Best resource from resources set. */
  private volatile Optional<Resource> bestResource = Optional.empty();

  private XMPPContact(JID bareJid, ContactStatus baseStatus, String nickname) {
    this.bareJid = Objects.requireNonNull(bareJid, "bareJid is null").getBareJID();
    this.baseStatus = Objects.requireNonNull(baseStatus, "baseStatus is null");
    this.nickname = Optional.ofNullable(nickname);
  }

  /**
   * This method should be avoided in future use outside of {@link saros.net.xmpp}
   *
   * @return Bare JID of this contact
   */
  public JID getBareJid() {
    return bareJid;
  }

  /**
   * If available returns the roster provided nickname, alternatively the bare JID of this contact.
   *
   * @return String containing nickname or JID of this contact
   */
  public String getDisplayableName() {
    return nickname.orElse(bareJid.getRAW());
  }

  /**
   * Get the combination of nickname (if available) and bare JID of this contact.
   *
   * @return String if nickname available in format "nickname (bare JID)" otherwise just bare JID
   */
  public String getDisplayableNameLong() {
    return nickname
        .map(name -> String.format("%s (%s)", name, bareJid.getRAW()))
        .orElse(bareJid.getRAW());
  }

  /**
   * Get the nickname if available.
   *
   * @return Optional with String of nickname if available
   */
  public Optional<String> getNickname() {
    return nickname;
  }

  /**
   * Provide the best resource of a contact currently online.
   *
   * @deprecated This method should be avoided in future use outside of {@link saros.net.xmpp}
   * @return Optional the JID of a resource online regardless of Saros support
   */
  @Deprecated
  public Optional<JID> getOnlineJid() {
    return bestResource.map(r -> r.fullJid);
  }

  /**
   * Provide the JID of a Saros Supporting resource if available.
   *
   * @deprecated This method should be avoided in future use outside of {@link saros.net.xmpp}
   * @return Optional with JID if available
   */
  @Deprecated
  public Optional<JID> getSarosJid() {
    return bestResource.filter(r -> r.sarosSupport).map(r -> r.fullJid);
  }

  /**
   * Get the latest available status information.
   *
   * @return current {@link ContactStatus}
   */
  public ContactStatus getStatus() {
    return bestResource.map(r -> r.status).orElse(baseStatus);
  }

  /**
   * Get Saros support of contact.
   *
   * @return true if contact has Saros support
   */
  public boolean hasSarosSupport() {
    return bestResource.map(r -> r.sarosSupport).orElse(false);
  }

  /**
   * Remove resource informations.
   *
   * @param fullJid
   * @return true if operation changed general status of contact
   */
  boolean removeResource(JID fullJid) {
    ContactStatus oldStatus = getStatus();
    boolean oldSupport = hasSarosSupport();
    resources.removeIf(res -> compareRawJid(res.fullJid, fullJid));
    updateBestResource();
    return !getStatus().equals(oldStatus) || oldSupport != hasSarosSupport();
  }

  /** Remove all resources. */
  void removeResources() {
    resources.clear();
    bestResource = Optional.empty();
  }

  /**
   * Set the general status of this contact.
   *
   * @param status
   * @return true if operation changed general status of contact
   */
  boolean setBaseStatus(ContactStatus status) {
    ContactStatus oldStatus = getStatus();
    baseStatus = status;
    return !getStatus().equals(oldStatus);
  }

  /**
   * Changes the contact nickname.
   *
   * @param newNickname
   * @return true if operation changed nickname of contact
   */
  boolean setNickname(String newNickname) {
    if (Objects.equals(nickname.orElse(null), newNickname)) return false;
    nickname = Optional.ofNullable(newNickname);
    return true;
  }

  /**
   * Adds or sets a new resource state.
   *
   * @param fullJid
   * @param status
   * @return true if operation changed general status of contact
   */
  boolean setResourceStatus(JID fullJid, ContactStatus status, boolean sarosSupport) {
    ContactStatus oldStatus = getStatus();
    boolean oldSarosSupport = hasSarosSupport();

    resources.removeIf(r -> compareRawJid(r.fullJid, fullJid));
    resources.add(new Resource(fullJid, status, sarosSupport));
    updateBestResource();

    if (oldSarosSupport != hasSarosSupport()) return true;
    return !getStatus().equals(oldStatus);
  }

  /**
   * After we received a Available presence we can assume that all pending subscriptions are done.
   *
   * @return true if operation changed general status of contact
   */
  boolean setSubscribed() {
    Type currentStatus = getStatus().getType();
    if (currentStatus == Type.SUBSCRIPTION_CANCELED || currentStatus == Type.SUBSCRIPTION_PENDING) {
      return setBaseStatus(ContactStatus.TYPE_OFFLINE);
    }
    return false;
  }

  private static boolean compareRawJid(JID jid1, JID jid2) {
    return jid1.getRAW().equals(jid2.getRAW());
  }

  private void updateBestResource() {
    bestResource = resources.stream().sorted(Resource.BEST_RESOURCE_FIRST).findFirst();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    XMPPContact other = (XMPPContact) obj;
    return Objects.equals(bareJid, other.bareJid);
  }

  @Override
  public int hashCode() {
    return bareJid.hashCode();
  }

  @Override
  public String toString() {
    return String.format(
        "XMPPContact [bareJid=%s, baseStatus=%s, bestResource=%s]",
        bareJid, baseStatus, bestResource.orElse(null));
  }
}
