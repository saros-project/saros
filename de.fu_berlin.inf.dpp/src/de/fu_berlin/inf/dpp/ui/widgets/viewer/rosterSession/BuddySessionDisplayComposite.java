package de.fu_berlin.inf.dpp.ui.widgets.viewer.rosterSession;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.editors.text.EditorsUI;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.internal.FollowingActivitiesManager;
import de.fu_berlin.inf.dpp.project.internal.IFollowModeChangesListener;
import de.fu_berlin.inf.dpp.project.internal.SarosSession;
import de.fu_berlin.inf.dpp.ui.model.TreeLabelProvider;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterEntryElement;
import de.fu_berlin.inf.dpp.ui.model.rosterSession.RosterSessionComparator;
import de.fu_berlin.inf.dpp.ui.model.rosterSession.RosterSessionContentProvider;
import de.fu_berlin.inf.dpp.ui.model.rosterSession.RosterSessionInput;
import de.fu_berlin.inf.dpp.ui.model.rosterSession.UserElement;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.ViewerComposite;
import de.fu_berlin.inf.nebula.utils.LayoutUtils;
import de.fu_berlin.inf.nebula.utils.PaintUtils;
import de.fu_berlin.inf.nebula.utils.ViewerUtils;

/**
 * This {@link Composite} displays the {@link SarosSession} and the
 * {@link Roster} in parallel.
 * <p>
 * This composite does <strong>NOT</strong> handle setting the layout.
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>NONE and those supported by {@link StructuredViewer}</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * 
 * @author bkahlert
 * 
 */
public class BuddySessionDisplayComposite extends ViewerComposite<TreeViewer> {

    @Inject
    private SarosNet sarosNet;

    @Inject
    private ISarosSessionManager sarosSessionManager;

    @Inject
    private SarosSessionObservable sarosSessionObservable;

    @Inject
    private EditorManager editorManager;

    @Inject
    private FollowingActivitiesManager followingActivitiesManager;

    private volatile ISarosSession currentSession;

    private IFollowModeChangesListener followModeChangesListener = new IFollowModeChangesListener() {

        @Override
        public void followModeChanged() {
            ViewerUtils.refresh(getViewer(), true);
            ViewerUtils.expandAll(getViewer());
        }
    };

    private IConnectionListener connectionListener = new IConnectionListener() {
        @Override
        public void connectionStateChanged(Connection connection,
            ConnectionState newState) {
            switch (newState) {
            case CONNECTED:
                updateViewer();
                ViewerUtils.expandAll(getViewer());
                break;
            case NOT_CONNECTED:
                updateViewer();
                ViewerUtils.expandAll(getViewer());
                break;
            default:
                break;
            }
        }
    };

    private ISharedProjectListener projectListener = new AbstractSharedProjectListener() {
        @Override
        public void userJoined(User user) {
            ViewerUtils.refresh(getViewer(), true);
            ViewerUtils.expandAll(getViewer());
        }

        @Override
        public void userLeft(User user) {
            ViewerUtils.refresh(getViewer(), true);
        }
    };

