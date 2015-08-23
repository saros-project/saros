package de.fu_berlin.inf.dpp.core.project.internal;

import org.picocontainer.MutablePicoContainer;

import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentServer;
import de.fu_berlin.inf.dpp.core.concurrent.ConsistencyWatchdogHandler;
import de.fu_berlin.inf.dpp.core.concurrent.ConsistencyWatchdogServer;
import de.fu_berlin.inf.dpp.intellij.project.SharedResourcesManager;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionContextFactory;
import de.fu_berlin.inf.dpp.session.SarosCoreSessionContextFactory;
import de.fu_berlin.inf.dpp.synchronize.StopManager;

/**
 * IntelliJ implementation of the {@link ISarosSessionContextFactory} interface.
 */
public class SarosIntellijSessionContextFactory extends
    SarosCoreSessionContextFactory {

    @Override
    public void createNonCoreComponents(ISarosSession session,
        MutablePicoContainer container) {

        // Concurrent Editing
        container.addComponent(ConcurrentDocumentServer.class);
        container.addComponent(ConcurrentDocumentClient.class);

        // Consistency Watchdog
        container.addComponent(ConsistencyWatchdogHandler.class);
        if (session.isHost())
            container.addComponent(ConsistencyWatchdogServer.class);

        // Session Timeout Handling
        if (session.isHost()) {
            container.addComponent(ServerSessionTimeoutHandler.class);
        } else {
            container.addComponent(ClientSessionTimeoutHandler.class);
        }

        // Other
        container.addComponent(FollowingActivitiesManager.class);
        container.addComponent(SharedResourcesManager.class);
        container.addComponent(StopManager.class);
        container.addComponent(UserInformationHandler.class);
    }

}
