package de.fu_berlin.inf.dpp.project.internal;

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
import de.fu_berlin.inf.dpp.project.FileActivityConsumer;
import de.fu_berlin.inf.dpp.project.FolderActivityConsumer;
import de.fu_berlin.inf.dpp.project.SharedResourcesManager;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionContextFactory;
import de.fu_berlin.inf.dpp.session.SarosCoreSessionContextFactory;
import org.picocontainer.MutablePicoContainer;

/** Eclipse implementation of the {@link ISarosSessionContextFactory} interface. */
public class SarosEclipseSessionContextFactory extends SarosCoreSessionContextFactory {

  @Override
  public void createNonCoreComponents(ISarosSession session, MutablePicoContainer container) {

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
    container.addComponent(FollowingActivitiesManager.class);

    // file activity related
    container.addComponent(SharedResourcesManager.class);
    container.addComponent(FileActivityConsumer.class);
    container.addComponent(FolderActivityConsumer.class);
  }
}