    private ISarosSessionListener sarosSessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionStarting(ISarosSession newSarosSession) {
            currentSession = newSarosSession;
            newSarosSession.addListener(projectListener);
            updateViewer();
            ViewerUtils.expandAll(getViewer());
        }

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            ViewerUtils.refresh(getViewer(), true);
        }

        @Override
        public void sessionEnding(ISarosSession oldSarosSession) {
            currentSession = null;
            oldSarosSession.removeListener(projectListener);
            ViewerUtils.refresh(getViewer(), true);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            updateViewer();
            ViewerUtils.expandAll(getViewer());
        }

        @Override
        public void projectAdded(String projectID) {
            ViewerUtils.refresh(getViewer(), true);
        }

        @Override
        public void preIncomingInvitationCompleted(IProgressMonitor monitor) {
            ViewerUtils.refresh(getViewer(), true);
            ViewerUtils.expandAll(getViewer());
        }

        @Override
        public void postOutgoingInvitationCompleted(IProgressMonitor monitor,
            User user) {
            ViewerUtils.refresh(getViewer(), true);
            ViewerUtils.expandAll(getViewer());
        }
    };

    private IPropertyChangeListener editorPrefsListener = new IPropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            ViewerUtils.refresh(getViewer(), true);
        }
    };

    /**
     * Used to display the {@link Roster} even in case the user is disconnected.
     */
    private Roster cachedRoster;

    public BuddySessionDisplayComposite(Composite parent, int style) {
        super(parent, style);

        SarosPluginContext.initComponent(this);

        super.setLayout(LayoutUtils.createGridLayout());

        getViewer().getControl()
            .setLayoutData(LayoutUtils.createFillGridData());

        updateViewer();
        ViewerUtils.expandAll(getViewer());

        sarosNet.addListener(connectionListener);
        this.sarosSessionManager.addSarosSessionListener(sarosSessionListener);

        ISarosSession session = sarosSessionManager.getSarosSession();

        /*
         * TODO: very rare race condition that will not GC the current session
         * until a new session is created or this widget is disposed.
         * 
         * E.G listener calls us that the session had ended and now we are
         * installing a listener to a dead session. This behavior can only
         * happen if the Saros View is destroyed and recreated.
         */

        if (session != null) {
            session.addListener(projectListener);
            currentSession = session;
        }

        this.followingActivitiesManager
            .addIinternalListener(followModeChangesListener);

        EditorsUI.getPreferenceStore().addPropertyChangeListener(
            editorPrefsListener);

        this.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {

                if (EditorsUI.getPreferenceStore() != null) {
                    EditorsUI.getPreferenceStore()
                        .removePropertyChangeListener(editorPrefsListener);
                }
                if (sarosSessionManager != null) {
                    sarosSessionManager
                        .removeSarosSessionListener(sarosSessionListener);
                }
                if (sarosNet != null) {
                    sarosNet.removeListener(connectionListener);
                }

                ISarosSession session = currentSession;
                if (session != null)
                    session.removeListener(projectListener);
            }
        });

        /*
         * Double click on a session participant in Saros view jumps to position
         * of clicked user.
         */
        final Control control = getViewer().getControl();
        control.addMouseListener(new MouseAdapter() {

            /**
             * Tries to find a tree item at the given click coordinates. If it
             * doesn't find anything, it continuously tries to find a target by
             * shifting the X coordinate.
             * 
             * @param event
             * @return
             */
            protected TreeItem findTreeItemNear(MouseEvent event) {
                TreeItem treeItem = ((Tree) control).getItem(new Point(event.x,
                    event.y));
                /*
                 * Background: the items are only targetable at their
                 * text-labels and icons. In the session view, the tree items
                 * get a rectangle with background color that expands beyond the
                 * text label. Users think that they can interact with the
                 * element by clicking anywhere on the background color, but
                 * actually miss the element.
                 */
                int x = event.x;
                while (treeItem == null && x > 0) {
                    x -= 5; // try 5 px to the left...
                    treeItem = ((Tree) control).getItem(new Point(x, event.y));
                }
                return treeItem;
            }

            /**
             * Toggle follow user when doubleclicked on UserElement in the
             * session tree.
             * 
             * Jump to User file+position when doubleclicked on
             * AwarenessTreeItem.
             * 
             */
            @Override
            public void mouseDoubleClick(MouseEvent event) {

                if (!(control instanceof Tree))
                    return;

                TreeItem treeItem = findTreeItemNear(event);

                if (treeItem == null)
                    return;

                User user = (User) Platform.getAdapterManager().getAdapter(
                    treeItem.getData(), User.class);

                if (user == null || user.isLocal())
                    return;

                /*
                 * toggle follow mode when doubleclicked on user element in
                 * session tree
                 */
                if (treeItem.getData() instanceof UserElement) {
                    User followedUser = editorManager.getFollowedUser();
                    editorManager.setFollowing(user.equals(followedUser) ? null
                        : user);
                } else {
                    /*
                     * jump to editor position of the user if doubleclicked on
                     * AwarenessTreeInformationElement
                     */
                    editorManager.jumpToUser(user);
                }
            }
        });

    }

    @Override
    protected TreeViewer createViewer(int style) {
        return new TreeViewer(new Tree(this, style));
    }

    @Override
    protected void configureViewer(TreeViewer viewer) {
        viewer.setContentProvider(new RosterSessionContentProvider());
        viewer.setLabelProvider(new TreeLabelProvider());
        viewer.setComparator(new RosterSessionComparator());
        viewer.addFilter(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement,
                Object element) {
                /*
                 * Don't show contacts in the contact list that are part of the
                 * session
                 */
                if (element instanceof RosterEntryElement) {
                    RosterEntryElement entry = (RosterEntryElement) element;
                    // Don't filter out the groups
                    if (entry.getChildren().length != 0) {
                        return true;
                    }
                    ISarosSession session = sarosSessionObservable.getValue();
                    if (session != null) {
                        JID resJID = session.getResourceQualifiedJID(entry
                            .getJID());
                        if (resJID != null && session.getUser(resJID) != null) {
                            return false;
                        }
                    }
                }
                return true;
            }
        });

        viewer.setUseHashlookup(true);

        /*
         * Draw a rounded rectangle indicating the highlighting color that is
         * used for this participant in the current session
         */
        viewer.getTree().addListener(SWT.PaintItem, new Listener() {
            @Override
            public void handleEvent(Event event) {

                if (event.item == null
                    || !(event.item.getData() instanceof UserElement))
                    return;

                User user = (User) Platform.getAdapterManager().getAdapter(
                    event.item.getData(), User.class);

                if (user == null)
                    return;

                GC gc = event.gc;

                Rectangle bounds = ((TreeItem) event.item)
                    .getBounds(event.index);

                bounds.width = 15;
                bounds.x += 15;

                Color backgroundColor = SarosAnnotation.getUserColor(user);

                PaintUtils.drawRoundedRectangle(gc, bounds, backgroundColor);

                backgroundColor.dispose();
            }
        });
    }

    private void updateViewer() {
        if (sarosNet.getRoster() != null)
            cachedRoster = sarosNet.getRoster();
        ViewerUtils.setInput(getViewer(), new RosterSessionInput(cachedRoster,
            sarosSessionManager.getSarosSession()));
    }

    @Override
    public void setLayout(Layout layout) {
        // ignore
    }
}
