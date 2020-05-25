package saros.feedback;

import java.util.HashMap;
import java.util.Map;
import saros.annotations.Component;
import saros.filesystem.IReferencePoint;
import saros.session.ISarosSession;
import saros.session.ISessionListener;

/**
 * A Collector class that collects information for each shared project during a session.
 *
 * @author srossbach
 */
@Component(module = "feedback")
public class ProjectCollector extends AbstractStatisticCollector {

  // Keys for shared project information
  private static final String KEY_COMPLETE_SHARED_PROJECTS =
      "session.shared.project.complete.count";

  private static class ProjectInformation {
    public int files;
  }

  private final Map<String, ProjectInformation> sharedProjects =
      new HashMap<String, ProjectInformation>();

  private final ISessionListener sessionListener =
      new ISessionListener() {
        @Override
        public void resourcesAdded(IReferencePoint project) {
          String projectID = sarosSession.getProjectID(project);

          ProjectInformation info = sharedProjects.get(projectID);

          if (info == null) {
            info = new ProjectInformation();
            sharedProjects.put(projectID, info);
          }
        }
      };

  public ProjectCollector(StatisticManager statisticManager, ISarosSession session) {
    super(statisticManager, session);
  }

  @Override
  protected void processGatheredData() {
    int completeSharedProjects = sharedProjects.size();

    data.put(KEY_COMPLETE_SHARED_PROJECTS, completeSharedProjects);
  }

  @Override
  protected void doOnSessionStart(ISarosSession sarosSession) {
    sarosSession.addListener(sessionListener);
  }

  @Override
  protected void doOnSessionEnd(ISarosSession sarosSession) {
    sarosSession.removeListener(sessionListener);
  }
}
