package de.fu_berlin.inf.dpp.ui.widgets.viewer.rosterSession;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.XMPPConnectionService;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.project.internal.SarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.model.TreeLabelProvider;
import de.fu_berlin.inf.dpp.ui.model.rosterSession.RosterSessionComparator;
import de.fu_berlin.inf.dpp.ui.model.rosterSession.RosterSessionContentProvider;
import de.fu_berlin.inf.dpp.ui.model.rosterSession.RosterSessionInput;
import de.fu_berlin.inf.dpp.ui.model.rosterSession.UserElement;
import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.util.PaintUtils;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.util.ViewerUtils;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.ViewerComposite;

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

    private static final Logger LOG = Logger
        .getLogger(BuddySessionDisplayComposite.class);

    @Inject
    private XMPPConnectionService connectionService;

    @Inject
    private ISarosSessionManager sarosSessionManager;

    @Inject
    private EditorManager editorManager;

    private ViewerFilter filter;

    /**
     * Used to display the {@link Roster} even in case the user is disconnected.
     */
    private Roster cachedRoster;

    private final IConnectionListener connectionListener = new IConnectionListener() {
        @Override
        public void connectionStateChanged(Connection connection,
            ConnectionState state) {

            boolean inputChanged = false;

            switch (state) {
            case CONNECTED:
            case NOT_CONNECTED:
                inputChanged = true;
                break;
            default:
                break;
            }

            if (!inputChanged)
                return;

            SWTUtils.runSafeSWTAsync(LOG, new Runnable() {

                @Override
                public void run() {
                    if (getViewer().getControl().isDisposed())
                        return;

                    updateViewer();
                    getViewer().expandAll();
                }
            });
        }
    };

    private final ISarosSessionListener sarosSessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionStarting(final ISarosSession session) {
            SWTUtils.runSafeSWTAsync(LOG, new Runnable() {

                @Override
                public void run() {
                    if (getViewer().getControl().isDisposed())
                        return;

                    if (filter != null)
                        getViewer().removeFilter(filter);

                    updateViewer();
                    getViewer().expandAll();
                    filter = new HideContactsInSessionFilter(session);
                    getViewer().addFilter(filter);
                }
            });
        }

        @Override
        public void sessionEnded(ISarosSession session) {
            SWTUtils.runSafeSWTAsync(LOG, new Runnable() {

                @Override
                public void run() {
                    if (getViewer().getControl().isDisposed())
                        return;

                    if (filter != null)
                        getViewer().removeFilter(filter);

                    filter = null;

                    updateViewer();
                    getViewer().expandAll();
                }
            });
        }

        @Override
        public void projectAdded(String projectID) {
            ViewerUtils.refresh(getViewer(), true);
        }
    };

    public BuddySessionDisplayComposite(Composite parent, int style) {
        super(parent, style);

        SarosPluginContext.initComponent(this);

        super.setLayout(LayoutUtils.createGridLayout());

        getViewer().getControl()
            .setLayoutData(LayoutUtils.createFillGridData());

        updateViewer();
        getViewer().expandAll();

        connectionService.addListener(connectionListener);
        sarosSessionManager.addSarosSessionListener(sarosSessionListener);

        ISarosSession session = sarosSessionManager.getSarosSession();

        if (session != null) {
            filter = new HideContactsInSessionFilter(session);
            getViewer().addFilter(filter);
        }

        addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {

                if (sarosSessionManager != null) {
                    sarosSessionManager
                        .removeSarosSessionListener(sarosSessionListener);
                }

                if (connectionService != null)
                    connectionService.removeListener(connectionListener);

                filter = null;
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
        viewer.setUseHashlookup(true);

        /*
         * Draw a rounded rectangle indicating the highlighting color that is
         * used for this participant in the current session
         */
        viewer.getTree().addListener(SWT.PaintItem, new Listener() {
            @Override
            public void handleEvent(Event event) {

                TreeItem treeItem = (TreeItem) event.item;

                /*
                 * do not adapt the object or we will draw into widget / tree
                 * items that should not be *decorated*
                 */

                if (!(treeItem.getData() instanceof UserElement))
                    return;

                User user = (User) ((UserElement) treeItem.getData()).getUser();

                Rectangle bounds = treeItem.getBounds(event.index);

                bounds.width = 15;
                bounds.x += 15;

                /*
                 * make the rectangle a little bit smaller so it does not
                 * collide with the edges when the tree item is selected
                 */

                bounds.y += 2;
                bounds.height -= 4;

                Color background = SarosAnnotation.getUserColor(user);
                PaintUtils.drawRoundedRectangle(event.gc, bounds, background);
                background.dispose();
            }
        });
    }

    private void updateViewer() {
        checkWidget();

        Roster roster = connectionService.getRoster();

        if (roster != null)
            cachedRoster = roster;

        getViewer().setInput(
            new RosterSessionInput(cachedRoster, sarosSessionManager
                .getSarosSession()));
    }

    @Override
    public void setLayout(Layout layout) {
        // ignore
    }
}
