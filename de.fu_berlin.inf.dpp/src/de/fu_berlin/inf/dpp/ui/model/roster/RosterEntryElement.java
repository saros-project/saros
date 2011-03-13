package de.fu_berlin.inf.dpp.ui.model.roster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.packet.RosterPacket;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.net.internal.discoveryManager.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.internal.discoveryManager.DiscoveryManager.CacheMissException;
import de.fu_berlin.inf.dpp.net.util.RosterUtils;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.model.ITreeElement;
import de.fu_berlin.inf.dpp.ui.model.TreeElement;

/**
 * Wrapper for {@link RosterEntryElement RosterEntryElements} in use with
 * {@link Viewer Viewers}
 * 
 * @author bkahlert
 */
public class RosterEntryElement extends TreeElement {
    @Inject
    protected DataTransferManager dataTransferManager;

    @Inject
    protected DiscoveryManager discoveryManager;

    @Inject
    protected PreferenceUtils preferenceUtils;

    protected Roster roster;
    protected JID jid;

    boolean checked = false;

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
        SarosPluginContext.initComponent(this);

        this.roster = roster;
        this.jid = jid;
    }

    protected RosterEntry getRosterEntry() {
        if (this.roster == null)
            return null;
        return roster.getEntry(jid.getBase());
    }

    @Override
    public StyledString getStyledText() {
        StyledString styledString = new StyledString();

        /*
         * Name
         */
        RosterEntry rosterEntry = this.getRosterEntry();
        styledString.append((rosterEntry == null) ? this.jid.toString()
            : RosterUtils.getDisplayableName(rosterEntry));

        /*
         * Append presence information if available. Otherwise append presence.
         */
        final Presence presence = roster.getPresence(this.jid.getBase());
        if (rosterEntry != null
            && rosterEntry.getStatus() == RosterPacket.ItemStatus.SUBSCRIPTION_PENDING) {
            styledString.append(" (subscription pending)",
                StyledString.COUNTER_STYLER);
        } else if (presence != null && presence.getType() != Type.available
            && presence.getType() != Type.unavailable) {
            styledString.append(" (" + presence.getType() + ")",
                StyledString.COUNTER_STYLER);
        }

        /*
         * Append DataTransfer state information if debug mode is enabled.
         */
        if (presence != null && presence.isAvailable()) {
            final NetTransferMode transferMode = dataTransferManager
                .getTransferMode(jid);

            if (transferMode != NetTransferMode.NONE) {
                styledString.append(" connected using: " + transferMode,
                    StyledString.QUALIFIER_STYLER);
            }
        }
        return styledString;
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

        RosterEntry rosterEntry = this.getRosterEntry();
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
