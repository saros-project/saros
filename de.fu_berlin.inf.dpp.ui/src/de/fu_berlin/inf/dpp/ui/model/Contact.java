package de.fu_berlin.inf.dpp.ui.model;

import de.fu_berlin.inf.dpp.net.util.XMPPUtils;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;

/**
 * Represent an entry in a contact list.
 *
 * <p>This class is immutable.
 */
public class Contact {

  private final String displayName;

  private final String presence;

  private final String addition;

  private final String jid;

  /**
   * @param displayName the name of the contact as it should be displayed
   * @param presence a string indicating the online status
   * @param addition a string containing subscription status
   * @param jid a string that represents the {@link #jid}
   */
  public Contact(String displayName, String presence, String addition, String jid) {
    this.displayName = displayName;
    this.presence = presence;
    this.addition = addition;
    this.jid = jid;
  }

  /**
   * Factory method to create Contact from roster entry.
   *
   * @param entry the roster entry of the contact
   * @param presence the presence object of the contact
   * @return a new contact created from the roster entry
   */
  public static Contact createContact(RosterEntry entry, Presence presence) {
    // TODO Move this logic to a renderer class

    String displayableName = XMPPUtils.getDisplayableName(entry);
    String addition = createAdditionString(entry, presence);
    String presenceString = createPresenceString(presence);
    String jid = entry.getUser();
    return new Contact(displayableName, presenceString, addition, jid);
  }

  private static String createPresenceString(Presence presence) {
    if (!presence.isAvailable()) return "Offline";

    Presence.Mode mode = presence.getMode();
    if (mode == null) {
      // see Presence#getMode();
      mode = Presence.Mode.available;
    }

    switch (mode) {
      case away:
        return "Away";
      case dnd:
        return "DND";
      case xa:
        return "XA";
      case chat:
        return "Chat";
      case available:
        return "Online";
      default:
        return "Online";
    }
  }

  private static String createAdditionString(RosterEntry entry, Presence presence) {
    String addition = "";
    if (entry.getStatus() == RosterPacket.ItemStatus.SUBSCRIPTION_PENDING) {
      addition = "subscription pending";
    } else if (entry.getType() == RosterPacket.ItemType.none
        || entry.getType() == RosterPacket.ItemType.from) {
      addition = "subscription cancelled";
    } else if (presence.isAway()) {
      addition = presence.getMode().toString();
    }
    return addition;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getPresenceString() {
    return presence;
  }

  public String getAddition() {
    return addition;
  }

  public String getJid() {
    return jid;
  }
}
