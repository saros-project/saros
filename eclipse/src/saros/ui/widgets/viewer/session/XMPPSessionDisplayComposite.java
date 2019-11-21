package saros.ui.widgets.viewer.session;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import saros.communication.connection.ConnectionHandler;
import saros.communication.connection.IConnectionStateListener;
import saros.net.ConnectionState;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.internal.SarosSession;
import saros.ui.model.TreeLabelProvider;
import saros.ui.model.roster.RosterComparator;
import saros.ui.model.roster.RosterContentProvider;
import saros.ui.model.session.SessionComparator;
import saros.ui.model.session.SessionContentProvider;
import saros.ui.model.session.SessionInput;
import saros.ui.util.SWTUtils;

/**
 * This {@link Composite} displays the {@link SarosSession} and the Contact list in parallel.
 *
 * <p>This composite does <strong>NOT</strong> handle setting the layout.
 *
 * <dl>
 *   <dt><b>Styles:</b>
 *   <dd>NONE and those supported by {@link SessionDisplayComposite}
 *   <dt><b>Events:</b>
 *   <dd>(none)
 * </dl>
 *
 * @author bkahlert
 */
public final class XMPPSessionDisplayComposite extends SessionDisplayComposite {

  private static final Logger LOG = Logger.getLogger(XMPPSessionDisplayComposite.class);

  @Inject private ConnectionHandler connectionHandler;
  @Inject private XMPPContactsService contactsService;

  private final IConnectionStateListener connectionListener =
      (state, error) -> {
        if (state == ConnectionState.CONNECTED || state == ConnectionState.NOT_CONNECTED) {
          SWTUtils.runSafeSWTAsync(
              LOG,
              () -> {
                if (getViewer().getControl().isDisposed()) return;

                updateViewer();
                getViewer().expandAll();
              });
        }
      };

  public XMPPSessionDisplayComposite(Composite parent, int style) {
    super(parent, style);
    connectionHandler.addConnectionStateListener(connectionListener);

    addDisposeListener(
        e -> {
          connectionHandler.removeConnectionStateListener(connectionListener);
          connectionHandler = null;
          contactsService = null;
        });
  }

  @Override
  protected void configureViewer(TreeViewer viewer) {
    viewer.setContentProvider(new SessionContentProvider(new RosterContentProvider()));

    viewer.setLabelProvider(new TreeLabelProvider());
    viewer.setComparator(new SessionComparator(new RosterComparator()));
    viewer.setUseHashlookup(true);
  }

  @Override
  protected void updateViewer() {
    checkWidget();

    getViewer().setInput(new SessionInput(sessionManager.getSession(), contactsService));
  }
}
