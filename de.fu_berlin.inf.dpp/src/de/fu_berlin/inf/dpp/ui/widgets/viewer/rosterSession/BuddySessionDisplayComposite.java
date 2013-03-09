package de.fu_berlin.inf.dpp.ui.widgets.viewer.rosterSession;

import org.apache.log4j.Logger;
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

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
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
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
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
public class BuddySessionDisplayComposite extends ViewerComposite {
    private static final Logger log = Logger
        .getLogger(BuddySessionDisplayComposite.class);

    @Inject
    private Saros saros;

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
            ViewerUtils.refresh(viewer, true);
            ViewerUtils.expandAll(viewer);
        }
    };

    private IConnectionListener connectionListener = new IConnectionListener() {
        @Override
        public void connectionStateChanged(Connection connection,
            ConnectionState newState) {
            switch (newState) {
            case CONNECTED:
                updateViewer();
                ViewerUtils.expandAll(viewer);
                break;
            case NOT_CONNECTED:
                updateViewer();
                ViewerUtils.expandAll(viewer);
                break;
            default:
                break;
            }
        }
    };

    private ISharedProjectListener projectListener = new AbstractSharedProjectListener() {
        @Override
        public void userJoined(User user) {
            ViewerUtils.refresh(viewer, true);
            ViewerUtils.expandAll(viewer);
        }

        @Override
        public void userLeft(User user) {
            ViewerUtils.refresh(viewer, true);
        }
    };

    private ISarosSessionListener sarosSessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionStarting(ISarosSession newSarosSession) {
            currentSession = newSarosSession;
            newSarosSession.addListener(projectListener);
            updateViewer();
            ViewerUtils.expandAll(viewer);
        }

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            ViewerUtils.refresh(viewer, true);
        }

        @Override
        public void sessionEnding(ISarosSession oldSarosSession) {
            currentSession = null;
            oldSarosSession.removeListener(projectListener);
            ViewerUtils.refresh(viewer, true);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            updateViewer();
            ViewerUtils.expandAll(viewer);
        }

        @Override
        public void projectAdded(String projectID) {
            ViewerUtils.refresh(viewer, true);
        }

        @Override
        public void preIncomingInvitationCompleted(IProgressMonitor monitor) {
            ViewerUtils.refresh(viewer, true);
            ViewerUtils.expandAll(viewer);
        }

        @Override
        public void postOutgoingInvitationCompleted(IProgressMonitor monitor,
            User user) {
            ViewerUtils.refresh(viewer, true);
            ViewerUtils.expandAll(viewer);
        }
    };

    private IPropertyChangeListener editorPrefsListener = new IPropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            ViewerUtils.refresh(viewer, true);
        }
    };

    private Tree tree;

    /**
     * Used to display the {@link Roster} even in case the user is disconnected.
     */
    private Roster cachedRoster;

    public BuddySessionDisplayComposite(Composite parent, int style) {
        super(parent, style);

        SarosPluginContext.initComponent(this);

        super.setLayout(LayoutUtils.createGridLayout());
        this.viewer.getControl()
            .setLayoutData(LayoutUtils.createFillGridData());
        updateViewer();
        ViewerUtils.expandAll(viewer);

        saros.getSarosNet().addListener(connectionListener);
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
                if (saros.getSarosNet() != null) {
                    saros.getSarosNet().removeListener(connectionListener);
                }

                ISarosSession session = currentSession;
                if (session != null)
                    session.removeListener(projectListener);
            }
        });

        /*
         * Double click on a session participant in Saros view jumps to position
         * of clicked user.
         * 
         * Double click on a buddy in the roster adds its JID (bare component
         * part) to the active chat tab input
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
             * Also copies the jabber id of the roster entry to the chat (if
             * there is a chat)
             */
            @Override
            public void mouseDoubleClick(MouseEvent event) {
                TreeItem treeItem = findTreeItemNear(event);
                if (control instanceof Tree) {
                    if (treeItem != null) {

                        User user = (User) Platform.getAdapterManager()
                            .getAdapter(treeItem.getData(), User.class);
                        if (user != null && !user.isLocal()) {
                            SarosView sarosView = ((SarosView) SWTUtils
                                .findView(SarosView.ID));
                            /*
                             * toggle follow mode when doubleclicked on user
                             * element in session tree
                             */
                            if (treeItem.getData() instanceof UserElement) {
                                log.debug("Starting to follow "
                                    + user
                                    + " because of dbl click on UserElement in session view");
                                sarosView.getFollowModeAction()
                                    .setFollowModeActionStatus(user);
                                sarosView.getFollowModeAction().run();
                            } else {
                                /*
                                 * jump to editor position of the user if
                                 * doubleclicked on
                                 * AwarenessTreeInformationElement
                                 */
                                editorManager.jumpToUser(user);
                            }
                            return;
                        }
                    }
                } else {
                    log.warn("Control is not instance of Tree.");
                }
            }

            /**
             * This enables/disables the "follow mode action" ICON button in the
             * icon bar of the Saros view, and registers the user that was
             * clicked on as target for the follow mode button.
             */
            @Override
            public void mouseDown(MouseEvent event) {
                if (control instanceof Tree) {
                    TreeItem treeItem = findTreeItemNear(event);

                    if (treeItem != null) {
                        User user = (User) Platform.getAdapterManager()
                            .getAdapter(treeItem.getData(), User.class);
                        if (user == null) {
                            return;
                        }
                        if (!user.isLocal()) {
                            SarosView sarosView = ((SarosView) SWTUtils
                                .findView(SarosView.ID));
                            sarosView.getFollowModeAction()
                                .setFollowModeActionStatus(user);
                        }
                    }
                } else {
                    log.warn("Control is not instance of Tree.");
                }
            }
        });

    }

    @Override
    protected void createViewer(int style) {
        this.tree = new Tree(this, style);
        this.viewer = new TreeViewer(tree);
    }

    @Override
    protected void configureViewer() {
        this.viewer.setContentProvider(new RosterSessionContentProvider());
        this.viewer.setLabelProvider(new TreeLabelProvider());
        this.viewer.setComparator(new RosterSessionComparator());
        this.viewer.addFilter(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement,
                Object element) {
                /*
                 * Don't show buddies in the buddylist that are part of the
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
        this.viewer.setUseHashlookup(true);

        /*
         * Draw a rounded rectangle indicating the highlighting color that is
         * used for this participant in the current session
         */
        this.tree.addListener(SWT.EraseItem, new Listener() {
            @Override
            public void handleEvent(Event event) {
                event.detail &= ~SWT.HOT;

                if (event.item != null && event.item.getData() != null) {
                    User user = (User) Platform.getAdapterManager().getAdapter(
                        event.item.getData(), User.class);

                    if (event.item.getData() instanceof UserElement
                        && user != null) {
                        GC gc = event.gc;

                        Rectangle bounds = ((TreeItem) event.item)
                            .getBounds(event.index);
                        bounds.width = 15;
                        bounds.x += 15;

                        PaintUtils.drawRoundedRectangle(gc, bounds,
                            SarosAnnotation.getUserColor(user));

                        event.detail &= ~SWT.BACKGROUND;
                    }
                }
            }
        });
    }

    private void updateViewer() {
        if (saros.getSarosNet().getRoster() != null)
            cachedRoster = saros.getSarosNet().getRoster();
        ViewerUtils.setInput(viewer, new RosterSessionInput(cachedRoster,
            sarosSessionManager.getSarosSession()));
    }

    @Override
    public void setLayout(Layout layout) {
        // ignore
    }
}
