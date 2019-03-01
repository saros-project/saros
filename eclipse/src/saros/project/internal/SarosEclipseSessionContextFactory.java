package saros.project.internal;

import org.picocontainer.MutablePicoContainer;
import saros.feedback.DataTransferCollector;
import saros.feedback.ErrorLogManager;
import saros.feedback.FeedbackManager;
import saros.feedback.FollowModeCollector;
import saros.feedback.JumpFeatureUsageCollector;
import saros.feedback.ParticipantCollector;
import saros.feedback.PermissionChangeCollector;
import saros.feedback.ProjectCollector;
import saros.feedback.SelectionCollector;
import saros.feedback.SessionDataCollector;
import saros.feedback.StatisticManager;
import saros.feedback.TextEditCollector;
import saros.project.FileActivityConsumer;
import saros.project.FolderActivityConsumer;
import saros.project.SharedResourcesManager;
import saros.session.ISarosSession;
import saros.session.ISarosSessionContextFactory;
import saros.session.SarosCoreSessionContextFactory;

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
