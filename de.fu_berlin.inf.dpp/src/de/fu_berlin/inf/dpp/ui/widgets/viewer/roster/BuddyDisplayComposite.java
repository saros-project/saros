package de.fu_berlin.inf.dpp.ui.widgets.viewer.roster;

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
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.ui.model.TreeLabelProvider;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterComparator;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterContentProvider;
import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.util.ViewerUtils;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.ViewerComposite;

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

        Saros.injectDependenciesOnly(this);

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

    @Override
    public void setLayout(Layout layout) {
        // ignore
    }
}
