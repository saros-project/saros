package saros.session;

import saros.concurrent.management.ConcurrentDocumentClient;
import saros.concurrent.management.ConcurrentDocumentServer;
import saros.concurrent.management.HeartbeatDispatcher;
import saros.concurrent.watchdog.ConsistencyWatchdogClient;
import saros.concurrent.watchdog.ConsistencyWatchdogHandler;
import saros.concurrent.watchdog.ConsistencyWatchdogServer;
import saros.editor.FollowModeBroadcaster;
import saros.editor.FollowModeManager;
import saros.editor.remote.UserEditorStateManager;
import saros.misc.xstream.ResourceTransportWrapperConverter;
import saros.misc.xstream.UserConverter;
import saros.negotiation.ResourceNegotiationFactory;
import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.session.internal.ActivityHandler;
import saros.session.internal.ActivitySequencer;
import saros.session.internal.ChangeColorManager;
import saros.session.internal.DeletionAcknowledgmentDispatcher;
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

    // Negotiation
    container.addComponent(ResourceNegotiationFactory.class);

    // Concurrent Editing
    if (session.isHost()) container.addComponent(ConcurrentDocumentServer.class);

    container.addComponent(ConcurrentDocumentClient.class);
    container.addComponent(HeartbeatDispatcher.class);

    // Session Timeout Handling
    if (session.isHost()) container.addComponent(ServerSessionTimeoutHandler.class);
    else container.addComponent(ClientSessionTimeoutHandler.class);

    // Watchdogs
    if (session.isHost()) {
      container.addComponent(ConsistencyWatchdogServer.class);
      container.addComponent(ConsistencyWatchdogHandler.class);
    } else container.addComponent(ConsistencyWatchdogClient.class);

    // Session-dependent XStream Converter
    container.addComponent(ResourceTransportWrapperConverter.class);
    container.addComponent(UserConverter.class);

    // Other
    container.addComponent(ActivityHandler.class);
    container.addComponent(ActivitySequencer.class);
    container.addComponent(ChangeColorManager.class);
    container.addComponent(DeletionAcknowledgmentDispatcher.class);
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
