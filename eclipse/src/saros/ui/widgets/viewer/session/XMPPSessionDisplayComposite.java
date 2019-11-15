package saros.ui.widgets.viewer.session;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import saros.net.ConnectionState;
import saros.net.xmpp.IConnectionListener;
import saros.net.xmpp.XMPPConnectionService;
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
 * This {@link Composite} displays the {@link SarosSession} and the {@link Roster} in parallel.
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

  @Inject private XMPPConnectionService connectionService;
  @Inject private XMPPContactsService contactsService;

  private final IConnectionListener connectionListener =
      new IConnectionListener() {
        @Override
        public void connectionStateChanged(Connection connection, ConnectionState state) {

          boolean inputChanged = false;

          switch (state) {
            case CONNECTED:
            case NOT_CONNECTED:
              inputChanged = true;
              break;
            default:
              break;
          }

          if (!inputChanged) return;

          SWTUtils.runSafeSWTAsync(
              LOG,
              new Runnable() {

                @Override
                public void run() {
                  if (getViewer().getControl().isDisposed()) return;

                  updateViewer();
                  getViewer().expandAll();
                }
              });
        }
      };

  public XMPPSessionDisplayComposite(Composite parent, int style) {
    super(parent, style);
    connectionService.addListener(connectionListener);

    addDisposeListener(
        new DisposeListener() {
          @Override
          public void widgetDisposed(DisposeEvent e) {
            connectionService.removeListener(connectionListener);
            connectionService = null;
            contactsService = null;
          }
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
