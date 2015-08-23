package de.fu_berlin.inf.dpp.project.internal;

import org.picocontainer.MutablePicoContainer;

import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentServer;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogHandler;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogServer;
import de.fu_berlin.inf.dpp.feedback.DataTransferCollector;
import de.fu_berlin.inf.dpp.feedback.ErrorLogManager;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;
import de.fu_berlin.inf.dpp.feedback.FollowModeCollector;
import de.fu_berlin.inf.dpp.feedback.JumpFeatureUsageCollector;
import de.fu_berlin.inf.dpp.feedback.ParticipantCollector;
import de.fu_berlin.inf.dpp.feedback.PermissionChangeCollector;
import de.fu_berlin.inf.dpp.feedback.ProjectCollector;
import de.fu_berlin.inf.dpp.feedback.SelectionCollector;
import de.fu_berlin.inf.dpp.feedback.SessionDataCollector;
import de.fu_berlin.inf.dpp.feedback.StatisticManager;
import de.fu_berlin.inf.dpp.feedback.TextEditCollector;
import de.fu_berlin.inf.dpp.project.SharedResourcesManager;
import de.fu_berlin.inf.dpp.project.internal.timeout.ClientSessionTimeoutHandler;
import de.fu_berlin.inf.dpp.project.internal.timeout.ServerSessionTimeoutHandler;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionContextFactory;
import de.fu_berlin.inf.dpp.session.SarosCoreSessionContextFactory;
import de.fu_berlin.inf.dpp.synchronize.StopManager;

/**
 * Eclipse implementation of the {@link ISarosSessionContextFactory} interface.
 */
public class SarosEclipseSessionContextFactory extends
    SarosCoreSessionContextFactory {

    @Override
    public void createNonCoreComponents(ISarosSession session,
        MutablePicoContainer container) {

        // Concurrent Editing
        /*
         * As Pico Container complains about null, just add the server even in
         * client mode as it will not matter because it is not accessed.
         */
        container.addComponent(ConcurrentDocumentServer.class);
        container.addComponent(ConcurrentDocumentClient.class);

        // Consistency Watchdog
        container.addComponent(ConsistencyWatchdogHandler.class);
        if (session.isHost())
            container.addComponent(ConsistencyWatchdogServer.class);

        // Session Timeout Handling
        if (session.isHost())
            container.addComponent(ServerSessionTimeoutHandler.class);
        else
            container.addComponent(ClientSessionTimeoutHandler.class);

        // Statistic Collectors
        /*
         * If you add a new collector here, make sure to add it to the
         * StatisticCollectorTest as well.
         */
        container.addComponent(StatisticManager.class);
        container.addComponent(DataTransferCollector.class);
        container.addComponent(PermissionChangeCollector.class);
        container.addComponent(ParticipantCollector.class);
        container.addComponent(SessionDataCollector.class);
        container.addComponent(TextEditCollector.class);
        container.addComponent(JumpFeatureUsageCollector.class);
        container.addComponent(FollowModeCollector.class);
        container.addComponent(SelectionCollector.class);
        container.addComponent(ProjectCollector.class);

        // Feedback
        container.addComponent(ErrorLogManager.class);
        container.addComponent(FeedbackManager.class);

        // Other
        container.addComponent(ChangeColorManager.class);
        container.addComponent(FollowingActivitiesManager.class);
        container.addComponent(SharedResourcesManager.class);
        container.addComponent(StopManager.class);
        container.addComponent(UserInformationHandler.class);
    }
}
