package de.fu_berlin.inf.dpp.ui.widgets.session;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.editors.text.EditorsUI;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.net.IRosterListener;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.model.session.SessionContentProvider;
import de.fu_berlin.inf.dpp.ui.model.session.SessionLabelProvider;
import de.fu_berlin.inf.dpp.ui.sarosView.SessionViewTableViewer;
import de.fu_berlin.inf.dpp.ui.util.ViewerUtils;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.SimpleExplanationComposite.SimpleExplanation;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.explanatory.SimpleExplanatoryComposite;
import de.fu_berlin.inf.dpp.util.Utils;

public class SessionComposite extends SimpleExplanatoryComposite {

    private Logger log = Logger.getLogger(SessionComposite.class);

    @Inject
    SarosSessionObservable sarosSessionObservable;

    @Inject
    protected EditorManager editorManager;

    @Inject
    protected RosterTracker rosterTracker;

    @Inject
    protected SarosSessionManager sessionManager;

    private SessionViewTableViewer tableViewer;

    protected SimpleExplanation noSarosSessionRunning = new SimpleExplanation(
        "No running session");

    protected IPropertyChangeListener editorPrefsListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            tableViewer.refresh();
        }
    };

    protected ISharedEditorListener sharedEditorListener = new AbstractSharedEditorListener() {
        @Override
        public void followModeChanged(User user) {
            tableViewer.refresh();
        }

        @Override
        public void colorChanged() {
            tableViewer.refresh();
        }
    };

    public final ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(final ISarosSession newSarosSession) {
            Utils.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    tableViewer.setInput(newSarosSession);
                    hideExplanation();
                    layout();
                }
            });
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            Utils.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    showExplanation(noSarosSessionRunning);
                    tableViewer.setInput(null);
                    layout();
                }
            });
        }
    };

    /**
     * This RosterListener is responsible to trigger updates to our table
     * viewer, whenever roster elements change.
     * 
     * This is mostly used to update the nickname of the user at the moment.
     */
    IRosterListener rosterListener = new IRosterListener() {
        public void presenceChanged(Presence presence) {
            ViewerUtils.refresh(tableViewer, true);
        }

        public void entriesUpdated(Collection<String> addresses) {
            ViewerUtils.refresh(tableViewer, true);
            log.warn("Hunger");
        }

        public void entriesDeleted(Collection<String> addresses) {
            ViewerUtils.refresh(tableViewer, true);
        }

        public void entriesAdded(Collection<String> addresses) {
            ViewerUtils.refresh(tableViewer, true);
        }

        public void rosterChanged(Roster roster) {
            ViewerUtils.refresh(tableViewer, true);
        }
    };

    public SessionComposite(Composite parent, int style) {
        super(parent, style);

        SarosPluginContext.initComponent(this);
        this.setBackgroundMode(SWT.INHERIT_DEFAULT);
        final GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginTop = 0;
        this.setLayout(gridLayout);

        Composite containerComposite = new Composite(this, SWT.NONE);
        this.setContentControl(containerComposite);
        containerComposite.setBackground(Display.getDefault().getSystemColor(
            SWT.COLOR_WHITE));

        if (sarosSessionObservable.getValue() == null) {
            this.showExplanation(noSarosSessionRunning);
        } else {
            this.hideExplanation();
        }

        this.tableViewer = new SessionViewTableViewer(new Table(
            containerComposite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.FULL_SELECTION));

        // Make sure one column we got fills the whole table
        Table table = this.tableViewer.getTable();

        TableColumnLayout tableColLayout = new TableColumnLayout();
        containerComposite.setLayout(tableColLayout);
        TableColumn column = new TableColumn(table, SWT.NONE);
        tableColLayout.setColumnData(column, new ColumnWeightData(100));

        this.tableViewer.setContentProvider(new SessionContentProvider());
        final SessionLabelProvider labelProvider = new SessionLabelProvider();
        this.tableViewer.setLabelProvider(labelProvider);

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

        this.tableViewer.setInput(sessionManager.getSarosSession());

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
        rosterTracker.addRosterListener(rosterListener);

        sessionManager.addSarosSessionListener(sessionListener);

        this.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {

                sessionManager.removeSarosSessionListener(sessionListener);
                rosterTracker.removeRosterListener(rosterListener);
                editorManager.removeSharedEditorListener(sharedEditorListener);
                EditorsUI.getPreferenceStore().removePropertyChangeListener(
                    editorPrefsListener);
            }
        });

    }

    public SessionViewTableViewer getSessionViewer() {
        return this.tableViewer;
    }
}
