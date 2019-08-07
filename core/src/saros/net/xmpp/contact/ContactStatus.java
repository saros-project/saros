package saros.net.xmpp.contact;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents the current status of a contact. A status is mainly defined by a {@link
 * ContactStatus.Type} provided via {@link #getType()} and can have a message {@link #getMessage()}.
 */
public class ContactStatus implements Comparable<ContactStatus> {

  /**
   * Describing the type of contact status as a simplified view on all the possible states XMPP
   * allows.
   *
   * <p>This enum is ordered by priority, which is used to sort contact status.
   */
  public enum Type {
    /** Online and Available */
    AVAILABLE,
    /** Online but Away */
    AWAY,
    /** Subscription is Pending */
    SUBSCRIPTION_PENDING,
    /** Subscription is Canceled */
    SUBSCRIPTION_CANCELED,
    /** Contact is removed from Contact list */
    REMOVED,
    /** Offline */
    OFFLINE
  }

  static final ContactStatus TYPE_AVAILABLE = newInstance(Type.AVAILABLE);
  static final ContactStatus TYPE_SUBSCRIPTION_PENDING = newInstance(Type.SUBSCRIPTION_PENDING);
  static final ContactStatus TYPE_SUBSCRIPTION_CANCELED = newInstance(Type.SUBSCRIPTION_CANCELED);
  static final ContactStatus TYPE_REMOVED = newInstance(Type.REMOVED);
  static final ContactStatus TYPE_OFFLINE = newInstance(Type.OFFLINE);

  /**
   * Creates a new ContactStatus. If possible use static provided ones e.g. {@link #TYPE_AVAILABLE}.
   *
   * @param status
   * @param message
   * @return a new ContactStatus
   */
  static ContactStatus newInstance(Type status, String message) {
    return new ContactStatus(status, message);
  }

  private final Type status;
  private final String message;

  private ContactStatus(Type status, String message) {
    this.status = Objects.requireNonNull(status);
    this.message = message;
  }

  private static ContactStatus newInstance(Type status) {
    return new ContactStatus(status, null);
  }

  /**
   * Get the {@link Type} of this Status.
   *
   * @return {@link Type}
   */
  public Type getType() {
    return status;
  }

  /**
   * Get a contact provided status message if available.
   *
   * @return Optional of message
   */
  public Optional<String> getMessage() {
    return Optional.ofNullable(message);
  }

  /**
   * Returns if contact is online.
   *
   * @return true if status is {@link Type#AVAILABLE} or {@link Type#AWAY}
   */
  public boolean isOnline() {
    return status == Type.AVAILABLE || status == Type.AWAY;
  }

  @Override
  public int compareTo(ContactStatus o) {
    return status.compareTo(o.status);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ContactStatus other = (ContactStatus) obj;
    return status == other.status && Objects.equals(message, other.message);
  }

  @Override
  public int hashCode() {
    return 31 * Objects.hashCode(status) + Objects.hashCode(message);
  }

  @Override
  public String toString() {
    return "ContactStatus [mode=" + status + ", message=" + message + "]";
  }
}
