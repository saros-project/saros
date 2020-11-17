package saros.ui.model.roster;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.model.HeaderElement;
import saros.ui.model.TreeElement;

/** Container {@link TreeElement} for a contact list. */
public class RosterHeaderElement extends HeaderElement {
  private final RosterContentProvider rosterContentProvider;
  private final XMPPContactsService contactsService;

  public RosterHeaderElement(
      Font font, RosterContentProvider rosterContentProvider, XMPPContactsService contactsService) {
    super(font);
    this.rosterContentProvider = rosterContentProvider;
    this.contactsService = contactsService;
  }

  @Override
  public StyledString getStyledText() {
    StyledString styledString = new StyledString();
    styledString.append(Messages.RosterHeaderElement_contacts, boldStyler);
    return styledString;
  }

  @Override
  public Image getImage() {
    return ImageManager.ICON_GROUP;
  }

  @Override
  public boolean hasChildren() {
    return true;
  }

  @Override
  public Object[] getChildren() {
    return rosterContentProvider.getElements(contactsService);
  }
}
