package saros.ui.renderer;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;
import saros.net.util.XMPPUtils;
import saros.ui.model.Contact;

public class ContactRenderer {
  /**
   * Factory method to create Contact from roster entry.
   *
   * @param entry the roster entry of the contact
   * @param presence the presence object of the contact
   * @return a new contact created from the roster entry
   */
  public static Contact convert(RosterEntry entry, Presence presence) {
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
      /*
       * see http://xmpp.org/rfcs/rfc3921.html chapter 8.2.1, 8.3.1 and
       * 8.6
       */
      addition = "subscription cancelled";
    } else if (presence.isAway()) {
      addition = presence.getMode().toString();
    }
    return addition;
  }
}
