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
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.part.ViewPart;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.PreferenceConstants;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.actions.ConsistencyAction;
import de.fu_berlin.inf.dpp.ui.actions.FollowModeAction;
import de.fu_berlin.inf.dpp.ui.actions.GiveDriverRoleAction;
import de.fu_berlin.inf.dpp.ui.actions.GiveExclusiveDriverRoleAction;
import de.fu_berlin.inf.dpp.ui.actions.JumpToDriverPositionAction;
import de.fu_berlin.inf.dpp.ui.actions.LeaveSessionAction;
import de.fu_berlin.inf.dpp.ui.actions.OpenInviteInterface;
import de.fu_berlin.inf.dpp.ui.actions.RemoveAllDriverRoleAction;
import de.fu_berlin.inf.dpp.ui.actions.RemoveDriverRoleAction;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * View responsible for showing who is in a SharedProject session using which
 * color, who is being followed, and provide actions for changing roles and
 * follow mode.
 */
public class SessionView extends ViewPart {

    private static final Logger log = Logger.getLogger(SessionView.class
        .getName());

    protected TableViewer viewer;

    protected ISharedProject sharedProject;

    protected GiveDriverRoleAction giveDriverRoleAction;

    protected GiveExclusiveDriverRoleAction giveExclusiveDriverRoleAction;

    protected RemoveDriverRoleAction removeDriverRoleAction;

    protected RemoveAllDriverRoleAction removeAllDriverRoleAction;

    protected FollowModeAction followModeAction;

    protected ConsistencyAction consistencyAction;

    protected LeaveSessionAction leaveSessionAction;

    protected OpenInviteInterface openInvitationInterfaceAction;

