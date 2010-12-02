/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.packet.RosterPacket;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.feedback.ErrorLogManager;
import de.fu_berlin.inf.dpp.feedback.StatisticManager;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.IRosterListener;
import de.fu_berlin.inf.dpp.net.ITransferModeListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.net.internal.ConnectionTestManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.IBytestreamConnection;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.net.internal.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.internal.DiscoveryManager.CacheMissException;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.actions.ChangeXMPPAccountAction;
import de.fu_berlin.inf.dpp.ui.actions.ConnectionTestAction;
import de.fu_berlin.inf.dpp.ui.actions.DeleteContactAction;
import de.fu_berlin.inf.dpp.ui.actions.InviteAction;
import de.fu_berlin.inf.dpp.ui.actions.NewContactAction;
import de.fu_berlin.inf.dpp.ui.actions.RenameContactAction;
import de.fu_berlin.inf.dpp.ui.actions.SkypeAction;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * This view displays the roster (also known as contact list) of the local user.
 * 
 * FIXME RosterView does not work if a User is in several groups!
 * 
 * @author rdjemili
 */
@Component(module = "ui")
public class RosterView extends ViewPart {

    private static final Logger log = Logger.getLogger(RosterView.class);

    public static final Image groupImage = SarosUI.getImage("icons/group.png");
    public static final Image personImage = SarosUI.getImage("icons/user.png");
    public static final Image personImage_saros = SarosUI
        .getImage("icons/user_saros.png");
    public static final Image personAwayImage = SarosUI
        .getImage("icons/clock.png");
    public static final Image personOfflineImage = new Image(
        Display.getDefault(), personImage, SWT.IMAGE_DISABLE);

    protected TreeViewer viewer;

    protected Composite composite;

    protected Label label;

    protected Roster roster;

    /*
     * Actions
     */
    protected InviteAction inviteAction;

    protected RenameContactAction renameContactAction;

    protected ConnectionTestAction testAction;

    protected DeleteContactAction deleteContactAction;

    protected SkypeAction skypeAction;

    protected List<Disposable> disposables = new ArrayList<Disposable>();

    protected RosterViewTransferModeListener transferModeListener = new RosterViewTransferModeListener();

    @Inject
    protected Saros saros;

    @Inject
    protected SarosUI sarosUI;

    @Inject
    protected SarosSessionManager sessionManager;

    @Inject
    protected StatisticManager statisticManager;

    @Inject
    protected ErrorLogManager errorLogManager;

    @Inject
    protected PreferenceUtils preferenceUtils;

    @Inject
    protected DataTransferManager dataTransferManager;

    @Inject
    protected ConnectionTestManager connectionTestManager;

    @Inject
    protected RosterTracker rosterTracker;

    @Inject
    protected InvitationProcessObservable invitationProcesses;

    @Inject
    protected DiscoveryManager discoveryManager;

    @Inject
    protected XMPPAccountStore accountStore;

    /*
     * TODO Maybe we could only update those elements that have been updated
     * (but make sure that the given addresses have not structurally changed/new
     * buddy groups)
     */
    protected IRosterListener rosterListener = new IRosterListener() {

        public void entriesAdded(Collection<String> addresses) {
            refreshRosterTree(true);
        }

        public void entriesUpdated(Collection<String> addresses) {
            refreshRosterTree(true);
        }

        public void entriesDeleted(Collection<String> addresses) {
            refreshRosterTree(false);
        }

        public void presenceChanged(Presence presence) {
            refreshRosterTree(true);
        }

        public void rosterChanged(Roster newRoster) {
            roster = newRoster;
            refreshRosterTree(true);
        }
    };

