package de.fu_berlin.inf.dpp.ui.model.roster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
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
import de.fu_berlin.inf.dpp.net.ITransferModeListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.IBytestreamConnection;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.net.internal.discoveryManager.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.internal.discoveryManager.events.DiscoveryManagerListener;
import de.fu_berlin.inf.dpp.ui.model.TreeContentProvider;
import de.fu_berlin.inf.dpp.ui.util.ViewerUtils;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * {@link IContentProvider} for use in conjunction with a {@link Roster} input.
 * <p>
 * Automatically keeps track of changes of buddies.
 * 
 * @author bkahlert
 */
public class RosterContentProvider extends TreeContentProvider {
    private static final Logger log = Logger
        .getLogger(RosterContentProvider.class);

    protected Viewer viewer;

    @Inject
    protected DataTransferManager dataTransferManager;
    protected ITransferModeListener transferModeListener = new ITransferModeListener() {
        public void transferFinished(JID jid, NetTransferMode newMode,
            boolean incoming, long sizeTransferred, long sizeUncompressed,
            long transmissionMillisecs) {
            // do nothing
        }

        public void connectionChanged(JID jid, IBytestreamConnection connection) {
            ViewerUtils.update(viewer, new RosterEntryElement(roster, jid),
                null);
        }

        public void clear() {
            ViewerUtils.refresh(viewer, true);
        }
    };

    @Inject
    protected DiscoveryManager discoveryManager;
    protected DiscoveryManagerListener discoveryManagerListener = new DiscoveryManagerListener() {
        public void featureSupportUpdated(final JID jid, String feature,
            boolean isSupported) {
            if (Saros.NAMESPACE.equals(feature)) {
                Utils.runSafeSWTAsync(log, new Runnable() {
                    public void run() {
                        // ViewerUtils.refresh(viewer, true);
                        ViewerUtils.update(viewer, new RosterEntryElement(
                            roster, jid), null);
                    }
                });
            }
        }
    };

    protected Roster roster;
    protected RosterListener rosterListener = new RosterListener() {
        public void presenceChanged(Presence presence) {
            ViewerUtils.update(viewer, new RosterEntryElement(roster, new JID(
                presence.getFrom())), null);
        }

        public void entriesUpdated(Collection<String> addresses) {
            ViewerUtils.refresh(viewer, true);
        }

        public void entriesDeleted(Collection<String> addresses) {
            ViewerUtils.refresh(viewer, true);
        }

        public void entriesAdded(Collection<String> addresses) {
            for (Iterator<String> iterator = addresses.iterator(); iterator
                .hasNext();) {
                String address = iterator.next();
                RosterEntryElement rosterEntryElement = new RosterEntryElement(
                    roster, new JID(address));
                if (rosterEntryElement.getParent() != null) {
                    ViewerUtils.add(viewer, rosterEntryElement.getParent(),
                        rosterEntryElement);
                } else {
                    ViewerUtils.refresh(viewer, true);
                }
            }
        }
    };

    public RosterContentProvider() {
        super();
        SarosPluginContext.initComponent(this);
        this.dataTransferManager.getTransferModeDispatch().add(
            this.transferModeListener);
        this.discoveryManager
            .addDiscoveryManagerListener(this.discoveryManagerListener);
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        this.viewer = viewer;

        if (oldInput instanceof Roster) {
            ((Roster) oldInput).removeRosterListener(this.rosterListener);
        }

        if (newInput instanceof Roster) {
            this.roster = (Roster) newInput;
            this.roster.addRosterListener(this.rosterListener);
        } else {
            this.roster = null;
        }
    }

    @Override
    public void dispose() {
        if (this.roster != null) {
            this.roster.removeRosterListener(this.rosterListener);
        }
        this.discoveryManager
            .removeDiscoveryManagerListener(this.discoveryManagerListener);
        this.dataTransferManager.getTransferModeDispatch().remove(
            transferModeListener);
    }

    /**
     * Returns {@link RosterGroup}s followed by {@link RosterEntry}s which don't
     * belong to any {@link RosterGroup}.
     */
    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement != null && inputElement instanceof Roster) {
            Roster roster = (Roster) inputElement;
            List<Object> elements = new ArrayList<Object>();

            for (RosterGroup rosterGroup : roster.getGroups())
                elements.add(new RosterGroupElement(roster, rosterGroup));

            for (RosterEntry rosterEntry : roster.getUnfiledEntries())
                elements.add(new RosterEntryElement(roster, new JID(rosterEntry
                    .getUser())));

            return elements.toArray();
        }

        return new Object[0];
    }

}
