package de.fu_berlin.inf.dpp.vcs;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.project.ProjectDeltaVisitor;
import de.fu_berlin.inf.dpp.project.SharedProject;
import de.fu_berlin.inf.dpp.session.ISarosSession;

/**
 * Visits the resource changes in a shared SVN project.<br>
 * <br>
 * When running an SVN operation, Subclipse uses multiple jobs, which results in
 * multiple resourceChanged events. This visitor detects these
 * Subclipse-specific jobs.<br>
 * The visitor is not supposed to be reused for multiple different
 * resourceChanged events.
 */
public class SubclipseProjectDeltaVisitor extends ProjectDeltaVisitor {

    protected final VCSAdapter vcs;

    public SubclipseProjectDeltaVisitor(EditorManager editorManager,
        ISarosSession sarosSession, SharedProject sharedProject) {
        super(editorManager, sarosSession, sharedProject);
        vcs = sharedProject.getVCSAdapter();
    }

    // Don't ignore 'SyncFileChangeOperation'
    private static final Collection<String> jobNames = Arrays.asList(
        "SVN Update", "SVN Switch", "ResourcesChangedOperation");

    @Override
    public boolean visit(IResourceDelta delta) {
        IResource resource = delta.getResource();

        if (resource.isDerived())
            return false;

        assert resource.getProject().isOpen();

        // FIXME the DeltaVisitor should use an SubclipseAdapter instance
        assert vcs != null && (vcs instanceof SubclipseAdapter)
            && vcs.equals(sharedProject.getVCSAdapter());

        /*
         * Note that it is possible to get a delta with both a sync info change
         * and a content change.
         */
        if (isSync(delta)) {
            VCSResourceInfo info = vcs.getResourceInfo(resource);
            if (sharedProject.updateVcsUrl(resource, info.getURL())) {
                sharedProject.updateRevision(resource, info.getRevision());
                // Switch
                if (info.getRevision() != null && !ignoreChildren(resource)) {
                    addActivity(vcs.getSwitchActivity(sarosSession, resource));
                    setIgnoreChildren(resource);
                }
            } else if (sharedProject.updateRevision(resource,
                info.getRevision())) {
                // Update
                if (!ignoreChildren(resource)) {
                    addActivity(vcs.getUpdateActivity(sarosSession, resource));
                    setIgnoreChildren(resource);
                }
            }
        }

        if (resource instanceof IProject) {
            // Ignore all the jobs from SVN.
            IJobManager jobManager = Job.getJobManager();
            Job currentJob = jobManager.currentJob();
            if (currentJob != null) {
                String jobName = currentJob.getName();
                if (jobNames.contains(jobName)) {
                    setPostponeSending(true);
                }
            }
        }

        final boolean visitChildren = super.visit(delta);

        return visitChildren;
    }

    @Override
    public void add(IResource resource) {
        super.add(resource);
        updateInfo(resource);
    }

    @Override
    protected void move(IResource resource, IPath oldPath, IProject oldProject,
        boolean contentChange) throws IOException {
        super.move(resource, oldPath, oldProject, contentChange);
        updateInfo(resource);
    }

    protected void updateInfo(IResource resource) {
        if (!vcs.isManaged(resource)) {
            return;
        }
        VCSResourceInfo info = vcs.getResourceInfo(resource);
        sharedProject.updateVcsUrl(resource, info.getURL());
        sharedProject.updateRevision(resource, info.getRevision());
    }

}
