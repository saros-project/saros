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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.editors.text.EditorsUI;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.net.IRosterListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.actions.ChangeColorAction;
import de.fu_berlin.inf.dpp.ui.actions.ConsistencyAction;
import de.fu_berlin.inf.dpp.ui.actions.FollowModeAction;
import de.fu_berlin.inf.dpp.ui.actions.FollowThisPersonAction;
import de.fu_berlin.inf.dpp.ui.actions.GiveWriteAccessAction;
import de.fu_berlin.inf.dpp.ui.actions.JumpToUserWithWriteAccessPositionAction;
import de.fu_berlin.inf.dpp.ui.actions.LeaveSessionAction;
import de.fu_berlin.inf.dpp.ui.actions.OpenInviteInterface;
import de.fu_berlin.inf.dpp.ui.actions.RestrictInviteesToReadOnlyAccessAction;
import de.fu_berlin.inf.dpp.ui.actions.RestrictToReadOnlyAccessAction;
import de.fu_berlin.inf.dpp.ui.actions.SendFileAction;
import de.fu_berlin.inf.dpp.ui.actions.StoppedAction;
import de.fu_berlin.inf.dpp.ui.actions.VideoSharingAction;
import de.fu_berlin.inf.dpp.ui.actions.VoIPAction;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.ListExplanationComposite.ListExplanation;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.explanatory.ListExplanatoryViewPart;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.pico.ChildContainer;

/**
 * View responsible for showing who is in a SarosSession session using which
 * color, who is being followed, and provide actions for changing
 * {@link User.Permission}s and follow mode.
 */
@Component(module = "ui")
public class SessionView extends ListExplanatoryViewPart {

    private static final Logger log = Logger.getLogger(SessionView.class
        .getName());

    public static final Image awayImage = ImageManager
        .getImage("icons/elcl16/away.png");
    public static final Image userImage = ImageManager
        .getImage("icons/obj16/user.png");
    public static final Image participantImage = ImageManager
        .getImage("icons/obj16/participant.png");
    public static final Image participantReadOnlyImage = ImageManager
        .getImage("icons/obj16/participant_readonly.png");

    protected IRosterListener rosterListener;

    protected ListExplanation howTo = new ListExplanation(
        SWT.ICON_INFORMATION,
        "To begin a Saros session you can either:",
        "Right-click on a project in the Package Explorer and select \"Share project\" in the Saros sub-menu.",
        "Sign into the Saros buddies, right-click on the invitee(s) and select \"Invite buddy\".");

    IPartListener2 viewPartListener = new IPartListener2() {
        public void partVisible(IWorkbenchPartReference partRef) {
            // Do nothing
        }

        public void partOpened(IWorkbenchPartReference partRef) {
            // Do nothing
        }

        public void partInputChanged(IWorkbenchPartReference partRef) {
            // Do nothing
        }

        public void partHidden(IWorkbenchPartReference partRef) {
            // Do nothing
        }

        public void partDeactivated(IWorkbenchPartReference partRef) {
            // Ensure the user in the session view is deselected when this
            // view loses focus.
            viewer.setSelection(new StructuredSelection());
        }

        public void partClosed(IWorkbenchPartReference partRef) {
            // Do nothing
        }

        public void partBroughtToTop(IWorkbenchPartReference partRef) {
            // Do nothing
        }

        public void partActivated(IWorkbenchPartReference partRef) {
            // Do nothing
        }
    };

    /**
     * This RosterListener is responsible to trigger updates to our table
     * viewer, whenever roster elements change.
     * 
     * This is mostly used to update the nickname of the user at the moment.
     */
    protected class SessionRosterListener implements IRosterListener {
        public void changed(final Collection<String> addresses) {
            Utils.runSafeSWTSync(log, new Runnable() {
                public void run() {
                    if (sarosSession == null)
                        return;

                    for (String address : addresses) {
                        JID jid = sarosSession.getResourceQualifiedJID(new JID(
                            address));
                        if (jid == null)
                            continue;

                        User user = sarosSession.getUser(jid);
                        if (user == null)
                            continue;

                        try {
                            viewer.refresh(user);
                        } catch (IllegalArgumentException e) {
                            // FIXME This looks like a bug, we shouldn't get an
                            // exception here.
                            log.error("viewer.refresh(" + user.toString() + ")");
                        }
                    }
                }
            });
        }

        public void presenceChanged(Presence presence) {
            changed(Collections.singleton(presence.getFrom()));
        }

        public void entriesUpdated(Collection<String> addresses) {
            changed(addresses);
        }

        public void entriesDeleted(Collection<String> addresses) {
            changed(addresses);
        }

        public void entriesAdded(Collection<String> addresses) {
            changed(addresses);
        }

