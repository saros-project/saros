package saros.ui.model.roster;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jface.viewers.IContentProvider;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactGroup;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.ui.model.TreeContentProvider;

/** {@link IContentProvider} for providing the contact list. */
public final class RosterContentProvider extends TreeContentProvider {

  /** Returns ContactGroups followed by Contacts which don't belong to any Group. */
  @Override
  public Object[] getElements(Object inputElement) {
    if (!(inputElement instanceof XMPPContactsService)) return new Object[0];
    XMPPContactsService contactsService = (XMPPContactsService) inputElement;

    Stream<RosterGroupElement> contactGroups =
        contactsService.getContactGroups().stream().map(this::createRosterGroupElement);
    Stream<RosterEntryElement> contactsWithoutGroup =
        createRosterEntryElements(contactsService.getContactsWithoutGroup()).stream();

    Object[] elements = Stream.concat(contactGroups, contactsWithoutGroup).toArray();
    return elements;
  }

  private RosterGroupElement createRosterGroupElement(XMPPContactGroup group) {
    return new RosterGroupElement(
        group.getName(),
        createRosterEntryElements(group.getContacts()).toArray(new RosterEntryElement[0]));
  }

  private List<RosterEntryElement> createRosterEntryElements(Collection<XMPPContact> contacts) {
    return contacts.stream().map(RosterEntryElement::new).collect(Collectors.toList());
  }
}
