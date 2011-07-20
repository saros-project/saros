package de.fu_berlin.inf.dpp.ui.widgets.viewer.rosterSession;

import java.util.Collection;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.editors.text.EditorsUI;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.IRosterListener;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.project.internal.SarosSession;
import de.fu_berlin.inf.dpp.ui.model.TreeLabelProvider;
import de.fu_berlin.inf.dpp.ui.model.rosterSession.RosterSessionComparator;
import de.fu_berlin.inf.dpp.ui.model.rosterSession.RosterSessionContentProvider;
import de.fu_berlin.inf.dpp.ui.model.rosterSession.RosterSessionInput;
import de.fu_berlin.inf.dpp.ui.model.rosterSession.UserElement;
import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.util.PaintUtils;
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
public class BuddySessionDisplayComposite extends ViewerComposite {

    @Inject
    protected Saros saros;

    @Inject
    protected SarosSessionManager sarosSessionManager;

    @Inject
    protected EditorManager editorManager;

    @Inject
    protected RosterTracker rosterTracker;

    protected IRosterListener rosterListener = new IRosterListener() {

        public void entriesAdded(Collection<String> addresses) {
            updateViewer();
            ViewerUtils.expandAll(viewer);
        }

        public void entriesUpdated(Collection<String> addresses) {
            updateViewer();
            ViewerUtils.expandAll(viewer);
        }

        public void entriesDeleted(Collection<String> addresses) {
            updateViewer();
            ViewerUtils.expandAll(viewer);
        }

        public void presenceChanged(Presence presence) {
            ViewerUtils.refresh(viewer, true);
        }

        public void rosterChanged(Roster roster) {
            updateViewer();
            ViewerUtils.expandAll(viewer);
        }

    };

    protected IConnectionListener connectionListener = new IConnectionListener() {
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

    protected ISarosSessionListener sarosSessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionStarting(ISarosSession newSarosSession) {
            updateViewer();
            ViewerUtils.expandAll(viewer);
        }

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            updateViewer();
            ViewerUtils.expandAll(viewer);
        }

        @Override
        public void sessionEnding(ISarosSession oldSarosSession) {
            updateViewer();
            ViewerUtils.expandAll(viewer);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            updateViewer();
            ViewerUtils.expandAll(viewer);
        }

        @Override
        public void projectAdded(String projectID) {
            updateViewer();
            ViewerUtils.expandAll(viewer);
        }

        @Override
        public void preIncomingInvitationCompleted(SubMonitor subMonitor) {
            updateViewer();
            ViewerUtils.expandAll(viewer);
        }

        @Override
        public void postOutgoingInvitationCompleted(SubMonitor subMonitor,
            User user) {
            updateViewer();
            ViewerUtils.expandAll(viewer);
        }
    };

    protected IPropertyChangeListener editorPrefsListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            ViewerUtils.refresh(viewer, true);
            ViewerUtils.expandAll(viewer);
        }
    };

    protected ISharedEditorListener sharedEditorListener = new AbstractSharedEditorListener() {
        @Override
        public void followModeChanged(User user, boolean isFollowed) {
            ViewerUtils.update(viewer, new UserElement(user, saros
                .getSarosNet().getRoster()), null);
        }

        @Override
        public void colorChanged() {
            ViewerUtils.refresh(viewer, true);
            ViewerUtils.expandAll(viewer);
        }
    };

    protected Tree tree;

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
        ViewerUtils.expandAll(this.viewer);

        saros.getSarosNet().addListener(connectionListener);
        this.sarosSessionManager.addSarosSessionListener(sarosSessionListener);
        EditorsUI.getPreferenceStore().addPropertyChangeListener(
            editorPrefsListener);
        editorManager.addSharedEditorListener(sharedEditorListener);

        this.rosterTracker.addRosterListener(rosterListener);

        this.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (editorManager != null) {
                    editorManager
                        .removeSharedEditorListener(sharedEditorListener);
                }
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
                if (rosterTracker != null) {
                    rosterTracker.removeRosterListener(rosterListener);
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
        this.viewer.setUseHashlookup(true);

        /*
         * Draw a rounded rectangle around the participants of a session.
         */
        this.tree.addListener(SWT.EraseItem, new Listener() {
            public void handleEvent(Event event) {
                event.detail &= ~SWT.HOT;
                if ((event.detail & SWT.SELECTED) != 0)
                    return;

                if (event.item != null && event.item.getData() != null) {
                    User user = (User) Platform.getAdapterManager().getAdapter(
                        event.item.getData(), User.class);

                    if (event.item instanceof TreeItem && user != null) {
                        GC gc = event.gc;
                        Rectangle bounds = ((TreeItem) event.item)
                            .getBounds(event.index);
                        bounds.width = Math
                            .max(
                                bounds.width,
                                BuddySessionDisplayComposite.this
                                    .getClientArea().width - 2 * bounds.x);
                        PaintUtils.drawRoundedRectangle(gc, bounds,
                            SarosAnnotation.getUserColor(user));
                    }
                }
            }
        });
    }

    protected void updateViewer() {
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
