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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
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
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.packet.Presence.Type;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.ITransferModeListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.SubscriptionListener;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.ui.actions.ConnectDisconnectAction;
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
public class RosterView extends ViewPart implements IConnectionListener,
    IRosterTree {

    private static Logger log = Logger.getLogger(RosterView.class);

    private TreeViewer viewer;

    private Roster roster;

    private XMPPConnection connection;

    // actions
    // private Action messagingAction;

    private Action inviteAction;

    private RenameContactAction renameContactAction;

    private DeleteContactAction deleteContactAction;

    private SkypeAction skypeAction;

    private Composite composite;

    private Label label;

    public RosterView() {

        DataTransferManager manager = Saros.getDefault().getContainer()
            .getComponent(DataTransferManager.class);
        manager.addTransferModeListener(new ITransferModeListener() {

            public void clear() {
                Util.runSafeSWTAsync(log, new Runnable() {
                    public void run() {
                        viewer.refresh();
                    }
                });
            }

            public void setTransferMode(final JID jid, NetTransferMode newMode,
                boolean incoming) {

                refreshRosterTree(jid);
            }
        });
    }

    /**
     * An item of the roster tree. Can be either a group or a single contact.
     * 
     * @author rdjemili
     */
    private interface TreeItem {

        /**
         * @return all child items of this tree item.
         */
        Object[] getChildren();
    }

    /**
     * A group item which holds a number of users.
     */
    private class GroupItem implements TreeItem {
        private final RosterGroup group;

        public GroupItem(RosterGroup group) {
            this.group = group;
        }

        /*
         * (non-Javadoc)
         * 
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
    private class UnfiledGroupItem implements TreeItem {

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
            if (parent.equals(getViewSite())
                && (RosterView.this.roster != null)) {
                List<TreeItem> groups = new LinkedList<TreeItem>();
                for (RosterGroup rg : RosterView.this.roster.getGroups()) {
                    GroupItem item = new GroupItem(rg);
                    groups.add(item);
                }

                // for (Iterator it = roster.getGroups(); it.hasNext();) {
                // GroupItem item = new GroupItem((RosterGroup) it.next());
                // groups.add(item);
                // }

                groups.add(new UnfiledGroupItem());

                return groups.toArray();
            }

            return new Object[0];
        }

        /*
         * @see org.eclipse.jface.viewers.ITreeContentProvider
         */
        public Object getParent(Object child) {
            return null; // TODO
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

        private Image personOfflineImage;

        public Image getPersonOfflineImage() {
            if (personOfflineImage == null) {
                synchronized (this) {
                    if (personOfflineImage == null) {
                        personOfflineImage = new Image(Display.getDefault(),
                            personImage, SWT.IMAGE_DISABLE);
                    }
                }
            }
            return personOfflineImage;
        }

        @Override
        public String getText(Object obj) {
            if (obj instanceof RosterEntry) {
                RosterEntry entry = (RosterEntry) obj;

                String label = entry.getName();

                // show JID if entry has no nickname
                if (label == null) {
                    label = entry.getUser();
                }

                // append presence information if available
                Presence presence = RosterView.this.roster.getPresence(entry
                    .getUser());
                RosterEntry e = RosterView.this.roster
                    .getEntry(entry.getUser());
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
                    return this.personImage;
                } else {
                    return getPersonOfflineImage();
                }
            }
            return this.groupImage;
        }

        public StyledString getStyledText(Object element) {
            if (element instanceof RosterEntry) {
                RosterEntry entry = (RosterEntry) element;

                StyledString result = new StyledString();

                String label = entry.getName();

                // show JID if entry has no nickname
                if (label == null) {
                    result.append(entry.getUser());
                } else {
                    result.append(label);
                }

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

                    DataTransferManager data = Saros.getDefault()
                        .getContainer().getComponent(DataTransferManager.class);

                    JID jid = new JID(entry.getUser());

                    NetTransferMode incoming = data
                        .getIncomingTransferMode(jid);
                    NetTransferMode outgoing = data
                        .getOutgoingTransferMode(jid);

                    if (incoming == null) {
                        if (outgoing == null) {
                            result.append(" [???]",
                                StyledString.QUALIFIER_STYLER);
                        } else {
                            result.append(" [" + outgoing.toString()
                                + "<->???]", StyledString.QUALIFIER_STYLER);
                        }
                    } else {
                        if (outgoing == null) {
                            result.append(" [???<->" + incoming.toString()
                                + "]", StyledString.QUALIFIER_STYLER);
                        } else {
                            if (incoming.equals(outgoing)) {
                                result.append(" [" + outgoing.toString() + "]",
                                    StyledString.QUALIFIER_STYLER);
                            } else {
                                result.append(" [" + outgoing.toString()
                                    + "<->" + incoming.toString() + "]",
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
    private class NameSorter extends ViewerSorter {
        @Override
        public int compare(Viewer viewer, Object elem1, Object elem2) {

            // sort by presence
            if (elem1 instanceof RosterEntry) {
                RosterEntry entry1 = (RosterEntry) elem1;

                if (elem2 instanceof RosterEntry) {
                    RosterEntry entry2 = (RosterEntry) elem2;

                    Presence presence1 = RosterView.this.roster
                        .getPresence(entry1.getUser());

                    Presence presence2 = RosterView.this.roster
                        .getPresence(entry2.getUser());

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
        updateEnablement();
        composite.layout();

        Saros saros = Saros.getDefault();
        saros.addListener(this);

        connectionStateChanged(saros.getConnection(), saros
            .getConnectionState());
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    @Override
    public void setFocus() {
        this.viewer.getControl().setFocus();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.IConnectionListener
     */
    public void connectionStateChanged(XMPPConnection connection,
        final ConnectionState newState) {
        if (newState == ConnectionState.CONNECTED) {
            // roster = Saros.getDefault().getRoster();
            this.roster = connection.getRoster();
            this.connection = connection;
            attachRosterListener();

        } else if (newState == ConnectionState.NOT_CONNECTED) {
            this.roster = null;
        }

        refreshRosterTree(true);

        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                updateStatusInformation(newState);
                updateEnablement();
            }
        });
    }

    /**
     * Needs to called from an UI thread.
     */
    private void updateEnablement() {
        this.label.setEnabled(Saros.getDefault().isConnected());
    }

    /**
     * Needs to called from an UI thread.
     */
    private void updateStatusInformation(final ConnectionState newState) {
        // IStatusLineManager statusLine = getViewSite().getActionBars()
        // .getStatusLineManager();
        // statusLine.setMessage(SarosUI.getDescription(newState));
        label.setText(SarosUI.getDescription(newState));
        composite.layout();
    }

    private void attachRosterListener() {

        this.connection.addPacketListener(new SubscriptionListener(
            this.connection, this), new PacketTypeFilter(Presence.class));

        this.connection.getRoster().addRosterListener(new RosterListener() {
            public void entriesAdded(Collection<String> addresses) {
                refreshRosterTree(true);
            }

            public void entriesUpdated(Collection<String> addresses) {
                for (String address : addresses) {
                    log.debug(address + ": "
                        + connection.getRoster().getEntry(address).getType()
                        + ", "
                        + connection.getRoster().getEntry(address).getStatus());
                }
                /*
                 * TODO Maybe we could only update those elements that have been
                 * updated (but make sure that the given addresses have not
                 * structurally changed/new buddy groups)
                 */
                refreshRosterTree(true);
            }

            public void entriesDeleted(Collection<String> addresses) {
                refreshRosterTree(false);
            }

            public void presenceChanged(Presence presence) {
                log.debug(presence.getFrom() + ": " + presence);
                refreshRosterTree(new JID(presence.getFrom()));
            }
        });
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
        if (this.viewer.getControl().isDisposed()) {
            return;
        }

        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                RosterView.this.viewer.refresh(updateLabels);
                RosterView.this.viewer.expandAll();
                RosterView.this.composite.layout();
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
        // menuManager.add(new TestJoinWizardAction());
        menuManager.add(new Separator());

        IToolBarManager toolBarManager = bars.getToolBarManager();
        toolBarManager.add(new ConnectDisconnectAction());
        toolBarManager.add(new NewContactAction());
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
        this.inviteAction = new InviteAction(this.viewer);
        this.renameContactAction = new RenameContactAction(this.viewer);
        this.deleteContactAction = new DeleteContactAction(this.viewer);
    }
}