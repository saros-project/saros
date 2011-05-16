package de.fu_berlin.inf.dpp.ui.views;

/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2010
 * (c) Patrick Bitterling - 2010
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.IRosterListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.RosterAdapter;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.BalloonNotification;
import de.fu_berlin.inf.dpp.ui.actions.ChangeColorAction;
import de.fu_berlin.inf.dpp.ui.actions.ChangeXMPPAccountAction;
import de.fu_berlin.inf.dpp.ui.actions.ConnectionTestAction;
import de.fu_berlin.inf.dpp.ui.actions.ConsistencyAction;
import de.fu_berlin.inf.dpp.ui.actions.DeleteContactAction;
import de.fu_berlin.inf.dpp.ui.actions.FollowModeAction;
import de.fu_berlin.inf.dpp.ui.actions.FollowThisPersonAction;
import de.fu_berlin.inf.dpp.ui.actions.GiveWriteAccessAction;
import de.fu_berlin.inf.dpp.ui.actions.IMBeepAction;
import de.fu_berlin.inf.dpp.ui.actions.JumpToUserWithWriteAccessPositionAction;
import de.fu_berlin.inf.dpp.ui.actions.LeaveSessionAction;
import de.fu_berlin.inf.dpp.ui.actions.NewContactAction;
import de.fu_berlin.inf.dpp.ui.actions.RenameContactAction;
import de.fu_berlin.inf.dpp.ui.actions.RestrictToReadOnlyAccessAction;
import de.fu_berlin.inf.dpp.ui.actions.SendFileAction;
import de.fu_berlin.inf.dpp.ui.actions.SkypeAction;
import de.fu_berlin.inf.dpp.ui.actions.StoppedAction;
import de.fu_berlin.inf.dpp.ui.actions.VideoSharingAction;
import de.fu_berlin.inf.dpp.ui.actions.VoIPAction;
import de.fu_berlin.inf.dpp.ui.sounds.SoundManager;
import de.fu_berlin.inf.dpp.ui.sounds.SoundPlayer;
import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.ui.widgets.ConnectionStateComposite;
import de.fu_berlin.inf.dpp.ui.widgets.session.ChatRoomsComposite;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.rosterSession.BuddySessionDisplayComposite;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.pico.ChildContainer;

/**
 * This view displays the contact list, the Saros Session and Saros Chat.
 * 
 * @author patbit
 */
@Component(module = "ui")
public class SarosView extends ViewPart {

    private static final Logger log = Logger.getLogger(SarosView.class);

    public static final String ID = "de.fu_berlin.inf.dpp.ui.views.SarosView";

    protected IRosterListener rosterListenerBuddys = new RosterAdapter() {
        /**
         * Stores the most recent presence for each user, so we can keep track
         * of away/available changes which should not update the RosterView.
         */
        protected Map<String, Presence> lastPresenceMap = new HashMap<String, Presence>();

        @Override
        public void presenceChanged(Presence presence) {

            Presence lastPresence = lastPresenceMap.put(presence.getFrom(),
                presence);

            if ((lastPresence == null || !lastPresence.isAvailable())
                && presence.isAvailable()) {
                SoundPlayer.playSound(SoundManager.USER_ONLINE);
            }

            if ((lastPresence != null) && lastPresence.isAvailable()
                && !presence.isAvailable()) {
                SoundPlayer.playSound(SoundManager.USER_OFFLINE);
            }
        }
    };

    protected IPartListener2 partListener = new IPartListener2() {
        public void partInputChanged(IWorkbenchPartReference partRef) {
            // do nothing
        }

        public void partVisible(IWorkbenchPartReference partRef) {
            // do nothing
        }

        public void partHidden(IWorkbenchPartReference partRef) {
            // do nothing
        }

        public void partOpened(IWorkbenchPartReference partRef) {
            // do nothing
        }

        public void partDeactivated(IWorkbenchPartReference partRef) {
            if (buddySessionDisplayComposite != null
                && !buddySessionDisplayComposite.isDisposed()) {
                buddySessionDisplayComposite.getViewer().setSelection(
                    new ISelection() {
                        public boolean isEmpty() {
                            return true;
                        }
                    });
            }
        }

        public void partClosed(IWorkbenchPartReference partRef) {
            getViewSite().getPage().removePartListener(partListener);
        }

        public void partBroughtToTop(IWorkbenchPartReference partRef) {
            // do nothing
        }

        public void partActivated(IWorkbenchPartReference partRef) {
            // do nothing
        }
    };