    protected final IConnectionListener connectionListener = new IConnectionListener() {

        public void connectionStateChanged(XMPPConnection connection,
            final ConnectionState newState) {

            Util.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    updateStatusInformation(newState);
                    updateEnablement();
                }
            });
        }

    };

    protected final class RosterViewTransferModeListener implements
        ITransferModeListener {

        public void clear() {
            refreshRosterTree(true);
        }

        public void transferFinished(JID jid, NetTransferMode newMode,
            boolean incoming, long size, long transmissionMillisecs) {
            // we are not interested in transfer statistics
        }

        public void connectionChanged(JID jid, IBytestreamConnection connection) {
            refreshRosterTree(jid);
        }
    }

    /**
     * An item of the roster tree. Can be either a group or a single contact.
     * 
     * @author rdjemili
     */
    public interface TreeItem {

        /**
         * @return {@link JID} for this {@link TreeItem} or <code>null</code> if
         *         there is none associated with it.
         */
        JID getJID();

        /**
         * @return the {@link RosterEntry} for this {@link TreeItem} or
         *         <code>null</code> if there is none associated with it.
         */
        RosterEntry getRosterEntry();

        /**
         * @return all child items of this tree item.
         */
        Collection<TreeItem> getChildren();

        /**
         * @return if this {@link TreeItem} has children.
         */
        boolean hasChildren();

        /**
         * @return image to display for this tree item.
         */
        Image getImage();

        /**
         * @return item rendered as {@link StyledString} for display.
         */
        StyledString getStyledText();

        /**
         * @return the number of the category this item belongs to. Used to sort
         *         {@link TreeItem}s. Items are first sorted by category, then
         *         by the "normal" comparison result.
         */
        int getCategory();
    }

    /**
     * A contact item for a single user.
     */
    protected class ContactItem implements TreeItem {

        protected final String jid;

        public ContactItem(String jid) {
            if (jid == null) {
                throw new IllegalArgumentException("jid must not be null");
            }
            this.jid = jid;
        }

        public JID getJID() {
            return new JID(jid);
        }

        public RosterEntry getRosterEntry() {
            return roster.getEntry(jid);
        }

        public Collection<TreeItem> getChildren() {
            return Collections.emptyList();
        }

        public boolean hasChildren() {
            return false;
        }

        public Image getImage() {
            final Presence presence = roster.getPresence(jid);
            boolean rqPeer = false;

            // Check cache for Saros-support.
            try {
                rqPeer = discoveryManager.isSupportedNonBlock(getJID(),
                    Saros.NAMESPACE);
            } catch (CacheMissException e) {
                // Saros support wasn't in cache. Update the discovery manager.
                discoveryManager.cacheSarosSupport(getJID());
            }

            if (presence.isAvailable() && saros.isConnected()) {
                if (presence.isAway()) {
                    return personAwayImage;
                } else {
                    return rqPeer == false ? personImage : personImage_saros;
                }
            } else {
                return personOfflineImage;
            }
        }

        public StyledString getStyledText() {
            // TODO Add a description of the pattern of the result.
            final StyledString result = new StyledString();

            final String user = jid;
            final RosterEntry entry = RosterView.this.roster.getEntry(user);
            if (entry == null) {
                return result;
            }
            result.append(Util.getDisplayableName(entry));

            // Append presence information if available.
            final Presence presence = roster.getPresence(user);
            if (entry.getStatus() == RosterPacket.ItemStatus.SUBSCRIPTION_PENDING) {
                result.append(" (wait for permission)",
                    StyledString.COUNTER_STYLER);
            } else if (presence != null && presence.getType() != Type.available
                && presence.getType() != Type.unavailable) {
                // Available and Unavailable are visible in the icon color
                result.append(" (" + presence.getType() + ")",
                    StyledString.COUNTER_STYLER);
            }

            // Append DataTransfer state information.
            if (presence != null && presence.isAvailable()) {

                final JID jid = new JID(user);

                final NetTransferMode transferMode = dataTransferManager
                    .getTransferMode(jid);

                if (transferMode != NetTransferMode.NONE) {
                    result.append(" Connected using: " + transferMode,
                        StyledString.QUALIFIER_STYLER);
                }
            }
            return result;
        }

        /**
         * @return 0 if the contact is online, otherwise 1. So online contacts
         *         get sorted before offline ones.
         */
        public int getCategory() {
            return roster.getPresence(jid).isAvailable() ? 0 : 1;
        }

        @Override
        public int hashCode() {
            return jid.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof ContactItem)) {
                return false;
            }
            return jid.equals(((ContactItem) obj).jid);
        }

        @Override
        public String toString() {
            return Util.getDisplayableName(roster.getEntry(jid));
        }
    }

    /**
     * A group item which holds a number of users.
     * 
     * There are to concrete subclasses because of the way Smack handles groups
     * and unfiled contacts.
     */
    protected abstract class AbstractGroupItem implements TreeItem {

        /**
         * @return <code>null</code> because groups do not have a {@link JID}.
         */
        public JID getJID() {
            return null;
        }

        /**
         * @return <code>null</code> because groups do not have a
         *         {@link RosterEntry}.
         */
        public RosterEntry getRosterEntry() {
            return null;
        }

        public Image getImage() {
            return groupImage;
        }

        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }
    }

    /**
     * A group item for users in groups.
     */
    protected class GroupItem extends AbstractGroupItem {

        private final RosterGroup group;

        public GroupItem(RosterGroup group) {
            super();
            this.group = group;
        }

        public Collection<TreeItem> getChildren() {
            final List<TreeItem> result = new ArrayList<TreeItem>(
                group.getEntryCount());
            for (RosterEntry rosterEntry : group.getEntries()) {
                result.add(new ContactItem(rosterEntry.getUser()));
            }
            return result;
        }

        public boolean hasChildren() {
            return group.getEntryCount() > 0;
        }

        public StyledString getStyledText() {
            return new StyledString(this.toString());
        }

        /**
         * @return 1 so that {@link UnfiledGroupItem} will be sorted before all
         *         other groups.
         */
        public int getCategory() {
            return 1;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof GroupItem)) {
                return false;
            }
            return this.toString().equals(obj.toString());
        }

        @Override
        public String toString() {
            return group.getName();
        }
    }

    /**
     * A group item that holds all users that don't belong to another group.
     */
    protected class UnfiledGroupItem extends AbstractGroupItem {

        public Collection<TreeItem> getChildren() {
            final List<TreeItem> result = new ArrayList<TreeItem>(
                roster.getUnfiledEntryCount());
            for (RosterEntry rosterEntry : roster.getUnfiledEntries()) {
                result.add(new ContactItem(rosterEntry.getUser()));
            }
            return result;
        }

        public boolean hasChildren() {
            return roster.getUnfiledEntryCount() > 0;
        }

        public StyledString getStyledText() {
            return new StyledString(this.toString(),
                StyledString.QUALIFIER_STYLER);
        }

        /**
         * @return 0 so the unfiled contacts are sorted before the groups.
         * 
         * @see GroupItem#getCategory()
         */
        public int getCategory() {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof UnfiledGroupItem)) {
                return false;
            }
            return this.toString().equals(obj.toString());
        }

        @Override
        public String toString() {
            return "Buddies";
        }
    }

    /**
     * Provide tree content. Elements are of type {@link TreeItem}.
     */
    protected class TreeContentProvider implements IStructuredContentProvider,
        ITreeContentProvider {

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // do nothing
        }

        public void dispose() {
            // do nothing
        }

        public Object[] getElements(Object parent) {
            if (parent.equals(getViewSite()) && (roster != null)) {
                final List<TreeItem> groups = new LinkedList<TreeItem>();
                for (RosterGroup rosterGroup : roster.getGroups()) {
                    groups.add(new GroupItem(rosterGroup));
                }
                groups.add(new UnfiledGroupItem());

                return groups.toArray();
            }

            return new Object[0];
        }

        public Object getParent(Object child) {
            return null;
        }

        public Object[] getChildren(Object parent) {
            return ((TreeItem) parent).getChildren().toArray();
        }

        public boolean hasChildren(Object parent) {
            return ((TreeItem) parent).hasChildren();
        }
    }

    protected static class ViewLabelProvider extends LabelProvider implements
        IStyledLabelProvider {

        @Override
        public String getText(Object object) {
            log.warn("Unexpected call to getText(), getStyledText() should"
                + " be called instead.");
            return super.getText(object);
        }

        @Override
        public Image getImage(Object element) {
            return ((TreeItem) element).getImage();
        }

        public StyledString getStyledText(Object element) {
            return ((TreeItem) element).getStyledText();
        }
    }

    protected static class RosterComparator extends ViewerComparator {
        @Override
        public int category(Object element) {
            return ((TreeItem) element).getCategory();
        }
    }

    public RosterView() {
        super();

        // Make sure that we get all dependencies injected
        Saros.reinject(this);

        dataTransferManager.getTransferModeDispatch().add(transferModeListener);
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    @Override
    public void createPartControl(Composite parent) {

        this.composite = parent;
        this.composite.setBackground(Display.getDefault().getSystemColor(
            SWT.COLOR_WHITE));

        final GridLayout layout = new GridLayout(1, true);
        composite.setLayout(layout);
        this.label = new Label(composite, SWT.LEFT);
        this.accountStore.loadAccounts();
        if (this.accountStore.getActiveAccount() != null) {
            this.label.setText(String.format("%s Not Connected",
                this.accountStore.getActiveAccount()));
        } else {
            this.label.setText("No active account detected!");
        }
        this.label.setBackground(Display.getDefault().getSystemColor(
            SWT.COLOR_WHITE));
        this.label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        // TODO Should scroll
        this.viewer = new TreeViewer(composite, SWT.MULTI);
        this.viewer.getControl().setLayoutData(
            new GridData(SWT.FILL, SWT.FILL, true, true));
        this.viewer.setContentProvider(new TreeContentProvider());
        this.viewer.setLabelProvider(new DelegatingStyledCellLabelProvider(
            new ViewLabelProvider()));
        this.viewer.setComparator(new RosterComparator());
        this.viewer.setInput(getViewSite());
        this.viewer.expandAll();

        makeActions();
        hookContextMenu();
        // hookDoubleClickAction();
        contributeToActionBars();

        saros.addListener(connectionListener);
        rosterTracker.addRosterListener(rosterListener);
        connectionListener.connectionStateChanged(saros.getConnection(),
            saros.getConnectionState());
        rosterListener.rosterChanged(saros.getRoster());
    }

    /*
     * private String[] createAccountStringArray(List<XMPPAccount> accounts) {
     * String[] result = new String[accounts.size()]; for (int i = 0; i <
     * accounts.size(); i++) { // format is username@server String
     * accountAsString = accounts.get(i).toString(); result[i] =
     * accountAsString; } return result; }
     */

    @Override
    public void dispose() {
        super.dispose();

        for (Disposable disposable : disposables) {
            disposable.dispose();
        }

        saros.removeListener(connectionListener);

        dataTransferManager.getTransferModeDispatch().remove(
            transferModeListener);

        rosterTracker.removeRosterListener(rosterListener);

        /*
         * personImage.dispose(); personAwayImage.dispose();
         * personOfflineImage.dispose();
         */
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    @Override
    public void setFocus() {
        this.viewer.getControl().setFocus();
    }

    /**
     * @swt Needs to called from an UI thread.
     */
    protected void updateEnablement() {
        boolean connected = saros.isConnected();

        this.label.setEnabled(connected);
        this.renameContactAction.setEnabled(connected);
        this.deleteContactAction.setEnabled(connected);
        this.testAction.setEnabled(connected);

        if (connected) {
            label.setToolTipText("Connected using Saros " + saros.getVersion());
        } else {
            label.setToolTipText(null);
        }
    }

    /**
     * @swt Needs to called from UI thread.
     */
    protected void updateStatusInformation(final ConnectionState newState) {
        if (label.getShell().isDisposed())
            return;
        final String description = sarosUI.getDescription(newState);
        label.setText(description);
        composite.layout();
    }

    protected void refreshRosterTree(final JID jid) {
        if (viewer.getControl().isDisposed()) {
            return;
        }

        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                if (roster == null) {
                    return;
                }

                final RosterEntry entry = roster.getEntry(jid.getBase());

                if (entry == null) {
                    return;
                }

                viewer.update(new ContactItem(entry.getUser()), null);
            }
        });
    }

    /**
     * Refreshes the roster tree.
     * 
     * @param updateLabels
     *            <code>true</code> if item labels (might) have changed.
     *            <code>false</code> otherwise.
     */
    public void refreshRosterTree(final boolean updateLabels) {
        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                if (viewer == null || viewer.getControl().isDisposed()) {
                    return;
                }

                viewer.refresh(updateLabels);
                viewer.expandAll();
                composite.layout();
            }
        });
    }

    private void hookContextMenu() {
        final MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(final IMenuManager manager) {
                fillContextMenu(manager);
            }
        });

        final Menu menu = menuMgr.createContextMenu(viewer.getControl());

        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    // private void hookDoubleClickAction() {
    // this.viewer.addDoubleClickListener(new IDoubleClickListener() {
    // public void doubleClick(DoubleClickEvent event) {
    // if (RosterView.this.messagingAction.isEnabled()) {
    // RosterView.this.messagingAction.run();
    // }
    // }
    // });
    // }

    private void contributeToActionBars() {
        final IActionBars bars = getViewSite().getActionBars();

        final IMenuManager menuManager = bars.getMenuManager();
        // menuManager.add(this.messagingAction);
        menuManager.add(inviteAction);
        menuManager.add(new Separator());

        final IToolBarManager toolBarManager = bars.getToolBarManager();
        toolBarManager.add(new ChangeXMPPAccountAction());
        toolBarManager.add(new NewContactAction(saros));
    }

    private void fillContextMenu(IMenuManager manager) {
        // manager.add(this.messagingAction);
        manager.add(this.skypeAction);
        manager.add(this.inviteAction);
        manager.add(new Separator());
        manager.add(this.renameContactAction);
        manager.add(this.deleteContactAction);
        manager.add(this.testAction);
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        updateEnablement();
    }

    private void makeActions() {
        // this.messagingAction = new MessagingAction(this.viewer);
        this.skypeAction = new SkypeAction(this.viewer);
        this.inviteAction = new InviteAction(sessionManager, saros,
            this.viewer, discoveryManager, invitationProcesses);
        this.renameContactAction = new RenameContactAction(saros, this.viewer);
        this.deleteContactAction = new DeleteContactAction(saros, this.viewer);
        this.testAction = new ConnectionTestAction(saros,
            connectionTestManager, this.viewer);
    }
}