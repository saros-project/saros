package de.fu_berlin.inf.dpp.ui.model.roster;

import de.fu_berlin.inf.dpp.net.util.XMPPUtils;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.model.ITreeElement;
import de.fu_berlin.inf.dpp.ui.model.TreeElement;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;

/**
 * Wrapper for {@link RosterEntryElement RosterEntryElements} in use with {@link Viewer Viewers}
 *
 * @author bkahlert
 */
public class RosterEntryElement extends TreeElement {

  private final Roster roster;
  private final JID jid;

  private final boolean hasSarosSupport;

  public RosterEntryElement(Roster roster, JID jid, boolean hasSarosSupport) {

    this.roster = roster;
    this.jid = jid;
    this.hasSarosSupport = hasSarosSupport;
  }

  protected RosterEntry getRosterEntry() {
    if (roster == null) return null;

    return roster.getEntry(jid.getBase());
  }

  @Override
  public StyledString getStyledText() {
    StyledString styledString = new StyledString();

    final RosterEntry rosterEntry = getRosterEntry();

    if (rosterEntry == null) {
      styledString.append(jid.toString());
      return styledString;
    }

    styledString.append(XMPPUtils.getDisplayableName(rosterEntry));

    final Presence presence = roster.getPresence(jid.getBase());

    if (rosterEntry.getStatus() == RosterPacket.ItemStatus.SUBSCRIPTION_PENDING) {

      styledString
          .append(" ")
          .append( //$NON-NLS-1$
              Messages.RosterEntryElement_subscription_pending, StyledString.COUNTER_STYLER);

    } else if (rosterEntry.getType() == ItemType.none || rosterEntry.getType() == ItemType.from) {

      /*
       * see http://xmpp.org/rfcs/rfc3921.html chapter 8.2.1, 8.3.1 and
       * 8.6
       */

      styledString
          .append(" ")
          .append( //$NON-NLS-1$
              Messages.RosterEntryElement_subscription_canceled, StyledString.COUNTER_STYLER);

    } else if (presence.isAway()) {
      styledString.append(
          " (" + presence.getMode() + ")", // $NON-NLS-1$ //$NON-NLS-2$
          StyledString.COUNTER_STYLER);
    }

    return styledString;
  }

  @Override
  public Image getImage() {
    if (roster == null) return null;

    final Presence presence = roster.getPresence(jid.getBase());
    boolean sarosSupported = isSarosSupported();

    if (!presence.isAvailable()) return ImageManager.ICON_CONTACT_OFFLINE;

    Mode mode = presence.getMode();

    if (mode == null)
      // see Presence#getMode();
      mode = Mode.available;

    // TODO add icons for different modes
    switch (mode) {
      case away:
        // $FALL-THROUGH$
      case dnd:
        // $FALL-THROUGH$
      case xa:
        return sarosSupported ? ImageManager.ICON_USER_SAROS_AWAY : ImageManager.ICON_CONTACT_AWAY;
      case chat:
        // $FALL-THROUGH$
      case available:
        // $FALL-THROUGH$
      default:
        return sarosSupported ? ImageManager.ICON_CONTACT_SAROS_SUPPORT : ImageManager.ICON_CONTACT;
    }
  }

  public boolean isOnline() {
    if (roster == null) return false;

    return roster.getPresence(jid.getBase()).isAvailable();
  }

  public JID getJID() {
    return jid;
  }

  public boolean isSarosSupported() {
    return hasSarosSupport;
  }

  @Override
  public ITreeElement getParent() {
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof RosterEntryElement)) {
      return false;
    }

    RosterEntryElement rosterEntryElement = (RosterEntryElement) obj;
    return (jid == null ? rosterEntryElement.jid == null : jid.equals(rosterEntryElement.jid));
  }

  @Override
  public int hashCode() {
    return (jid != null) ? jid.hashCode() : 0;
  }
}
