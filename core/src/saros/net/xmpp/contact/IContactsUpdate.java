package saros.net.xmpp.contact;

import java.util.Optional;
import saros.net.xmpp.XMPPConnectionService;

/** Interface for receiving Contact Updates from {@link XMPPConnectionService}. */
public interface IContactsUpdate {

  /** List of possible update types. */
  enum UpdateType {
    /** Contact added */
    ADDED,
    /** Service connected */
    CONNECTED,
    /** Service not connected */
    NOT_CONNECTED,
    /** Contact nickname changed */
    NICKNAME_CHANGED,
    /** Contact removed from contact list */
    REMOVED,
    /** Contact changed {@link ContactStatus} and maybe Saros support */
    STATUS,
    /** Contact changed Feature support */
    FEATURE_SUPPORT,
    /** Contact group mapping has changed */
    GROUP_MAPPING
  }

  /**
   * Notification about a contact update.
   *
   * @param contact if available
   * @param updateType {@link UpdateType}
   */
  public void update(Optional<XMPPContact> contact, UpdateType updateType);
}