    protected Composite leftComposite;
    protected BuddySessionDisplayComposite buddySessionDisplayComposite;

    @Inject
    protected Saros saros;

    @Inject
    protected SarosSessionManager sarosSessionManager;

    @Inject
    protected EditorManager editorManager;

    @Inject
    protected RosterTracker rosterTracker;

    @Inject
    protected ChildContainer container;

    public SarosView() {
        super();
        SarosPluginContext.initComponent(this);
    }

    @Override
    public void createPartControl(Composite parent) {

        parent.setLayout(new FillLayout());

        SashForm baseSashForm = new SashForm(parent, SWT.SMOOTH);

        /*
         * LEFT COLUMN
         */
        leftComposite = new Composite(baseSashForm, SWT.BORDER);
        leftComposite.setLayout(LayoutUtils.createGridLayout());
        leftComposite.setBackground(Display.getCurrent().getSystemColor(
            SWT.COLOR_WHITE));

        ConnectionStateComposite connectionStateComposite = new ConnectionStateComposite(
            leftComposite, SWT.NONE);
        connectionStateComposite.setLayoutData(LayoutUtils
            .createFillHGrabGridData());

        buddySessionDisplayComposite = new BuddySessionDisplayComposite(
            leftComposite, SWT.V_SCROLL);
        buddySessionDisplayComposite.setLayoutData(LayoutUtils
            .createFillGridData());

        /*
         * Double click on buddy in Saros view jumps to position of clicked
         * user.
         */
        final Control control = buddySessionDisplayComposite.getViewer()
            .getControl();
        control.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent event) {
                if (control instanceof Tree) {
                    TreeItem treeItem = ((Tree) control).getItem(new Point(
                        event.x, event.y));
                    if (treeItem != null) {
                        User user = (User) Platform.getAdapterManager()
                            .getAdapter(treeItem.getData(), User.class);
                        if (user != null)
                            editorManager.jumpToUser(user);
                    }
                } else {
                    log.warn("Control is not instance of Tree.");
                }
            }
        });

        /*
         * RIGHT COLUMN
         */
        Composite rightComposite = new Composite(baseSashForm, SWT.NONE);
        rightComposite.setLayout(new FillLayout());

        new ChatRoomsComposite(rightComposite, SWT.NONE);

        /*
         * contributeToActionBars: TODO the creation of actions through the
         * PicoContainer is an old heritage. Actions (especially new ones)
         * should be implemented with commands and command handlers.
         */

        container.addComponent(SarosView.class, this);

        // Make sure all components are registered
        container.getComponents(Object.class);

        /*
         * Toolbar
         */
        IActionBars bars = this.getViewSite().getActionBars();
        IToolBarManager toolBar = bars.getToolBarManager();
        addToolBarItems(toolBar);

        /*
         * Context Menu
         */
        MenuManager menuManager = new MenuManager();
        menuManager.setRemoveAllWhenShown(true);
        addRosterMenuItems(menuManager);
        addSessionMenuItems(menuManager);
        addAdditionsSeparator(menuManager);

        Viewer buddySessionViewer = buddySessionDisplayComposite.getViewer();
        Menu menu = menuManager.createContextMenu(buddySessionViewer
            .getControl());
        buddySessionViewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuManager, buddySessionViewer);
        getSite().setSelectionProvider(buddySessionViewer);

        rosterTracker.addRosterListener(rosterListenerBuddys);
        rosterListenerBuddys.rosterChanged(saros.getRoster());

        getViewSite().getPage().addPartListener(partListener);
    }

    protected void addToolBarItems(IToolBarManager toolBar) {
        toolBar.add(new ChangeXMPPAccountAction());
        toolBar.add(new NewContactAction());
        toolBar.add(new Separator());
        toolBar.add(new StoppedAction());
        toolBar.add(new ConsistencyAction());
        toolBar.add(new FollowModeAction());
        toolBar.add(new IMBeepAction());
        toolBar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        toolBar.add(new LeaveSessionAction());
    }

    /**
     * @param menuManager
     */
    protected void addRosterMenuItems(MenuManager menuManager) {
        final SkypeAction skypeAction = new SkypeAction();
        final RenameContactAction renameContactAction = new RenameContactAction();
        final DeleteContactAction deleteContactAction = new DeleteContactAction();
        final ConnectionTestAction connectionTestAction = new ConnectionTestAction();
        menuManager.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(final IMenuManager manager) {
                /*
                 * Do not display the following actions if participants are
                 * selected.
                 */
                List<User> participants = SelectionRetrieverFactory
                    .getSelectionRetriever(User.class).getSelection();
                if (participants.size() > 0)
                    return;

                /*
                 * Do not display the following actions if no buddies are
                 * selected.
                 */
                List<JID> buddies = SelectionRetrieverFactory
                    .getSelectionRetriever(JID.class).getSelection();
                if (buddies.size() == 0)
                    return;

                manager.add(skypeAction);
                manager.add(new Separator());
                manager.add(renameContactAction);
                manager.add(deleteContactAction);
                manager.add(connectionTestAction);
            }
        });
    }

    /**
     * @param menuManager
     */
    protected void addSessionMenuItems(MenuManager menuManager) {
        final GiveWriteAccessAction giveWriteAccessAction = new GiveWriteAccessAction();
        final RestrictToReadOnlyAccessAction restrictToReadOnlyAccessAction = new RestrictToReadOnlyAccessAction();
        final FollowThisPersonAction followModeAction = new FollowThisPersonAction();
        final JumpToUserWithWriteAccessPositionAction jumpToUserWithWriteAccessPositionAction = new JumpToUserWithWriteAccessPositionAction();
        final SendFileAction sendFileAction = new SendFileAction();
        final VideoSharingAction videoSharingAction = new VideoSharingAction();
        final VoIPAction voipAction = new VoIPAction();
        final ChangeColorAction changedColourAction = new ChangeColorAction();
        menuManager.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                /*
                 * Do not display the following actions if no participants are
                 * selected.
                 */
                List<User> participants = SelectionRetrieverFactory
                    .getSelectionRetriever(User.class).getSelection();
                if (participants.size() == 0)
                    return;

                /*
                 * Do not display the following actions if non-participants are
                 * selected.
                 */
                List<JID> buddies = SelectionRetrieverFactory
                    .getSelectionRetriever(JID.class).getSelection();
                if (buddies.size() > 0)
                    return;

                if (participants.size() == 1) {
                    if (participants.get(0).isLocal()) {
                        manager.add(changedColourAction);
                    } else {
                        if (sarosSessionManager.getSarosSession() != null
                            && sarosSessionManager.getSarosSession().isHost()) {
                            manager.add(giveWriteAccessAction);
                            manager.add(restrictToReadOnlyAccessAction);
                            manager.add(new Separator());
                        }
                        manager.add(followModeAction);
                        manager.add(jumpToUserWithWriteAccessPositionAction);
                        manager.add(new Separator());
                        manager.add(sendFileAction);
                        manager.add(videoSharingAction);
                        manager.add(voipAction);
                    }
                }
            }
        });
    }

    /**
     * Adds the {@link IWorkbenchActionConstants#MB_ADDITIONS additions}
     * {@link Separator} to the {@link MenuManager} in order to let others
     * extend the menu.
     * 
     * @param menuManager
     */
    protected void addAdditionsSeparator(MenuManager menuManager) {
        menuManager.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                manager.add(new Separator(
                    IWorkbenchActionConstants.MB_ADDITIONS));
            }
        });
    }

    @Override
    public void dispose() {
        super.dispose();

        rosterTracker.removeRosterListener(rosterListenerBuddys);

        /*
         * Stop container and remove it from its parent.
         */
        container.dispose();
        /*
         * Unfortunately, child.getParent is immutable, so we have to ask Saros.
         */
        saros.removeChildContainer(container.getDelegate());
    }

    public static void showNotification(final String title, final String text) {
        Utils.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                IViewPart sarosView = Utils.findView(SarosView.ID);
                /*
                 * If no session view is open then show the balloon notification
                 * in the control which has the keyboard focus
                 */
                Control control;

                if (sarosView != null) {
                    control = ((SarosView) sarosView).leftComposite;
                } else {
                    control = Display.getDefault().getFocusControl();

                }
                BalloonNotification.showNotification(control, title, text,
                    15000);
            }
        });
    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }
}