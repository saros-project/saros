package de.fu_berlin.inf.dpp.ui.widgets.viewer.session;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.session.internal.SarosSession;
import de.fu_berlin.inf.dpp.ui.model.TreeLabelProvider;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterComparator;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterContentProvider;
import de.fu_berlin.inf.dpp.ui.model.session.SessionComparator;
import de.fu_berlin.inf.dpp.ui.model.session.SessionContentProvider;
import de.fu_berlin.inf.dpp.ui.model.session.SessionInput;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;

/**
 * This {@link Composite} displays the {@link SarosSession} and the
 * {@link Roster} in parallel.
 * <p>
 * This composite does <strong>NOT</strong> handle setting the layout.
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>NONE and those supported by {@link SessionDisplayComposite}</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * 
 * @author bkahlert
 * 
 */
public final class XMPPSessionDisplayComposite extends SessionDisplayComposite {

    private static final Logger LOG = Logger
        .getLogger(XMPPSessionDisplayComposite.class);

    @Inject
    private XMPPConnectionService connectionService;

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

    public XMPPSessionDisplayComposite(Composite parent, int style) {
        super(parent, style);
        connectionService.addListener(connectionListener);

        addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                connectionService.removeListener(connectionListener);
                connectionService = null;
                cachedRoster = null;
            }
        });
    }

    @Override
    protected void configureViewer(TreeViewer viewer) {
        viewer.setContentProvider(new SessionContentProvider(
            new RosterContentProvider()));

        viewer.setLabelProvider(new TreeLabelProvider());
        viewer.setComparator(new SessionComparator(new RosterComparator()));
        viewer.setUseHashlookup(true);
    }

    @Override
    protected void updateViewer() {
        checkWidget();

        Roster roster = connectionService.getRoster();

        if (roster != null)
            cachedRoster = roster;

        getViewer().setInput(
            new SessionInput(sessionManager.getSession(), cachedRoster));
    }
}
