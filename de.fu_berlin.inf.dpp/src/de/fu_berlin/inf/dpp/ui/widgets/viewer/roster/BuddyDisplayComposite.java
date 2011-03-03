package de.fu_berlin.inf.dpp.ui.widgets.viewer.roster;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Tree;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPConnection;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.ui.model.TreeLabelProvider;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterComparator;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterContentProvider;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterEntryElement;
import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.util.ViewerUtils;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.ViewerComposite;
import de.fu_berlin.inf.dpp.util.ArrayUtils;

/**
 * This {@link Composite} displays the {@link Roster} with its
 * {@link RosterGroup}s and {@link RosterEntry}s.
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
public class BuddyDisplayComposite extends ViewerComposite {

    @Inject
    protected Saros saros;

    protected IConnectionListener connectionListener = new IConnectionListener() {
        public void connectionStateChanged(XMPPConnection connection,
            ConnectionState newState) {
            switch (newState) {
            case CONNECTED:
                ViewerUtils.setInput(viewer, saros.getRoster());
                ViewerUtils.expandAll(viewer);
                break;
            case NOT_CONNECTED:
                /*
                 * The Roster should also be displayed in case we are not
                 * connected but have been already connected before.
                 */
                // ViewerUtils.setInput(viewer, null);
                break;
            default:
                break;
            }
        }
    };

    public BuddyDisplayComposite(Composite parent, int style) {
        super(parent, style);

        SarosPluginContext.initComponent(this);

        super.setLayout(LayoutUtils.createGridLayout());
        this.viewer.getControl()
            .setLayoutData(LayoutUtils.createFillGridData());
        this.viewer.setInput(saros.getRoster());
        ViewerUtils.expandAll(this.viewer);

        this.saros.addListener(connectionListener);

        this.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (saros != null) {
                    saros.removeListener(connectionListener);
                }
            }
        });
    }

    @Override
    protected void createViewer(int style) {
        this.viewer = new TreeViewer(new Tree(this, style));
    }

    @Override
    protected void configureViewer() {
        this.viewer.setContentProvider(new RosterContentProvider());
        this.viewer.setLabelProvider(new TreeLabelProvider());
        this.viewer.setComparator(new RosterComparator());
        this.viewer.setUseHashlookup(true);
    }

    /**
     * Returns the displayed {@link JID}s.
     * 
     * @return
     */
    public List<JID> getBuddies() {
        List<RosterEntryElement> objects = collectAllRosterEntryElement((TreeViewer) this.viewer);
        return ArrayUtils.getAdaptableObjects(objects.toArray(), JID.class);
    }

    /**
     * Returns the displayed {@link JID}s that support Saros.
     * 
     * @return
     */
    public List<JID> getBuddiesWithSarosSupport() {
        List<JID> buddies = new ArrayList<JID>();
        List<RosterEntryElement> objects = collectAllRosterEntryElement((TreeViewer) this.viewer);
        for (RosterEntryElement rosterEntryElement : objects) {
            if (rosterEntryElement.isSarosSupported()) {
                JID jid = (JID) rosterEntryElement.getAdapter(JID.class);
                if (jid != null && !buddies.contains(jid)) {
                    buddies.add(jid);
                }
            }
        }
        return buddies;
    }

    /**
     * Gathers the checked states of the given widget and its descendants,
     * following a pre-order traversal of the {@link ITreeContentProvider}.
     * 
     * @param treeViewer
     *            to be traversed
     * @return
     */
    protected static List<RosterEntryElement> collectAllRosterEntryElement(
        TreeViewer treeViewer) {
        ITreeContentProvider treeContentProvider = (ITreeContentProvider) treeViewer
            .getContentProvider();

        List<Object> collectedObjects = new ArrayList<Object>();

        Object[] objects = treeContentProvider.getElements(treeViewer
            .getInput());
        for (Object object : objects) {
            collectedObjects.add(object);
            collectAllRosterEntryElement(collectedObjects, treeViewer, object);
        }

        return ArrayUtils.getInstances(collectedObjects.toArray(),
            RosterEntryElement.class);
    }

    /**
     * Gathers the checked states of the given widget and its descendants,
     * following a pre-order traversal of the {@link ITreeContentProvider}.
     * 
     * @param collectedObjects
     *            a writable list of elements (element type: <code>Object</code>
     *            )
     * @param treeViewer
     *            to be traversed
     * @param parentElement
     *            of which to determine the child nodes
     */
    protected static void collectAllRosterEntryElement(
        List<Object> collectedObjects, TreeViewer treeViewer,
        Object parentElement) {
        ITreeContentProvider treeContentProvider = (ITreeContentProvider) treeViewer
            .getContentProvider();
        Object[] objects = treeContentProvider.getChildren(parentElement);
        for (Object object : objects) {
            collectedObjects.add(object);
            collectAllRosterEntryElement(collectedObjects, treeViewer, object);
        }
    }

    @Override
    public void setLayout(Layout layout) {
        // ignore
    }
}
