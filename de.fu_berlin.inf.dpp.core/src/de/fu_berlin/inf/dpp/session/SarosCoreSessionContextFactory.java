package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentServer;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogHandler;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogServer;
import de.fu_berlin.inf.dpp.editor.FollowModeBroadcaster;
import de.fu_berlin.inf.dpp.editor.FollowModeManager;
import de.fu_berlin.inf.dpp.editor.remote.UserEditorStateManager;
import de.fu_berlin.inf.dpp.misc.xstream.SPathConverter;
import de.fu_berlin.inf.dpp.misc.xstream.UserConverter;
import de.fu_berlin.inf.dpp.session.internal.ActivityHandler;
import de.fu_berlin.inf.dpp.session.internal.ActivitySequencer;
import de.fu_berlin.inf.dpp.session.internal.ChangeColorManager;
import de.fu_berlin.inf.dpp.session.internal.LeaveAndKickHandler;
import de.fu_berlin.inf.dpp.session.internal.PermissionManager;
import de.fu_berlin.inf.dpp.session.internal.UserInformationHandler;
import de.fu_berlin.inf.dpp.session.internal.timeout.ClientSessionTimeoutHandler;
import de.fu_berlin.inf.dpp.session.internal.timeout.ServerSessionTimeoutHandler;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import org.picocontainer.MutablePicoContainer;

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
