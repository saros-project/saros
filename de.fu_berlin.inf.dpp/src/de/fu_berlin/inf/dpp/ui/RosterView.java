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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
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
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.packet.Presence.Type;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.feedback.ErrorLogManager;
import de.fu_berlin.inf.dpp.feedback.StatisticManager;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.IRosterListener;
import de.fu_berlin.inf.dpp.net.ITransferModeListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferManager;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferManager.FileTransferConnection;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferManager.IJingleStateListener;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferManager.JingleConnectionState;
import de.fu_berlin.inf.dpp.observables.JingleFileTransferManagerObservable;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.actions.ConnectDisconnectAction;
import de.fu_berlin.inf.dpp.ui.actions.DeleteContactAction;
import de.fu_berlin.inf.dpp.ui.actions.InviteAction;
import de.fu_berlin.inf.dpp.ui.actions.NewContactAction;
import de.fu_berlin.inf.dpp.ui.actions.RenameContactAction;
import de.fu_berlin.inf.dpp.ui.actions.SkypeAction;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.ValueChangeListener;

/**
 * This view displays the roster (also known as contact list) of the local user.
 * 
 * FIXME RosterView does not work if a User is in several groups!
 * 
 * @author rdjemili
 */
@Component(module = "ui")
public class RosterView extends ViewPart {

    private static Logger log = Logger.getLogger(RosterView.class);

    private TreeViewer viewer;

    private Composite composite;

    private Label label;

    private Roster roster;

    /*
     * Actions
     */
    private InviteAction inviteAction;

    private RenameContactAction renameContactAction;

    private DeleteContactAction deleteContactAction;

    private SkypeAction skypeAction;

    protected List<Disposable> disposables = new ArrayList<Disposable>();

    protected RosterViewTransferModeListener transferModeListener = new RosterViewTransferModeListener();

    @Inject
    protected DiscoveryManager discoManager;

    @Inject
    protected JingleFileTransferManagerObservable jingleManager;

    @Inject
    protected Saros saros;

    @Inject
    protected SarosUI sarosUI;

    @Inject
    protected SessionManager sessionManager;

    @Inject
    protected StatisticManager statisticManager;

    @Inject
    ErrorLogManager errorLogManager;

    @Inject
    protected PreferenceUtils preferenceUtils;

    @Inject
    protected DataTransferManager dataTransferManager;

    @Inject
    protected RosterTracker rosterTracker;

