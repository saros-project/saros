package saros.ui.widgets.viewer.session;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.picocontainer.annotations.Inject;
import saros.communication.connection.ConnectionHandler;
import saros.communication.connection.IConnectionStateListener;
import saros.net.ConnectionState;
import saros.net.mdns.MDNSService;
import saros.session.internal.SarosSession;
import saros.ui.model.TreeLabelProvider;
import saros.ui.model.mdns.MDNSComparator;
import saros.ui.model.mdns.MDNSContentProvider;
import saros.ui.model.session.SessionComparator;
import saros.ui.model.session.SessionContentProvider;
import saros.ui.model.session.SessionInput;
import saros.ui.util.SWTUtils;
import saros.ui.util.ViewerUtils;

/**
 * This {@link Composite} displays the {@link SarosSession} and the Local Area Network via MDNS in
 * parallel.
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
 * @author srossbach
 */
public final class MDNSSessionDisplayComposite extends SessionDisplayComposite {

  private final Logger LOG = Logger.getLogger(MDNSSessionDisplayComposite.class);

  @Inject private MDNSService mDNSService;

  @Inject private ConnectionHandler connectionHandler;

  private MDNSService currentmDNSService;

  private final IConnectionStateListener connectionStateListener =
      new IConnectionStateListener() {
        @Override
        public void connectionStateChanged(final ConnectionState state, final Exception error) {

          switch (state) {
            case CONNECTING:
            case NOT_CONNECTED:
              break;
            case CONNECTED:
              ViewerUtils.refresh(getViewer(), true);
              // $FALL-THROUGH$
            default:
              return;
          }

          SWTUtils.runSafeSWTAsync(
              LOG,
              new Runnable() {

                @Override
                public void run() {
                  if (getViewer().getControl().isDisposed()) return;

                  currentmDNSService = state == ConnectionState.CONNECTING ? mDNSService : null;

                  updateViewer();
                }
              });
        }
      };

  public MDNSSessionDisplayComposite(Composite parent, int style) {
    super(parent, style);
    connectionHandler.addConnectionStateListener(connectionStateListener);
    currentmDNSService = connectionHandler.isConnected() ? mDNSService : null;

    addDisposeListener(
        new DisposeListener() {
          @Override
          public void widgetDisposed(DisposeEvent e) {
            connectionHandler.removeConnectionStateListener(connectionStateListener);
            connectionHandler = null;
          }
        });
  }

  @Override
  protected void configureViewer(TreeViewer viewer) {
    viewer.setContentProvider(new SessionContentProvider(new MDNSContentProvider()));

    viewer.setComparator(new SessionComparator(new MDNSComparator()));
    viewer.setLabelProvider(new TreeLabelProvider());
    viewer.setUseHashlookup(true);
  }

  @Override
  protected void updateViewer() {
    checkWidget();
    getViewer().setInput(new SessionInput(sessionManager.getSession(), currentmDNSService));
  }
}