        public void rosterChanged(Roster roster) {
            Utils.runSafeSWTSync(log, new Runnable() {
                public void run() {
                    viewer.refresh();
                }
            });
        }
    }

    @Component(module = "ui")
    public static class SessionViewTableViewer extends TableViewer {
        public SessionViewTableViewer(Composite parent, int style) {
            super(parent, style);
        }
    }

    protected TableViewer viewer;

    protected ISarosSession sarosSession;

    protected class SessionContentProvider implements
        IStructuredContentProvider, ISharedProjectListener {

        private TableViewer tableViewer;

        /**
         * Comparator for comparing users. The host has a lower rank than any
         * client (compare(host, client) = -1, compare(client, host) = 1.
         * Clients are compared alphabetically by JID (not case sensitive).
         */
        protected Comparator<User> alphabeticalUserComparator = new Comparator<User>() {
            public int compare(User user1, User user2) {
                if (user1.equals(user2))
                    return 0;
                if (user1.isHost())
                    return -1;
                if (user2.isHost())
                    return +1;
                return user1.getJID().toString().toLowerCase()
                    .compareTo(user2.getJID().toString().toLowerCase());
            }
        };

        public void inputChanged(Viewer v, Object oldInput, Object newInput) {

            if (oldInput != null) {
                ISarosSession oldSarosSession = (ISarosSession) oldInput;
                oldSarosSession.removeListener(this);
            }

            SessionView.this.sarosSession = (ISarosSession) newInput;

            if (SessionView.this.sarosSession != null) {
                SessionView.this.sarosSession.addListener(this);
            }

            this.tableViewer = (TableViewer) v;
            this.tableViewer.refresh();

            updateEnablement();
        }

        public Object[] getElements(Object parent) {
            if (sarosSession == null)
                return new Object[] {};

            User[] participants = sarosSession.getParticipants().toArray(
                new User[] {});
            Arrays.sort(participants, alphabeticalUserComparator);

            return participants;
        }

        public void permissionChanged(User user) {
            refreshTable();
        }

        public void invitationCompleted(User user) {
            refreshTable();
        }

        public void userJoined(User user) {
            refreshTable();
        }

        public void userLeft(User user) {
            refreshTable();
        }

        public void dispose() {
            // Do nothing
        }

        public void refreshTable() {
            Utils.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    tableViewer.refresh();
                }
            });
        }
    }

    protected class SessionLabelProvider extends LabelProvider implements
        ITableLabelProvider, ITableColorProvider, ITableFontProvider {

        private Font boldFont = null;

        public String getColumnText(Object obj, int index) {

            User participant = (User) obj;
            return participant.getHumanReadableName()
                + (participant.hasReadOnlyAccess() ? " (read-only)" : "")
                + (participant.isInvitationComplete() ? "" : " [Joining...]");
        }

        @Override
        public Image getImage(Object obj) {
            User user = (User) obj;
            if (user.isAway()) {
                return awayImage;
            } else {
                if (user.hasWriteAccess())
                    return participantImage;
                else if (user.hasReadOnlyAccess())
                    return participantReadOnlyImage;
            }

            return userImage;
        }

        public Image getColumnImage(Object obj, int index) {
            return getImage(obj);
        }

        // TODO getting current color does not work if default was changed.
        public Color getBackground(Object element, int columnIndex) {
            return SarosAnnotation.getUserColor((User) element);
        }

        public Color getForeground(Object element, int columnIndex) {
            return null;
        }

        public Font getFont(Object element, int columnIndex) {
            if (this.boldFont == null) {
                Display disp = SessionView.this.viewer.getControl()
                    .getDisplay();
                FontData[] data = disp.getSystemFont().getFontData();
                for (FontData fontData : data) {
                    fontData.setStyle(SWT.BOLD);
                }
                this.boldFont = new Font(disp, data);
            }

            User user = (User) element;
            if (user.equals(editorManager.getFollowedUser())) {
                return this.boldFont;
            }
            return null;
        }

        @Override
        public void dispose() {
            if (this.boldFont != null) {
                this.boldFont.dispose();
                this.boldFont = null;
            }

            super.dispose();
        }
    }

    protected IPropertyChangeListener editorPrefsListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            viewer.refresh();
        }
    };

    protected ISharedEditorListener sharedEditorListener = new AbstractSharedEditorListener() {
        @Override
        public void followModeChanged(User user) {
            viewer.refresh();
        }

        @Override
        public void colorChanged() {
            viewer.refresh();
        }
    };

    @Inject
    protected Saros saros;

    @Inject
    protected EditorManager editorManager;

    @Inject
    protected SarosSessionManager sessionManager;

    @Inject
    protected ChildContainer container;

    @Inject
    protected RosterTracker rosterTracker;

    public SessionView() {

        Saros.injectDependenciesOnly(this);

        /*
         * Register with the Editors preference store, for getting notified when
         * color settings change.
         */
        EditorsUI.getPreferenceStore().addPropertyChangeListener(
            editorPrefsListener);

        /*
         * Listener responsible for refreshing the viewer if the follow mode
         * changed (because the followed user is shown in bold)
         */
        editorManager.addSharedEditorListener(sharedEditorListener);

        /*
         * Make sure the Session View is informed about changes to the roster
         * entry of the user
         */
        rosterListener = new SessionRosterListener();
        rosterTracker.addRosterListener(rosterListener);
    }

    @Override
    public void dispose() {
        EditorsUI.getPreferenceStore().removePropertyChangeListener(
            editorPrefsListener);
        editorManager.removeSharedEditorListener(sharedEditorListener);
        sessionManager.removeSarosSessionListener(sessionListener);
        rosterTracker.removeRosterListener(rosterListener);

        // Stop container and remove it from its parent.
        container.dispose();
        // Unfortunately, child.getParent is immutable, so we have to ask Saros.
        saros.removeChildContainer(container.getDelegate());

        super.dispose();
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    @Override
    public void createContentPartControl(Composite parent) {
        this.showExplanation(true);

        // TODO Add 5 pixels of padding
        this.viewer = new SessionViewTableViewer(parent, SWT.MULTI
            | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        this.viewer.setContentProvider(new SessionContentProvider());

        final SessionLabelProvider labelProvider = new SessionLabelProvider();
        this.viewer.setLabelProvider(labelProvider);

        // Make sure one column we got fills the whole table
        final Table table = this.viewer.getTable();
        TableColumnLayout layout = new TableColumnLayout();
        table.getParent().setLayout(layout);
        TableColumn column = new TableColumn(table, SWT.NONE);
        layout.setColumnData(column, new ColumnWeightData(100));

        /**
         * Make sure that background color fills the whole row
         * 
         * Adapted from
         * http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.swt.snippets
         * /src/org/eclipse/swt/snippets/Snippet229.java?view=co
         */
        table.addListener(SWT.EraseItem, new Listener() {
            public void handleEvent(Event event) {

                GC gc = event.gc;
                Color background = gc.getBackground();
                gc.setBackground(labelProvider.getBackground(
                    ((TableItem) event.item).getData(), event.index));
                gc.fillRectangle(((TableItem) event.item)
                    .getBounds(event.index));
                // restore colors for subsequent drawing
                gc.setBackground(background);
            }
        });

        this.viewer.setInput(null);

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
        container.addComponent(OpenInviteInterface.class);
        container.addComponent(SessionViewTableViewer.class, this.viewer);
        container.addComponent(RestrictInviteesToReadOnlyAccessAction.class);
        container.addComponent(RestrictToReadOnlyAccessAction.class);
        container.addComponent(SessionViewContextMenu.class);
        container.addComponent(SessionViewToolBar.class);
        container.addComponent(SessionView.class, this);

        // Make sure all components are registered
        container.getComponents(Object.class);

        // Add Session Listener
        sessionManager.addSarosSessionListener(sessionListener);
        if (sessionManager.getSarosSession() != null) {
            this.viewer.setInput(sessionManager.getSarosSession());
            showExplanation(false);
        }

        updateEnablement();

        getViewSite().getPage().addPartListener(viewPartListener);
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    @Override
    public void setFocus() {
        this.viewer.getControl().setFocus();
    }

    private void showExplanation(boolean show) {
        if (show)
            this.showExplanation(this.howTo);
        else
            this.hideExplanation();
    }

    /**
     * Needs to be called from the UI thread.
     */
    private void updateEnablement() {
        this.viewer.getControl().setEnabled(this.sarosSession != null);
    }

    public final ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(final ISarosSession newSarosSession) {
            Utils.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    viewer.setInput(newSarosSession);
                    showExplanation(false);
                }
            });
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            assert sarosSession == oldSarosSession;
            Utils.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    showExplanation(true);
                    viewer.setInput(null);
                }
            });
            sarosSession = null;
        }
    };

    public static void showNotification(final String title, final String text) {
        Utils.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                IViewPart sessionView = Utils
                    .findView("de.fu_berlin.inf.dpp.ui.SessionView");
                /*
                 * If no session view is open then show the balloon notification
                 * in the control which has the keyboard focus
                 */
                Control control = (sessionView == null) ? Display.getDefault()
                    .getFocusControl() : ((SessionView) sessionView).viewer
                    .getTable();
                BalloonNotification
                    .showNotification(control, title, text, 5000);
            }
        });
    }
}
