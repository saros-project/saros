package de.fu_berlin.inf.dpp.ui.menuContributions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.discoveryManager.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.internal.discoveryManager.DiscoveryManager.CacheMissException;
import de.fu_berlin.inf.dpp.net.util.RosterUtils;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterEntryElement;
import de.fu_berlin.inf.dpp.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;

/**
 * This class fills a {@link Menu} with {@link MenuItem}s.<br/>
 * Each {@link MenuItem} represents a Saros enabled buddy.<br/>
 * A click leads to a shared project invitation.
 */
public class ProjectShareBuddies extends ContributionItem {

    @Inject
    protected Saros saros;

    @Inject
    protected SarosSessionManager sarosSessionManager;

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
        if (!this.saros.isConnected())
            return;

        final List<IProject> selectedProjects = SelectionRetrieverFactory
            .getSelectionRetriever(IProject.class).getSelection();

        int numSarosSupportedBuddies = 0;

        for (final RosterEntry rosterEntry : this.getSortedRosterEntries()) {
            boolean sarosSupport;
            try {
                sarosSupport = discoveryManager.isSupportedNonBlock(new JID(
                    rosterEntry.getUser()), Saros.NAMESPACE);
            } catch (CacheMissException e) {
                sarosSupport = true;
            }

            if (sarosSupport) {
                createBuddyMenuItem(menu, numSarosSupportedBuddies++,
                    rosterEntry, selectedProjects);
            }
        }

        if (numSarosSupportedBuddies == 0) {
            createNoBuddiesMenuItem(menu, numSarosSupportedBuddies);
        }
    }

    /**
     * Returns a sorted array of {@link Roster}'s buddies.
     * 
     * @return
     */
    protected RosterEntry[] getSortedRosterEntries() {
        RosterEntry[] rosterEntries = saros.getRoster().getEntries()
            .toArray(new RosterEntry[0]);
        Arrays.sort(rosterEntries, new Comparator<RosterEntry>() {
            public int compare(RosterEntry o1, RosterEntry o2) {
                String name1 = RosterUtils.getDisplayableName(o1);
                String name2 = RosterUtils.getDisplayableName(o2);
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
     * @param projects
     * @return
     */
    protected MenuItem createBuddyMenuItem(Menu parentMenu, int index,
        final RosterEntry rosterEntry, final List<IProject> projects) {

        /*
         * The model knows how to display roster entries best.
         */
        RosterEntryElement rosterEntryElement = new RosterEntryElement(
            saros.getRoster(), new JID(rosterEntry.getUser()));

        MenuItem menuItem = new MenuItem(parentMenu, SWT.NONE, index);
        menuItem.setText(rosterEntryElement.getStyledText().toString());
        menuItem.setImage(rosterEntryElement.getImage());

        menuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                List<JID> buddies = new ArrayList<JID>();
                buddies.add(new JID(rosterEntry));
                CollaborationUtils.shareProjectWith(sarosSessionManager,
                    projects, buddies);
            }
        });

        return menuItem;
    }

    /**
     * Creates a menu entry which indicates that no Saros enabled buddies are
     * online.
     * 
     * @param parentMenu
     * @param index
     * @return
     */
    protected MenuItem createNoBuddiesMenuItem(Menu parentMenu, int index) {
        MenuItem menuItem = new MenuItem(parentMenu, SWT.NONE, index);
        menuItem.setText("No Buddies With Saros Support Online");
        menuItem.setEnabled(false);
        return menuItem;
    }

}