    public RosterView() {

        // Make sure that we get all dependencies injected
        Saros.reinject(this);

        jingleManager
            .addAndNotify(new ValueChangeListener<JingleFileTransferManager>() {

                IJingleStateListener stateListener = new IJingleStateListener() {
                    public void setState(JID jid, JingleConnectionState state) {
                        log.debug("JingleFileTransferManager sent state"
                            + " update for " + jid.toString() + ": " + state);
                        refreshRosterTree(jid);
                    }
                };

                JingleFileTransferManager currentManager = null;

                public void setValue(JingleFileTransferManager newValue) {

                    if (currentManager != null) {
                        currentManager.removeJingleStateListener(stateListener);
                    }

                    currentManager = newValue;

                    if (currentManager != null) {
                        currentManager.addJingleStateListener(stateListener);
                    }
                    refreshRosterTree(true);
                }
            });

        dataTransferManager.getTransferModeDispatch().add(transferModeListener);
    }

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
            log.trace("Presence changed: " + presence.getFrom() + ": "
                + presence);
            refreshRosterTree(new JID(presence.getFrom()));
        }

        public void rosterChanged(Roster newRoster) {
            roster = newRoster;
            refreshRosterTree(true);
        }
    };

    private IConnectionListener connectionListener = new IConnectionListener() {

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

    private final class RosterViewTransferModeListener implements
        ITransferModeListener {

        public void clear() {
            lastStateMap[0].clear();
            lastStateMap[1].clear();
            refreshRosterTree(true);
        }

        @SuppressWarnings("unchecked")
        Map<JID, NetTransferMode>[] lastStateMap = new HashMap[] {
            new HashMap<JID, NetTransferMode>(),
            new HashMap<JID, NetTransferMode>() };

        public void transferFinished(JID jid, NetTransferMode newMode,
            boolean incoming, long size, long transmissionMillisecs) {

            NetTransferMode lastState = lastStateMap[incoming ? 0 : 1].get(jid);
            if (newMode != lastState) {
                lastStateMap[incoming ? 0 : 1].put(jid, newMode);
                refreshRosterTree(jid);
            }
        }
    }

    /**
     * An item of the roster tree. Can be either a group or a single contact.
     * 
     * @author rdjemili
     */
    protected interface TreeItem {

        /**
         * @return all child items of this tree item.
         */
        Object[] getChildren();
    }

    /**
     * A group item which holds a number of users.
     */
    protected static class GroupItem implements TreeItem {
        private final RosterGroup group;

        public GroupItem(RosterGroup group) {
            this.group = group;
        }

        /*
         * @see de.fu_berlin.inf.dpp.ui.RosterView.TreeItem
         */
        public Object[] getChildren() {
            return this.group.getEntries().toArray();
        }

        @Override
        public String toString() {
            return this.group.getName();
        }
    }

    /**
     * A group item that holds all users that don't belong to another group.
     */
    protected class UnfiledGroupItem implements TreeItem {

        /*
         * (non-Javadoc)
         * 
         * @see de.fu_berlin.inf.dpp.ui.RosterView.TreeItem
         */
        public Object[] getChildren() {
            return RosterView.this.roster.getUnfiledEntries().toArray();
        }

        @Override
        public String toString() {
            /*
             * TODO This is confusing if the user really has a group named
             * "Buddies".
             */
            return "Buddies";
        }
    }

    /**
     * Provide tree content.
     */
    private class TreeContentProvider implements IStructuredContentProvider,
        ITreeContentProvider {

        /*
         * @see org.eclipse.jface.viewers.IContentProvider
         */
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
            // do nothing
        }

        /*
         * @see org.eclipse.jface.viewers.IContentProvider
         */
        public void dispose() {
            // do nothing
        }

        /*
         * @see org.eclipse.jface.viewers.IStructuredContentProvider
         */
        public Object[] getElements(Object parent) {
            if (parent.equals(getViewSite()) && (roster != null)) {
                List<TreeItem> groups = new LinkedList<TreeItem>();
                for (RosterGroup rosterGroup : roster.getGroups()) {
                    GroupItem item = new GroupItem(rosterGroup);
                    groups.add(item);
                }
                groups.add(new UnfiledGroupItem());

                return groups.toArray();
            }

            return new Object[0];
        }

        /*
         * @see org.eclipse.jface.viewers.ITreeContentProvider
         */
        public Object getParent(Object child) {
            return null; // TODO Is this always a good idea
        }

        /*
         * @see org.eclipse.jface.viewers.ITreeContentProvider
         */
        public Object[] getChildren(Object parent) {
            if (parent instanceof TreeItem) {
                return ((TreeItem) parent).getChildren();
            }

            return new Object[0];
        }

        /*
         * @see org.eclipse.jface.viewers.ITreeContentProvider
         */
        public boolean hasChildren(Object parent) {
            if (parent instanceof TreeItem) {
                Object[] children = ((TreeItem) parent).getChildren();
                return children.length > 0;
            }

            return false;
        }
    }

    /**
     * Shows user name and state in parenthesis.
     */
    private class ViewLabelProvider extends LabelProvider implements
        IStyledLabelProvider {
        private final Image groupImage = SarosUI.getImage("icons/group.png");

        private final Image personImage = SarosUI.getImage("icons/user.png");

        private final Image personAwayImage = SarosUI
            .getImage("icons/clock.png");

        private final Image personOfflineImage = new Image(
            Display.getDefault(), personImage, SWT.IMAGE_DISABLE);

        @Override
        public String getText(Object obj) {
            if (obj instanceof RosterEntry) {
                RosterEntry entry = (RosterEntry) obj;

                String label = Util.getDisplayableName(entry);

                // append presence information if available
                Presence presence = roster.getPresence(entry.getUser());
                RosterEntry e = roster.getEntry(entry.getUser());
                if (e.getStatus() == RosterPacket.ItemStatus.SUBSCRIPTION_PENDING) {
                    label = label + " (wait for permission)";
                } else if (presence != null) {
                    label = label + " (" + presence.getType() + ")";
                }

                return label;
            }

            return obj.toString();
        }

        @Override
        public Image getImage(Object element) {

            if (element instanceof RosterEntry) {
                RosterEntry entry = (RosterEntry) element;

                Presence presence = roster.getPresence(entry.getUser());

                if (presence.isAvailable()) {
                    return presence.isAway() ? personAwayImage : personImage;
                } else {
                    return personOfflineImage;
                }
            }
            return groupImage;
        }

        public StyledString getStyledText(Object element) {
            if (element instanceof RosterEntry) {
                RosterEntry entry = (RosterEntry) element;

                StyledString result = new StyledString();

                result.append(Util.getDisplayableName(entry));

                // append presence information if available
                Presence presence = RosterView.this.roster.getPresence(entry
                    .getUser());
                RosterEntry e = RosterView.this.roster
                    .getEntry(entry.getUser());
                if (e.getStatus() == RosterPacket.ItemStatus.SUBSCRIPTION_PENDING) {
                    result.append(" (wait for permission)",
                        StyledString.COUNTER_STYLER);
                } else if (presence != null
                    && presence.getType() != Type.available
                    && presence.getType() != Type.unavailable) {
                    // Available and Unavailable are visible in the icon color
                    result.append(" (" + presence.getType() + ")",
                        StyledString.COUNTER_STYLER);
                }

                // Append DataTransfer State information
                if (presence != null && presence.isAvailable()) {

                    JID jid = new JID(entry.getUser());

                    NetTransferMode in = dataTransferManager
                        .getIncomingTransferMode(jid);
                    NetTransferMode out = dataTransferManager
                        .getOutgoingTransferMode(jid);

                    if (in != NetTransferMode.UNKNOWN
                        || out != NetTransferMode.UNKNOWN) {
                        result.append(" Last Data Transfer - ",
                            StyledString.QUALIFIER_STYLER);
                    }
                    if (in != NetTransferMode.UNKNOWN) {
                        result.append("In: " + in.toString() + " ",
                            StyledString.QUALIFIER_STYLER);
                    }
                    if (out != NetTransferMode.UNKNOWN) {
                        result.append("Out: " + out.toString(),
                            StyledString.QUALIFIER_STYLER);
                    }

                    JingleFileTransferManager manager = jingleManager
                        .getValue();

                    if (manager != null) {

                        FileTransferConnection connection = manager
                            .getConnection(jid);

                        if (connection != null) {
                            JingleConnectionState state = connection.getState();
                            if (state == JingleConnectionState.ESTABLISHED) {
                                /*
                                 * result.append(" [" +
                                 * connection.getTransferMode().toString() +
                                 * "]", StyledString.QUALIFIER_STYLER);
                                 */
                            } else {
                                result.append(" [" + state.toString() + "]",
                                    StyledString.QUALIFIER_STYLER);
                            }
                        }
                    }
                }

                return result;
            }

            return new StyledString(element.toString());
        }
    }

    /**
     * A sorter that orders by presence and then by name.
     */
    protected class NameSorter extends ViewerSorter {
        @Override
        public int compare(Viewer viewer, Object elem1, Object elem2) {

            // sort by presence
            if (elem1 instanceof RosterEntry) {
                RosterEntry entry1 = (RosterEntry) elem1;

                if (elem2 instanceof RosterEntry) {
                    RosterEntry entry2 = (RosterEntry) elem2;

                    Presence presence1 = roster.getPresence(entry1.getUser());

                    Presence presence2 = roster.getPresence(entry2.getUser());

                    if (presence1 == null) {
                        if (presence2 != null) {
                            return 1;
                        }
                    } else {
                        if (presence2 == null) {
                            return -1;
                        }

                        // Both not null
                        if (presence1.isAvailable() && !presence2.isAvailable()) {
                            return -1;
                        } else if (!presence1.isAvailable()
                            && presence2.isAvailable()) {
                            return 1;
                        }
                    }
                }
            }

            // otherwise use default order
            return super.compare(viewer, elem1, elem2);
        }
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

        GridLayout layout = new GridLayout(1, true);
        composite.setLayout(layout);

        this.label = new Label(composite, SWT.LEFT);
        this.label.setText("Not Connected");
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
        this.viewer.setSorter(new NameSorter());
        this.viewer.setInput(getViewSite());
        this.viewer.expandAll();

        makeActions();
        hookContextMenu();
        // hookDoubleClickAction();
        contributeToActionBars();

        saros.addListener(connectionListener);
        rosterTracker.addRosterListener(rosterListener);
        connectionListener.connectionStateChanged(saros.getConnection(), saros
            .getConnectionState());
        rosterListener.rosterChanged(saros.getRoster());
    }

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
        this.label.setEnabled(saros.isConnected());
        if (saros.isConnected())
            label.setToolTipText("Connected using Saros " + saros.getVersion());
        else
            label.setToolTipText(null);
    }

    /**
     * @swt Needs to called from UI thread.
     */
    protected void updateStatusInformation(final ConnectionState newState) {
        label.setText(sarosUI.getDescription(newState));
        composite.layout();
    }

    protected void refreshRosterTree(final JID jid) {
        if (this.viewer.getControl().isDisposed()) {
            return;
        }

        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                if (roster == null)
                    return;

                RosterEntry entry = roster.getEntry(jid.getBase());

                if (entry == null)
                    return;

                viewer.update(entry, null);
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
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                RosterView.this.fillContextMenu(manager);
            }
        });

        Menu menu = menuMgr.createContextMenu(this.viewer.getControl());

        this.viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, this.viewer);
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
        IActionBars bars = getViewSite().getActionBars();

        IMenuManager menuManager = bars.getMenuManager();
        // menuManager.add(this.messagingAction);
        menuManager.add(this.inviteAction);
        menuManager.add(new Separator());

        IToolBarManager toolBarManager = bars.getToolBarManager();
        ConnectDisconnectAction connectAction = new ConnectDisconnectAction(
            sarosUI, saros, bars.getStatusLineManager(), statisticManager,
            errorLogManager, preferenceUtils);
        disposables.add(connectAction);
        toolBarManager.add(connectAction);
        toolBarManager.add(new NewContactAction(saros));
    }

    private void fillContextMenu(IMenuManager manager) {
        // manager.add(this.messagingAction);
        manager.add(this.skypeAction);
        manager.add(this.inviteAction);
        manager.add(new Separator());
        manager.add(this.renameContactAction);
        manager.add(this.deleteContactAction);

        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void makeActions() {
        // this.messagingAction = new MessagingAction(this.viewer);
        this.skypeAction = new SkypeAction(this.viewer);
        this.inviteAction = new InviteAction(sessionManager, saros,
            this.viewer, discoManager);
        this.renameContactAction = new RenameContactAction(saros, this.viewer);
        this.deleteContactAction = new DeleteContactAction(saros, this.viewer);
    }
}