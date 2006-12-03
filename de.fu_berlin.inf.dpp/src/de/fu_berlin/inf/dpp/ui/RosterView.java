/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.ui.actions.ConnectDisconnectAction;
import de.fu_berlin.inf.dpp.ui.actions.DeleteContactAction;
import de.fu_berlin.inf.dpp.ui.actions.InviteAction;
import de.fu_berlin.inf.dpp.ui.actions.MessagingAction;
import de.fu_berlin.inf.dpp.ui.actions.NewContactAction;
import de.fu_berlin.inf.dpp.ui.actions.RenameContactAction;
import de.fu_berlin.inf.dpp.ui.actions.SkypeAction;

/**
 * This view displays the roster (also known as contact list) of the local user.
 * 
 * @author rdjemili
 */
public class RosterView extends ViewPart implements IConnectionListener {
	private TreeViewer viewer;

	private Roster roster;

	// actions
	private Action messagingAction;

	private Action inviteAction;

	private RenameContactAction renameContactAction;

	private DeleteContactAction deleteContactAction;

	private SkypeAction skypeAction;

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
		private RosterGroup group;

		public GroupItem(RosterGroup group) {
			this.group = group;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fu_berlin.inf.dpp.ui.RosterView.TreeItem
		 */
		public Object[] getChildren() {
			List<RosterEntry> users = new LinkedList<RosterEntry>();

			for (Iterator it = group.getEntries(); it.hasNext();) {
				RosterEntry entry = (RosterEntry) it.next();
				
				if (entry.getType() == RosterPacket.ItemType.BOTH || 
					entry.getType() == RosterPacket.ItemType.TO) {
						users.add(entry);
				}
			}

			return users.toArray();
		}

		@Override
		public String toString() {
			return group.getName();
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
			List<RosterEntry> users = new LinkedList<RosterEntry>();

			for (Iterator it = roster.getUnfiledEntries(); it.hasNext();) {
				RosterEntry entry = (RosterEntry) it.next();
				
				if (entry.getType() == RosterPacket.ItemType.BOTH || 
					entry.getType() == RosterPacket.ItemType.TO) {
						users.add(entry);
				}
			}

			return users.toArray();
		}

		@Override
		public String toString() {
			return "Buddies";
		}
	}

	/**
	 * Provide tree content.
	 */
	private class TreeContentProvider implements IStructuredContentProvider, ITreeContentProvider {

		/*
		 * @see org.eclipse.jface.viewers.IContentProvider
		 */
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		/*
		 * @see org.eclipse.jface.viewers.IContentProvider
		 */
		public void dispose() {
		}

		/*
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider
		 */
		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite()) && roster != null) {
				List<TreeItem> groups = new LinkedList<TreeItem>();

				for (Iterator it = roster.getGroups(); it.hasNext();) {
					GroupItem item = new GroupItem((RosterGroup) it.next());
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
	private class ViewLabelProvider extends LabelProvider {
		private Image groupImage = SarosUI.getImage("icons/group.png");

		private Image personImage = SarosUI.getImage("icons/user.png");

		@Override
		public String getText(Object obj) {
			if (obj instanceof RosterEntry) {
				RosterEntry entry = (RosterEntry) obj;

				String label = entry.getName();

				// show JID if entry has no nickname
				if (label == null)
					label = entry.getUser();

				// append presence information if available
				Presence presence = roster.getPresence(entry.getUser());
				if (presence != null) {
					label = label + " (" + presence.getType() + ")";
				}

				return label;
			}

			return obj.toString();
		}

		@Override
		public Image getImage(Object element) {
			return element instanceof RosterEntry ? personImage : groupImage;
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

					String user1 = entry1.getUser();
					boolean presence1 = roster.getPresence(user1) != null;

					String user2 = entry2.getUser();
					boolean presence2 = roster.getPresence(user2) != null;

					if (presence1 && !presence2) {
						return -1;
					} else if (!presence1 && presence2) {
						return 1;
					}
				}
			}

			// otherwise use default order
			return super.compare(viewer, elem1, elem2);
		}
	}

	/**
	 * Creates an roster view..
	 */
	public RosterView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new TreeContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());
		viewer.expandAll();

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		updateEnablement();

		Saros saros = Saros.getDefault();
		saros.addListener(this);

		connectionStateChanged(saros.getConnection(), saros.getConnectionState());
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.listeners.IConnectionListener
	 */
	public void connectionStateChanged(XMPPConnection connection, final ConnectionState newState) {
		if (newState == ConnectionState.CONNECTED) {
			roster = Saros.getDefault().getRoster();
			attachRosterListener();

		} else if (newState == ConnectionState.NOT_CONNECTED) {
			roster = null;
		}

		refreshRosterTree(true);

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				updateStatusLine(newState);
				updateEnablement();
			}
		});
	}

	/**
	 * Needs to called from an UI thread.
	 */
	private void updateEnablement() {
		viewer.getControl().setEnabled(Saros.getDefault().isConnected());
	}

	/**
	 * Needs to called from an UI thread.
	 */
	private void updateStatusLine(final ConnectionState newState) {
		IStatusLineManager statusLine = getViewSite().getActionBars().getStatusLineManager();
		statusLine.setMessage(SarosUI.getDescription(newState));
	}

	private void attachRosterListener() {
		roster.addRosterListener(new RosterListener() {
			public void entriesAdded(Collection addresses) {
				refreshRosterTree(true);
			}

			public void entriesUpdated(Collection addresses) {
				refreshRosterTree(false);
			}

			public void entriesDeleted(Collection addresses) {
				refreshRosterTree(false);
			}

			public void presenceChanged(String XMPPAddress) {
				refreshRosterTree(true);
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
	private void refreshRosterTree(final boolean updateLabels) {
		if (viewer.getControl().isDisposed())
			return;
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				viewer.refresh(updateLabels);
				viewer.expandAll();
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

		Menu menu = menuMgr.createContextMenu(viewer.getControl());

		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (messagingAction.isEnabled()) {
					messagingAction.run();
				}
			}
		});
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();

		IMenuManager menuManager = bars.getMenuManager();
		menuManager.add(messagingAction);
		menuManager.add(inviteAction);
		// menuManager.add(new TestJoinWizardAction());
		menuManager.add(new Separator());

		IToolBarManager toolBarManager = bars.getToolBarManager();
		toolBarManager.add(new ConnectDisconnectAction());
		toolBarManager.add(new NewContactAction());
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(messagingAction);
		manager.add(skypeAction);
		manager.add(inviteAction);
		manager.add(new Separator());
		manager.add(renameContactAction);
		manager.add(deleteContactAction);

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void makeActions() {
		messagingAction = new MessagingAction(viewer);
		skypeAction = new SkypeAction(viewer);
		inviteAction = new InviteAction(viewer);
		renameContactAction = new RenameContactAction(viewer);
		deleteContactAction = new DeleteContactAction(viewer);
	}
}