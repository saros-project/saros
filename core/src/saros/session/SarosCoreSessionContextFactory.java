package saros.session;

import org.picocontainer.MutablePicoContainer;
import saros.concurrent.management.ConcurrentDocumentClient;
import saros.concurrent.management.ConcurrentDocumentServer;
import saros.concurrent.watchdog.ConsistencyWatchdogClient;
import saros.concurrent.watchdog.ConsistencyWatchdogHandler;
import saros.concurrent.watchdog.ConsistencyWatchdogServer;
import saros.editor.FollowModeBroadcaster;
import saros.editor.FollowModeManager;
import saros.editor.remote.UserEditorStateManager;
import saros.misc.xstream.SPathConverter;
import saros.misc.xstream.UserConverter;
import saros.session.internal.ActivityHandler;
import saros.session.internal.ActivitySequencer;
import saros.session.internal.ChangeColorManager;
import saros.session.internal.LeaveAndKickHandler;
import saros.session.internal.PermissionManager;
import saros.session.internal.UserInformationHandler;
import saros.session.internal.timeout.ClientSessionTimeoutHandler;
import saros.session.internal.timeout.ServerSessionTimeoutHandler;
import saros.synchronize.StopManager;

/**
 * Basic {@link ISarosSessionContextFactory} implementation which creates the {@link ISarosSession
 * session} components defined in the core.
 *
 * <p>Applications should extend from this class and override {@link #createNonCoreComponents} to
 * create application-specific session components.
 */
public class SarosCoreSessionContextFactory implements ISarosSessionContextFactory {

  @Override
  public final void createComponents(ISarosSession session, MutablePicoContainer container) {

    // Concurrent Editing
    /*
     * As Pico Container complains about null, just add the server even in
     * client mode as it will not matter because it is not accessed.
     */
    container.addComponent(ConcurrentDocumentServer.class);
    container.addComponent(ConcurrentDocumentClient.class);

    // Session Timeout Handling
    if (session.isHost()) container.addComponent(ServerSessionTimeoutHandler.class);
    else container.addComponent(ClientSessionTimeoutHandler.class);

    // Watchdogs
    // FIXME this should only be added to the host context
    container.addComponent(ConsistencyWatchdogHandler.class);

    if (session.isHost()) container.addComponent(ConsistencyWatchdogServer.class);
    else container.addComponent(ConsistencyWatchdogClient.class);

    // Session-dependent XStream Converter
    container.addComponent(SPathConverter.class);
    container.addComponent(UserConverter.class);

    // Other
    container.addComponent(ActivityHandler.class);
    container.addComponent(ActivitySequencer.class);
    container.addComponent(ChangeColorManager.class);
    container.addComponent(FollowModeManager.class);
    container.addComponent(FollowModeBroadcaster.class);
    container.addComponent(LeaveAndKickHandler.class);
    container.addComponent(PermissionManager.class);
    container.addComponent(StopManager.class);
    container.addComponent(UserEditorStateManager.class);
    container.addComponent(UserInformationHandler.class);

    // Non-Core Components
    createNonCoreComponents(session, container);
  }

  /**
   * Override this method in subclasses to add components to the session that are not part of the
   * core.
   *
   * @param container DI container to add session components to
   */
  protected void createNonCoreComponents(ISarosSession session, MutablePicoContainer container) {
    // Does nothing by default
  }
}
