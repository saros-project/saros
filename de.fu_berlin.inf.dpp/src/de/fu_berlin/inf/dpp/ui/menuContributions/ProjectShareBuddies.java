package de.fu_berlin.inf.dpp.ui.menuContributions;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosConstants;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.util.XMPPUtils;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.net.xmpp.discovery.DiscoveryManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterEntryElement;
import de.fu_berlin.inf.dpp.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;

/**
 * This class fills a {@link Menu} with {@link MenuItem}s. Each {@link MenuItem}
 * represents a contact with Saros support. A click starts a session
 * negotiation.
 */
public class ProjectShareBuddies extends ContributionItem {

    @Inject
    protected XMPPConnectionService connectionService;

    @Inject
    protected DiscoveryManager discoveryManager;

    public ProjectShareBuddies() {
        this(null);
    }

    public ProjectShareBuddies(String id) {
        super(id);
        SarosPluginContext.initComponent(this);
    }

    @Override
    public void fill(Menu menu, int index) {
        if (!connectionService.isConnected())
            return;

        final List<IResource> selectedResources = SelectionRetrieverFactory
            .getSelectionRetriever(IResource.class).getSelection();

        int numSarosSupportedContacts = 0;

        for (final RosterEntry rosterEntry : getSortedRosterEntries()) {
            Boolean sarosSupport = discoveryManager.isFeatureSupported(new JID(
                rosterEntry.getUser()), SarosConstants.XMPP_FEATURE_NAMESPACE);

            if (sarosSupport != null && sarosSupport) {
                createContactMenuItem(menu, numSarosSupportedContacts++,
                    rosterEntry, selectedResources);
            }
        }

        if (numSarosSupportedContacts == 0) {
            createInvalidContactsMenuItem(menu, numSarosSupportedContacts);
        }
    }

    /**
     * Returns a sorted array of {@link Roster}'s contacts.
     * 
     * @return
     */
    protected RosterEntry[] getSortedRosterEntries() {
        RosterEntry[] rosterEntries = connectionService.getRoster()
            .getEntries().toArray(new RosterEntry[0]);

        Arrays.sort(rosterEntries, new Comparator<RosterEntry>() {
            @Override
            public int compare(RosterEntry o1, RosterEntry o2) {
                String name1 = XMPPUtils.getDisplayableName(o1);
                String name2 = XMPPUtils.getDisplayableName(o2);
                return name1.compareToIgnoreCase(name2);
            }
        });
        return rosterEntries;
    }

    /**
     * Creates a menu entry which shares projects with the given
     * {@link RosterEntry}.
     * 
     * @param parentMenu
     * @param index
     * @param rosterEntry
     * @param resources
     * @return
     */
    protected MenuItem createContactMenuItem(Menu parentMenu, int index,
        final RosterEntry rosterEntry, final List<IResource> resources) {

        /*
         * The model knows how to display roster entries best.
         */
        RosterEntryElement rosterEntryElement = new RosterEntryElement(
            connectionService.getRoster(), new JID(rosterEntry.getUser()), true);

        MenuItem menuItem = new MenuItem(parentMenu, SWT.NONE, index);
        menuItem.setText(rosterEntryElement.getStyledText().toString());
        menuItem.setImage(rosterEntryElement.getImage());

        menuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CollaborationUtils.startSession(resources,
                    Collections.singletonList(new JID(rosterEntry.getUser())));
            }
        });

        return menuItem;
    }

    /**
     * Creates a menu entry which indicates that no Saros enabled contacts are
     * online.
     * 
     * @param parentMenu
     * @param index
     * @return
     */
    protected MenuItem createInvalidContactsMenuItem(Menu parentMenu, int index) {
        MenuItem menuItem = new MenuItem(parentMenu, SWT.NONE, index);
        menuItem
            .setText(Messages.SessionWithBuddies_menuItem_no_contacts_available_text);
        menuItem.setEnabled(false);
        return menuItem;
    }

}
