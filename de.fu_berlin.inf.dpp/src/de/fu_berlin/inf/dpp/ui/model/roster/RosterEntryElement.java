package de.fu_berlin.inf.dpp.ui.model.roster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.discoveryManager.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.internal.discoveryManager.DiscoveryManager.CacheMissException;
import de.fu_berlin.inf.dpp.net.util.RosterUtils;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.model.CheckBoxTreeElement;
import de.fu_berlin.inf.dpp.ui.model.ITreeElement;

/**
 * Wrapper for {@link RosterEntryElement RosterEntryElements} in use with
 * {@link Viewer Viewers}
 * 
 * @author bkahlert
 */
public class RosterEntryElement extends CheckBoxTreeElement {
    @Inject
    protected DiscoveryManager discoveryManager;

    protected Roster roster;
    protected JID jid;

    public static RosterEntryElement[] createAll(Roster roster,
        Collection<String> addresses) {
        List<RosterEntryElement> rosterEntryElements = new ArrayList<RosterEntryElement>();
        for (Iterator<String> iterator = addresses.iterator(); iterator
            .hasNext();) {
            String address = iterator.next();
            rosterEntryElements.add(new RosterEntryElement(roster, new JID(
                address)));
        }
        return rosterEntryElements.toArray(new RosterEntryElement[0]);
    }

    public RosterEntryElement(Roster roster, JID jid) {
        Saros.injectDependenciesOnly(this);

        this.roster = roster;
        this.jid = jid;
    }

    protected RosterEntry getRosterEntry() {
        if (this.roster == null)
            return null;
        return roster.getEntry(jid.getBase());
    }

    @Override
    public String getText() {
        if (this.roster == null)
            return null;

        RosterEntry rosterEntry = this.roster.getEntry(this.jid.getBase());
        return (rosterEntry == null) ? this.jid.toString() : RosterUtils
            .getDisplayableName(rosterEntry);
    }

    @Override
    public Image getImage() {
        if (this.roster == null)
            return null;

        final Presence presence = this.roster.getPresence(jid.getBase());
        boolean sarosSupported = isSarosSupported();

        if (presence.isAvailable()) {
            if (presence.isAway()) {
                return sarosSupported ? ImageManager.ICON_BUDDY_SAROS_AWAY
                    : ImageManager.ICON_BUDDY_AWAY;
            } else {
                return sarosSupported ? ImageManager.ICON_BUDDY_SAROS
                    : ImageManager.ICON_BUDDY;
            }
        } else {
            return ImageManager.ICON_BUDDY_OFFLINE;
        }
    }

    public boolean isSarosSupported() {
        boolean sarosSupported = false;

        try {
            sarosSupported = this.discoveryManager.isSupportedNonBlock(jid,
                Saros.NAMESPACE);
        } catch (CacheMissException e) {
            // Saros support wasn't in cache. Update the discovery manager.
            discoveryManager.cacheSarosSupport(jid);
        }

        return sarosSupported;
    }

    @Override
    public ITreeElement getParent() {
        if (this.roster == null)
            return null;

        RosterEntry rosterEntry = this.roster.getEntry(this.jid.getBase());
        if (rosterEntry == null)
            return null;

        Collection<RosterGroup> rosterGroups = rosterEntry.getGroups();
        return (rosterGroups.size() > 0) ? new RosterGroupElement(roster,
            rosterGroups.iterator().next()) : null;
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
        return (this.jid == null ? rosterEntryElement.jid == null : this.jid
            .equals(rosterEntryElement.jid));
    }

    @Override
    public int hashCode() {
        return (this.jid != null) ? this.jid.hashCode() : 0;
    }
}
