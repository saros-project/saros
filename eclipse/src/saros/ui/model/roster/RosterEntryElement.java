package saros.ui.model.roster;

import java.util.Objects;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.ContactStatus;
import saros.net.xmpp.contact.XMPPContact;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.model.ITreeElement;
import saros.ui.model.TreeElement;

/** Wrapper for {@link XMPPContact XMPPContacts} in use with {@link Viewer Viewers} */
public class RosterEntryElement extends TreeElement {

  private final XMPPContact contact;

  public RosterEntryElement(XMPPContact contact) {
    this.contact = contact;
  }

  @Override
  public StyledString getStyledText() {
    StyledString styledString = new StyledString(contact.getDisplayableName());

    ContactStatus status = contact.getStatus();
    switch (status.getType()) {
      case SUBSCRIPTION_CANCELED:
        styledString
            .append(" ")
            .append(Messages.RosterEntryElement_subscription_canceled, StyledString.COUNTER_STYLER);
        break;
      case SUBSCRIPTION_PENDING:
        styledString
            .append(" ")
            .append(Messages.RosterEntryElement_subscription_pending, StyledString.COUNTER_STYLER);
        break;
      case AWAY:
        status
            .getMessage()
            .ifPresent(
                message -> styledString.append(" (" + message + ")", StyledString.COUNTER_STYLER));
        break;
      default:
        break;
    }

    return styledString;
  }

  @Override
  public Image getImage() {
    switch (contact.getStatus().getType()) {
      case AVAILABLE:
        return isSarosSupported()
            ? ImageManager.ICON_CONTACT_SAROS_SUPPORT
            : ImageManager.ICON_CONTACT;
      case AWAY:
        return isSarosSupported()
            ? ImageManager.ICON_USER_SAROS_AWAY
            : ImageManager.ICON_CONTACT_AWAY;
      default:
        return ImageManager.ICON_CONTACT_OFFLINE;
    }
  }

  public boolean isOnline() {
    return contact.getStatus().isOnline();
  }

  @Deprecated
  public JID getJID() {
    return contact.getBareJid();
  }

  public XMPPContact getContact() {
    return contact;
  }

  public boolean isSarosSupported() {
    return contact.hasSarosSupport();
  }

  @Override
  public ITreeElement getParent() {
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof RosterEntryElement)) return false;
    RosterEntryElement other = (RosterEntryElement) obj;
    return Objects.equals(contact, other.contact);
  }

  @Override
  public int hashCode() {
    return contact.hashCode();
  }

  @Override
  public String toString() {
    return "RosterEntryElement [contact=" + contact + "]";
  }
}
