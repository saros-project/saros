package de.fu_berlin.inf.dpp.ui.sarosView;

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
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.IRosterListener;
import de.fu_berlin.inf.dpp.net.RosterAdapter;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.net.internal.ConnectionTestManager;
import de.fu_berlin.inf.dpp.net.internal.discoveryManager.DiscoveryManager;
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
import de.fu_berlin.inf.dpp.ui.actions.RestrictInviteesToReadOnlyAccessAction;
import de.fu_berlin.inf.dpp.ui.actions.RestrictToReadOnlyAccessAction;
import de.fu_berlin.inf.dpp.ui.actions.SendFileAction;
import de.fu_berlin.inf.dpp.ui.actions.SkypeAction;
import de.fu_berlin.inf.dpp.ui.actions.StoppedAction;
import de.fu_berlin.inf.dpp.ui.actions.VideoSharingAction;
import de.fu_berlin.inf.dpp.ui.actions.VoIPAction;
import de.fu_berlin.inf.dpp.ui.sounds.SoundManager;
import de.fu_berlin.inf.dpp.ui.sounds.SoundPlayer;
import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.widgets.ConnectionStateComposite;
import de.fu_berlin.inf.dpp.ui.widgets.session.ChatRoomsComposite;
import de.fu_berlin.inf.dpp.ui.widgets.session.RosterSessionComposite;
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

    public static final String ID = "de.fu_berlin.inf.dpp.ui.sarosView.SarosView";

    protected Composite leftComposite;
    protected RosterSessionComposite rosterSessionComposite;

    /*
     * Actions
     */

    protected RenameContactAction renameContactAction;
    protected DeleteContactAction deleteContactAction;
    protected ConnectionTestAction connectionTestAction;

    @Inject
    protected Saros saros;

    @Inject
    protected SarosSessionManager sessionManager;

    @Inject
    protected ConnectionTestManager connectionTestManager;

    @Inject
    protected RosterTracker rosterTracker;

    @Inject
    protected DiscoveryManager discoveryManager;

    @Inject
    protected ChildContainer container;

    /**
     * Listeners
     */

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

    protected final IConnectionListener connectionListener = new IConnectionListener() {

        public void connectionStateChanged(XMPPConnection connection,
            final ConnectionState newState) {

            Utils.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    updateRenameContactActionEnablement();
                    updateDeleteContactActionEnablement();
                    updateTestActionEnablement();
                }
            });
        }

    };

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

        ConnectionStateComposite connectionStateComposite = new ConnectionStateComposite(
            leftComposite, SWT.NONE);
        connectionStateComposite.setLayoutData(LayoutUtils
            .createFillHGrabGridData());

        final ScrolledComposite scrolledComposite = new ScrolledComposite(
            leftComposite, SWT.H_SCROLL | SWT.V_SCROLL);
        scrolledComposite.setLayoutData(LayoutUtils.createFillGridData());

        this.rosterSessionComposite = new RosterSessionComposite(
            scrolledComposite, SWT.NONE);

        scrolledComposite.setContent(rosterSessionComposite);
        scrolledComposite.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event event) {
                Rectangle clientArea = scrolledComposite.getClientArea();
                int availableWidth = clientArea.width;

                scrolledComposite.setMinHeight(rosterSessionComposite
                    .computeSize(availableWidth, SWT.DEFAULT).y);
            }
        });

        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);

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

        container.addComponent(NewContactAction.class);
        container.addComponent(ChangeXMPPAccountAction.class);
        container.addComponent(IMBeepAction.class);
        container.addComponent(VideoSharingAction.class);
        container.addComponent(SendFileAction.class);
        container.addComponent(VoIPAction.class);
        container.addComponent(StoppedAction.class);
        container.addComponent(ChangeColorAction.class);
        container.addComponent(ConsistencyAction.class);
        container.addComponent(GiveWriteAccessAction.class);
        container.addComponent(FollowModeAction.class);
        container.addComponent(FollowThisPersonAction.class);
        container.addComponent(JumpToUserWithWriteAccessPositionAction.class);
        container.addComponent(LeaveSessionAction.class);
        container.addComponent(SessionViewTableViewer.class,
            rosterSessionComposite.getSessionViewer());
        container.addComponent(RestrictInviteesToReadOnlyAccessAction.class);
        container.addComponent(RestrictToReadOnlyAccessAction.class);
        container.addComponent(SessionContextMenu.class);
        container.addComponent(SarosViewToolbar.class);
        container.addComponent(SarosView.class, this);

        // Make sure all components are registered
        container.getComponents(Object.class);

        scrolledComposite.setMinSize(rosterSessionComposite.computeSize(
            SWT.DEFAULT, SWT.DEFAULT));

        /*
         * Create Roster Actions
         */
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        this.renameContactAction = new RenameContactAction(saros);
        this.deleteContactAction = new DeleteContactAction(sessionManager,
            saros);
        this.connectionTestAction = new ConnectionTestAction(saros,
            connectionTestManager);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(final IMenuManager manager) {

                manager.add(new SkypeAction());
                manager.add(new Separator());
                manager.add(SarosView.this.renameContactAction);
                manager.add(SarosView.this.deleteContactAction);
                manager.add(SarosView.this.connectionTestAction);
                manager.add(new Separator(
                    IWorkbenchActionConstants.MB_ADDITIONS));

                updateRenameContactActionEnablement();
                updateDeleteContactActionEnablement();
                updateTestActionEnablement();
            }
        });

        Viewer buddyViewer = this.rosterSessionComposite.getBuddyViewer();
        final Menu menu = menuMgr.createContextMenu(buddyViewer.getControl());

        buddyViewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, buddyViewer);
        getSite().setSelectionProvider(buddyViewer);

        saros.addListener(connectionListener);

        rosterTracker.addRosterListener(rosterListenerBuddys);

        connectionListener.connectionStateChanged(saros.getConnection(),
            saros.getConnectionState());

        rosterListenerBuddys.rosterChanged(saros.getRoster());

    }

    @Override
    public void dispose() {
        super.dispose();

        rosterTracker.removeRosterListener(rosterListenerBuddys);
        saros.removeListener(connectionListener);

        /*
         * Stop container and remove it from its parent.
         */
        container.dispose();
        /*
         * Unfortunately, child.getParent is immutable, so we have to ask Saros.
         */
        saros.removeChildContainer(container.getDelegate());

    }

    /**
     * @swt Needs to called from an UI thread.
     */
    protected void updateRenameContactActionEnablement() {
        boolean connected = saros.isConnected();

        this.renameContactAction.setEnabled(connected);
    }

    /**
     * @swt Needs to called from an UI thread.
     */
    protected void updateDeleteContactActionEnablement() {
        boolean connected = saros.isConnected();

        this.deleteContactAction.setEnabled(connected);
    }

    /**
     * @swt Needs to called from an UI thread.
     */
    protected void updateTestActionEnablement() {
        boolean connected = saros.isConnected();

        this.connectionTestAction.setEnabled(connected);

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
                BalloonNotification
                    .showNotification(control, title, text, 5000);
            }
        });
    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }
}