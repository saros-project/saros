package saros.feedback;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import saros.annotations.Component;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
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
  private static final String KEY_PARTIAL_SHARED_PROJECTS = "session.shared.project.partial.count";
  private static final String KEY_PARTIAL_SHARED_PROJECTS_FILES =
      "session.shared.project.partial.files.count";

  private static class ProjectInformation {
    public boolean isPartial;
    public int files;
  }

  private final Map<String, ProjectInformation> sharedProjects =
      new HashMap<String, ProjectInformation>();

  private final ISessionListener sessionListener =
      new ISessionListener() {
        @Override
        public void resourcesAdded(IProject project) {
          String projectID = sarosSession.getProjectID(project);

          ProjectInformation info = sharedProjects.get(projectID);

          if (info == null) {
            info = new ProjectInformation();
            sharedProjects.put(projectID, info);
          }

          boolean isPartial = !sarosSession.isCompletelyShared(project);

          /*
           * ignore partial shared projects that were upgraded to full shared
           * projects so we now that the users use at least partial sharing
           */
          if (!info.isPartial && isPartial) info.isPartial = true;

          List<IResource> sharedResources = sarosSession.getSharedResources(project);

          if (sharedResources != null) {
            for (Iterator<IResource> it = sharedResources.iterator(); it.hasNext(); ) {

              IResource resource = it.next();

              if (resource.getType() != IResource.FILE) it.remove();
            }

            info.files = sharedResources.size();
          }
        }
      };

  public ProjectCollector(StatisticManager statisticManager, ISarosSession session) {
    super(statisticManager, session);
  }

  @Override
  protected void processGatheredData() {
    int completeSharedProjects = 0;
    int partialSharedProjects = 0;
    int partialSharedFiles = 0;

    for (ProjectInformation info : sharedProjects.values()) {
      if (info.isPartial) {
        partialSharedProjects++;
        partialSharedFiles += info.files;
      } else completeSharedProjects++;
    }

    data.put(KEY_COMPLETE_SHARED_PROJECTS, completeSharedProjects);
    data.put(KEY_PARTIAL_SHARED_PROJECTS, partialSharedProjects);
    data.put(KEY_PARTIAL_SHARED_PROJECTS_FILES, partialSharedFiles);
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
