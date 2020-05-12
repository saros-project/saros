package saros.ui.menu_contributions;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import saros.SarosPluginContext;
import saros.net.xmpp.XMPPConnectionService;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.ui.Messages;
import saros.ui.model.roster.RosterEntryElement;
import saros.ui.util.CollaborationUtils;
import saros.ui.util.selection.retriever.SelectionRetrieverFactory;

/**
 * This class fills a {@link Menu} with {@link MenuItem}s. Each entry represents a contact with
 * Saros support. A click starts a session negotiation.
 */
public class StartSessionWithContacts extends ContributionItem {

  @Inject private XMPPConnectionService connectionService;

  @Inject private XMPPContactsService contactsService;

  public StartSessionWithContacts() {
    this(null);
  }

  public StartSessionWithContacts(String id) {
    super(id);
    SarosPluginContext.initComponent(this);
  }

  @Override
  public void fill(Menu menu, int index) {
    if (!connectionService.isConnected()) return;

    final List<IResource> selectedResources =
        SelectionRetrieverFactory.getSelectionRetriever(IResource.class).getSelection();

    int numSarosSupportedContacts = 0;

    Iterator<XMPPContact> sortedContacts = getSortedContacts();
    while (sortedContacts.hasNext()) {
      XMPPContact contact = sortedContacts.next();
      if (contact.hasSarosSupport()) {
        createContactMenuItem(menu, numSarosSupportedContacts++, contact, selectedResources);
      }
    }

    if (numSarosSupportedContacts == 0) {
      createInvalidContactsMenuItem(menu, numSarosSupportedContacts);
    }
  }

  /** Returns a sorted iterator of xmpp contacts. */
  private Iterator<XMPPContact> getSortedContacts() {
    return contactsService
        .getAllContacts()
        .stream()
        .sorted(Comparator.comparing(XMPPContact::getDisplayableName))
        .iterator();
  }

  /** Creates a menu entry which shares projects with the given {@link XMPPContact}. */
  private MenuItem createContactMenuItem(
      Menu parentMenu, int index, XMPPContact contact, List<IResource> resources) {
    /*
     * The model knows how to display roster entries best.
     */
    RosterEntryElement rosterEntryElement = new RosterEntryElement(contact);

    MenuItem menuItem = new MenuItem(parentMenu, SWT.NONE, index);
    menuItem.setText(rosterEntryElement.getStyledText().toString());
    menuItem.setImage(rosterEntryElement.getImage());

    menuItem.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            CollaborationUtils.startSession(
                resources, Collections.singletonList(contact.getBareJid()));
          }
        });

    return menuItem;
  }

  /** Creates a menu entry which indicates that no contacts with Saros are online. */
  private MenuItem createInvalidContactsMenuItem(Menu parentMenu, int index) {
    MenuItem menuItem = new MenuItem(parentMenu, SWT.NONE, index);
    menuItem.setText(Messages.SessionWithContacts_menuItem_no_contacts_available_text);
    menuItem.setEnabled(false);
    return menuItem;
  }
}
