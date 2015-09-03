package de.fu_berlin.inf.dpp.feedback;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.NullSessionLifecycleListener;

/**
 * A Collector class that collects information for each shared project during a
 * session.
 * 
 * @author srossbach
 */
@Component(module = "feedback")
public class ProjectCollector extends AbstractStatisticCollector {

    private final ISarosSessionManager sessionManager;

    private static class ProjectInformation {
        public boolean isPartial;
        public int files;
    }

    private final Map<String, ProjectInformation> sharedProjects = new HashMap<String, ProjectInformation>();

    private final ISessionLifecycleListener sessionLifecycleListener = new NullSessionLifecycleListener() {
        @Override
        public void projectResourcesAvailable(String projectID) {
            ProjectInformation info = sharedProjects.get(projectID);

            if (info == null) {
                info = new ProjectInformation();
                sharedProjects.put(projectID, info);
            }

            IProject project = sarosSession.getProject(projectID);

            if (project == null)
                return;

            boolean isPartial = !sarosSession.isCompletelyShared(project);

            /*
             * ignore partial shared projects that were upgraded to full shared
             * projects so we now that the users use at least partial sharing
             */
            if (!info.isPartial && isPartial)
                info.isPartial = true;

            List<IResource> sharedResources = sarosSession
                .getSharedResources(project);

            if (sharedResources != null) {
                for (Iterator<IResource> it = sharedResources.iterator(); it
                    .hasNext();) {

                    IResource resource = it.next();

                    if (resource.getType() != IResource.FILE)
                        it.remove();
                }

                info.files = sharedResources.size();
            }
        }
    };

    public ProjectCollector(StatisticManager statisticManager,
        ISarosSession session, ISarosSessionManager sessionManager) {
        super(statisticManager, session);
        this.sessionManager = sessionManager;
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
            } else
                completeSharedProjects++;
        }

        data.setSharedProjectStatistic(completeSharedProjects,
            partialSharedProjects, partialSharedFiles);
    }

    @Override
    protected void doOnSessionStart(ISarosSession sarosSession) {
        sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
    }

    @Override
    protected void doOnSessionEnd(ISarosSession sarosSession) {
        sessionManager.removeSessionLifecycleListener(sessionLifecycleListener);
    }
}
