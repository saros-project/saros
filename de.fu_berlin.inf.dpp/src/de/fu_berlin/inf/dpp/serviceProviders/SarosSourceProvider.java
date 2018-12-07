package de.fu_berlin.inf.dpp.serviceProviders;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandler;
import de.fu_berlin.inf.dpp.communication.connection.IConnectionStateListener;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.picocontainer.annotations.Inject;

/**
 * Adds variables to Eclipse's Core {@link Expression}s in order to be used in plugin.xml.
 *
 * @author bkahlert
 */
public class SarosSourceProvider extends AbstractSourceProvider {

  /** Corresponds to a serviceProvider variable as defined in Extension org.eclipse.ui.services. */
  public static final String SAROS = "de.fu_berlin.inf.dpp.Saros";

  /** Corresponds to a serviceProvider variable as defined in Extension org.eclipse.ui.services. */
  public static final String SAROS_SESSION = "de.fu_berlin.inf.dpp.SarosSession";

  @Inject private Saros saros;

  @Inject private ISarosSessionManager sessionManager;

  @Inject private ConnectionHandler connectionHandler;

  private IConnectionStateListener connectionStateListener =
      new IConnectionStateListener() {
        @Override
        public void connectionStateChanged(final ConnectionState state, final Exception error) {
          connectionChanged();
        }
      };

  private ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {

        @Override
        public void sessionStarted(ISarosSession session) {
          sessionChanged(session);
        }

        @Override
        public void sessionEnding(ISarosSession session) {
          sessionChanged(null);
        }
      };

  public SarosSourceProvider() {
    SarosPluginContext.initComponent(this);
    connectionHandler.addConnectionStateListener(connectionStateListener);

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
  }

  @Override
  public void dispose() {
    sessionManager.removeSessionLifecycleListener(sessionLifecycleListener);
    connectionHandler.removeConnectionStateListener(connectionStateListener);
  }

  @Override
  public String[] getProvidedSourceNames() {
    return new String[] {SAROS, SAROS_SESSION};
  }

  @Override
  public Map<Object, Object> getCurrentState() {
    Object session = sessionManager.getSession();

    if (session == null) session = IEvaluationContext.UNDEFINED_VARIABLE;

    Map<Object, Object> map = new HashMap<Object, Object>(2);
    map.put(SAROS, saros);
    map.put(SAROS_SESSION, session);
    return map;
  }

  private final void connectionChanged() {
    SWTUtils.runSafeSWTAsync(
        null,
        new Runnable() {
          @Override
          public void run() {
            fireSourceChanged(ISources.WORKBENCH, SAROS, saros);
          }
        });
  }

  private final void sessionChanged(final ISarosSession session) {
    SWTUtils.runSafeSWTAsync(
        null,
        new Runnable() {
          @Override
          public void run() {
            fireSourceChanged(
                ISources.WORKBENCH,
                SAROS_SESSION,
                session == null ? IEvaluationContext.UNDEFINED_VARIABLE : session);
          }
        });
  }
}
