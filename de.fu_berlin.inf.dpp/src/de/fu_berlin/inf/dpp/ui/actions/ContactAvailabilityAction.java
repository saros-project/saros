package de.fu_berlin.inf.dpp.ui.actions;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.util.XMPPUtils;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.roster.AbstractRosterListener;
import de.fu_berlin.inf.dpp.net.xmpp.roster.IRosterListener;
import de.fu_berlin.inf.dpp.net.xmpp.roster.RosterTracker;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;

/**
 * Simple action which shows the availability status of a contact in a separate
 * window. Currently only the message for availability modes away, xa, and dnd
 * are supported.
 */

// TODO icons
public class ContactAvailabilityAction extends Action implements Disposable {

    public static final String ACTION_ID = ContactAvailabilityAction.class
        .getName();

    private static final Logger LOG = Logger
        .getLogger(ContactAvailabilityAction.class);

    private static final String UNAVAILABLE_TEXT = "Show Unavailable Message";

    @Inject
    private RosterTracker rosterTracker;

    private Roster roster;

    private ISelectionListener selectionListener = new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            updateEnablement();
        }
    };

    private final IRosterListener rosterListener = new AbstractRosterListener() {

        @Override
        public void rosterChanged(final Roster roster) {
            SWTUtils.runSafeSWTAsync(LOG, new Runnable() {
                @Override
                public void run() {
                    ContactAvailabilityAction.this.roster = roster;
                    updateEnablement();
                }
            });
        }

        @Override
        public void presenceChanged(Presence presence) {
            SWTUtils.runSafeSWTAsync(LOG, new Runnable() {
                @Override
                public void run() {
                    updateEnablement();
                }
            });
        }
    };

    public ContactAvailabilityAction() {
        super(UNAVAILABLE_TEXT);
        SarosPluginContext.initComponent(this);

        setId(ACTION_ID);

        rosterTracker.addRosterListener(rosterListener);
        SelectionUtils.getSelectionService().addSelectionListener(
            selectionListener);

        roster = rosterTracker.getRoster();

        updateEnablement();
    }

    @Override
    public void dispose() {
        rosterTracker.removeRosterListener(rosterListener);
        SelectionUtils.getSelectionService().removeSelectionListener(
            selectionListener);
    }

    @Override
    public void run() {

        final JID jid = getSelectedJID();

        if (jid == null)
            return;

        final String message = getAvailabilityMessage(jid);

        if (message.isEmpty())
            return;

        String nickname = XMPPUtils.getNickname(null, jid, jid.getRAW());

        MessageDialog.openInformation(SWTUtils.getShell(),
            "Unavailable Message for " + nickname, message);
    }

    /**
     * Returns the availability message for the given JID. Never
     * <code>null</code> but may be empty if no such message exist or the
     * contact is not in mode AWAY, XA, DND, or no connection to a XMPP server
     * is established.
     */
    private String getAvailabilityMessage(JID jid) {

        final Presence presence = getPresence(jid);

        if (presence == null || !presence.isAway())
            return "";

        final String message = presence.getStatus();

        return message == null ? "" : message.trim();
    }

    /**
     * Returns the presence for the given JID or <code>null</code> if no
     * presence information are available.
     */
    private Presence getPresence(JID jid) {

        if (roster == null)
            return null;

        final Presence presence;

        if (jid.isResourceQualifiedJID())
            presence = roster.getPresenceResource(jid.getRAW());
        else
            presence = roster.getPresence(jid.getBase());

        return presence;
    }

    private JID getSelectedJID() {
        List<JID> contacts = SelectionRetrieverFactory.getSelectionRetriever(
            JID.class).getSelection();

        if (contacts.size() != 1)
            return null;

        return contacts.get(0);
    }

    private void updateEnablement() {
        setEnabled(false);

        final JID jid = getSelectedJID();

        if (jid == null)
            return;

        final Presence presence = getPresence(jid);

        setEnabled(presence != null && presence.isAway());
    }
}
