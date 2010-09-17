package de.fu_berlin.inf.dpp.vcs;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

import de.fu_berlin.inf.dpp.activities.business.VCSActivity;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ProjectDeltaVisitor;
import de.fu_berlin.inf.dpp.project.SharedProject;
import de.fu_berlin.inf.dpp.project.SharedResourcesManager;

/**
 * Visits the resource changes in a shared SVN project.<br>
 * <br>
 * When running an SVN operation, Subclipse uses multiple jobs, which results in
 * multiple resourceChanged events. This visitor detects these
 * Subclipse-specific jobs.
 */
public class SubclipseProjectDeltaVisitor extends ProjectDeltaVisitor {

    public SubclipseProjectDeltaVisitor(
        SharedResourcesManager sharedResourcesManager,
        ISarosSession sarosSession, SharedProject sharedProject) {
        super(sharedResourcesManager, sarosSession, sharedProject);
    }

    private static final Collection<String> jobNames = Arrays.asList(
        "SVN Update", "SVN Switch", "ResourcesChangedOperation");

    @Override
    public boolean visit(IResourceDelta delta) {
        IResource resource = delta.getResource();
        if (!(resource instanceof IProject)) {
            return super.visit(delta);
        }

        IProject project = (IProject) resource;
        assert project.isOpen();

        VCSAdapter vcs = VCSAdapter.getAdapter(project);
        assert vcs != null;

        if (isSync(delta)) {
            VCSResourceInfo info = vcs.getResourceInfo(project);
            String url = info.url;
            if (sharedProject.updateVcsUrl(url)) {
                // Switch
                addActivity(VCSActivity.switch_(sarosSession, resource, url,
                    info.revision));
                return false;
            }

            if (sharedProject.updateRevision(info.revision)) {
                // Update
                addActivity(VCSActivity.update(sarosSession, resource,
                    info.revision));
                return false;
            }
        }

        // Ignore all the jobs from SVN.
        IJobManager jobManager = Job.getJobManager();
        Job currentJob = jobManager.currentJob();
        if (currentJob != null) {
            String jobName = currentJob.getName();
            if (jobNames.contains(jobName)) {
                return false;
            }
        }

        return super.visit(delta);
    }
}
