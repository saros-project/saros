package de.fu_berlin.inf.dpp.ui.model.roster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.discovery.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.discovery.DiscoveryManagerListener;
import de.fu_berlin.inf.dpp.ui.model.TreeContentProvider;
import de.fu_berlin.inf.dpp.ui.util.ViewerUtils;

/**
 * {@link IContentProvider} for use in conjunction with a {@link Roster} input.
 * <p>
 * Automatically keeps track of changes of contacts.
 * 
 * @author bkahlert
 */
public final class RosterContentProvider extends TreeContentProvider {

    private Viewer viewer;
    private Roster roster;

    @Inject
    private DiscoveryManager discoveryManager;

    private final DiscoveryManagerListener discoveryManagerListener = new DiscoveryManagerListener() {
        @Override
        public void featureSupportUpdated(final JID jid, String feature,
            boolean isSupported) {

            if (Saros.NAMESPACE.equals(feature))
                ViewerUtils.update(viewer, new RosterEntryElement(roster, jid),
                    null);
        }
    };

    private final RosterListener rosterListener = new RosterListener() {
        @Override
        public void presenceChanged(Presence presence) {
            ViewerUtils.update(viewer, new RosterEntryElement(roster, new JID(
                presence.getFrom())), null);
        }

        @Override
        public void entriesUpdated(Collection<String> addresses) {
            ViewerUtils.refresh(viewer, true);
        }

        @Override
        public void entriesDeleted(Collection<String> addresses) {
            ViewerUtils.refresh(viewer, true);
        }

        @Override
        public void entriesAdded(Collection<String> addresses) {
            ViewerUtils.refresh(viewer, true);
        }
    };

    public RosterContentProvider() {
        SarosPluginContext.initComponent(this);

        discoveryManager
            .addDiscoveryManagerListener(this.discoveryManagerListener);
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        this.viewer = viewer;

        if (oldInput instanceof Roster)
            ((Roster) oldInput).removeRosterListener(rosterListener);

        roster = null;

        if (newInput instanceof Roster) {
            roster = (Roster) newInput;
            roster.addRosterListener(rosterListener);
        }
    }

    @Override
    public void dispose() {
        if (roster != null)
            roster.removeRosterListener(rosterListener);

        discoveryManager
            .removeDiscoveryManagerListener(discoveryManagerListener);

        roster = null;
        discoveryManager = null;
    }

    /**
     * Returns {@link RosterGroup}s followed by {@link RosterEntry}s which don't
     * belong to any {@link RosterGroup}.
     */
    @Override
    public Object[] getElements(Object inputElement) {

        if (!(inputElement instanceof Roster))
            return new Object[0];

        Roster roster = (Roster) inputElement;
        List<Object> elements = new ArrayList<Object>();

        for (RosterGroup rosterGroup : roster.getGroups())
            elements.add(new RosterGroupElement(roster, rosterGroup));

        for (RosterEntry rosterEntry : roster.getUnfiledEntries())
            elements.add(new RosterEntryElement(roster, new JID(rosterEntry
                .getUser())));

        return elements.toArray();
    }
}