    protected class SessionContentProvider implements
        IStructuredContentProvider, ISharedProjectListener {

        private TableViewer tableViewer;

        public void inputChanged(Viewer v, Object oldInput, Object newInput) {

            if (oldInput != null) {
                ISharedProject oldProject = (ISharedProject) oldInput;
                oldProject.removeListener(this);
            }

            SessionView.this.sharedProject = (ISharedProject) newInput;

            if (SessionView.this.sharedProject != null) {
                SessionView.this.sharedProject.addListener(this);
            }

            this.tableViewer = (TableViewer) v;
            this.tableViewer.refresh();

            updateEnablement();
        }

        public Object[] getElements(Object parent) {
            if (SessionView.this.sharedProject != null) {
                return SessionView.this.sharedProject.getParticipants()
                    .toArray();
            }

            return new Object[] {};
        }

        public void roleChanged(User user, boolean replicated) {
            // Show balloon notification
            if (user.equals(saros.getLocalUser())) {
                if (user.isDriver()) {
                    BalloonNotification.showNotification(
                        tableViewer.getTable(), "Role changed",
                        "You are now a driver of this session.", 5000);
                } else {
                    BalloonNotification.showNotification(
                        tableViewer.getTable(), "Role changed",
                        "You are now an observer of this session.", 5000);
                }
            }
            refreshTable();

        }

        public void userJoined(JID user) {
            refreshTable();
        }

        public void userLeft(JID user) {
            refreshTable();
        }

        public void dispose() {
            // Do nothing
        }

        private void refreshTable() {
            Util.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    SessionContentProvider.this.tableViewer.refresh();
                }
            });
        }
    }

    protected class SessionLabelProvider extends LabelProvider implements
        ITableLabelProvider, ITableColorProvider, ITableFontProvider {

        private final Image userImage = SarosUI.getImage("icons/user.png");
        private final Image driverImage = SarosUI
            .getImage("icons/user_edit.png");
        private Font boldFont = null;

        public String getColumnText(Object obj, int index) {

            User participant = (User) obj;

            return getName(participant)
                + (participant.isDriver() ? " (Driver)" : "");
        }

        @Override
        public Image getImage(Object obj) {
            return ((User) obj).isDriver() ? this.driverImage : this.userImage;
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

    public String getName(User participant) {

        if (participant.equals(saros.getLocalUser())) {
            return "You";
        }

        String nickName = Util.getNickname(participant.getJID());
        String jidBase = participant.getJID().getBase();

        if (nickName != null && nickName.trim().length() > 0) {
            return nickName + " (" + jidBase + ")";
        }

        return jidBase;
    }

    protected IPropertyChangeListener editorPrefsListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            viewer.refresh();
        }
    };

    protected IPropertyChangeListener multiDriverPrefsListener = new IPropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent event) {
            if (event.getProperty().equals(PreferenceConstants.MULTI_DRIVER)) {
                updateMultiDriverActions();
            }
        }

        private void updateMultiDriverActions() {
            IViewSite site = getViewSite();

            // Check if the site exists (may be null when disposed or starting)
            if (site == null)
                return;

            IActionBars bars = site.getActionBars();
            IToolBarManager toolBar = bars.getToolBarManager();

            if (isMultiDriverEnabled()) {
                toolBar.insertBefore(FollowModeAction.ACTION_ID,
                    removeAllDriverRoleAction);
            } else {
                toolBar.remove(RemoveAllDriverRoleAction.ACTION_ID);
            }
            toolBar.update(false);
        }
    };

    private JumpToDriverPositionAction jumpToAction;

    private List<Disposable> disposables = new ArrayList<Disposable>();

    private ISharedEditorListener sharedEditorListener = new AbstractSharedEditorListener() {
        @Override
        public void followModeChanged(User user) {
            viewer.refresh();
        }
    };

    @Inject
    protected Saros saros;

    @Inject
    protected EditorManager editorManager;

    @Inject
    protected SessionManager sessionManager;

    public SessionView() {

        Saros.getDefault().reinject(this);

        /**
         * Register with the Editors preference store, for getting notified when
         * color settings change.
         */
        EditorsUI.getPreferenceStore().addPropertyChangeListener(
            editorPrefsListener);

        /**
         * Register for our preference store, so we can be notified if the
         * Multi-Driver setting changes
         */
        saros.getPreferenceStore().addPropertyChangeListener(
            multiDriverPrefsListener);

        /**
         * Listener responsible for refreshing the viewer if the follow mode
         * changed (because the followed user is shown in bold)
         */
        editorManager.addSharedEditorListener(sharedEditorListener);

    }

    @Override
    public void dispose() {
        EditorsUI.getPreferenceStore().removePropertyChangeListener(
            editorPrefsListener);
        saros.getPreferenceStore().removePropertyChangeListener(
            multiDriverPrefsListener);
        editorManager.removeSharedEditorListener(sharedEditorListener);

        // FIXME All actions need to be properly disposed, because they use
        // Listeners
        for (Disposable toDispose : disposables) {
            toDispose.dispose();
        }

        sessionManager.removeSessionListener(sessionListener);

        super.dispose();
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    @Override
    public void createPartControl(Composite parent) {

        // TODO Add 5 pixels of padding
        this.viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
            | SWT.V_SCROLL);
        this.viewer.setContentProvider(new SessionContentProvider());

        final SessionLabelProvider labelProvider = new SessionLabelProvider();
        this.viewer.setLabelProvider(labelProvider);

        // Make sure one column we got fills the whole table
        final Table table = this.viewer.getTable();
        TableColumnLayout ad = new TableColumnLayout();
        table.getParent().setLayout(ad);
        TableColumn column = new TableColumn(table, SWT.NONE);
        ad.setColumnData(column, new ColumnWeightData(100));

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

        this.giveExclusiveDriverRoleAction = new GiveExclusiveDriverRoleAction(
            this.viewer, "Give exclusive driver role");
        this.followModeAction = new FollowModeAction();
        this.disposables.add(followModeAction);
        this.leaveSessionAction = new LeaveSessionAction();
        this.consistencyAction = new ConsistencyAction();
        this.openInvitationInterfaceAction = new OpenInviteInterface();
        this.removeAllDriverRoleAction = new RemoveAllDriverRoleAction();
        this.giveDriverRoleAction = new GiveDriverRoleAction(this.viewer,
            "Give driver role");
        this.jumpToAction = new JumpToDriverPositionAction(this.viewer);
        this.removeDriverRoleAction = new RemoveDriverRoleAction(this.viewer);

        contributeToActionBars();
        hookContextMenu();
        attachSessionListener();
        updateEnablement();

        setPartName("Shared Project Session");
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    @Override
    public void setFocus() {
        this.viewer.getControl().setFocus();
    }

    /**
     * Needs to be called from the UI thread.
     */
    private void updateEnablement() {
        this.viewer.getControl().setEnabled(this.sharedProject != null);
    }

    public final ISessionListener sessionListener = new AbstractSessionListener() {

        @Override
        public void sessionStarted(final ISharedProject project) {
            Util.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    SessionView.this.viewer.setInput(project);
                }
            });
        }

        @Override
        public void sessionEnded(ISharedProject project) {
            assert sharedProject == project;
            Util.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    SessionView.this.viewer.setInput(null);
                }
            });
            sharedProject = null;
        }
    };

    private void attachSessionListener() {
        sessionManager.addSessionListener(sessionListener);
        if (sessionManager.getSharedProject() != null) {
            this.viewer.setInput(sessionManager.getSharedProject());
        }
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager toolBar = bars.getToolBarManager();

        toolBar.add(consistencyAction);
        toolBar.add(openInvitationInterfaceAction);
        if (isMultiDriverEnabled()) {
            toolBar.add(removeAllDriverRoleAction);
        }
        toolBar.add(followModeAction);
        toolBar.add(leaveSessionAction);
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                fillContextMenu(manager);
            }
        });

        Menu menu = menuMgr.createContextMenu(this.viewer.getControl());
        this.viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, this.viewer);

        this.viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                if (jumpToAction.isEnabled()) {
                    jumpToAction.run();
                }
            }
        });

    }

    private void fillContextMenu(IMenuManager manager) {

        manager.add(this.giveExclusiveDriverRoleAction);

        if (isMultiDriverEnabled()) {
            manager.add(this.giveDriverRoleAction);
            manager.add(this.removeDriverRoleAction);
        }
        manager.add(this.jumpToAction);

        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    protected boolean isMultiDriverEnabled() {
        return saros.getPreferenceStore().getBoolean(
            PreferenceConstants.MULTI_DRIVER);
    }

    public void runConsistencyActionIfEnabled() {
        if (this.consistencyAction.isEnabled())
            consistencyAction.run();
    }
}
