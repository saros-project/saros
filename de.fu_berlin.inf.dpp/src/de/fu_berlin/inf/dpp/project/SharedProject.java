package de.fu_berlin.inf.dpp.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.VCSActivity;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSAdapterFactory;
import de.fu_berlin.inf.dpp.vcs.VCSProjectInformation;

public class SharedProject {
    // private static final Logger log = Logger.getLogger(SharedProject.class);

    protected ISarosSession sarosSession;

    protected IProject project;

    private boolean open;

    protected VCSProjectInformation projectInformation;

    protected VCSAdapter vcs;

    AbstractActivityProvider activityProvider = new AbstractActivityProvider() {

        @Override
        public void exec(IActivity activity) {
            // TODO
        }
    };

    public SharedProject(IProject project, ISarosSession sarosSession) {
        this.sarosSession = sarosSession;
        this.project = project;

        if (sarosSession.isHost() && sarosSession.useVersionControl()) {
            initializeVCSInformation();
        }
    }

    protected void initializeVCSInformation() {
        vcs = VCSAdapterFactory.getAdapter(project);
        if (vcs == null)
            return;
        projectInformation = vcs.getProjectInformation(project);
    }

    protected void updateVCSInformation(IResource resource) {
        assert project.equals(resource.getProject());
        VCSAdapter vcs = VCSAdapterFactory.getAdapter(project);
        if (vcs == null) {
            return;
        }
        if (project.equals(resource)) {
            VCSProjectInformation newProjectInformation = vcs
                .getProjectInformation(project);
            String path = projectInformation.projectPath;
            String newPath = newProjectInformation.projectPath;
            if (!path.equals(newPath)) {
                // Switch
                SPath spath = new SPath(resource);
                IActivity activity = VCSActivity.switch_(
                    sarosSession.getLocalUser(), spath,
                    newProjectInformation.projectPath, "HEAD");
                activityProvider.fireActivity(activity);
                projectInformation.projectPath = newPath;
            }
        }
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isOpen() {
        return open;
    }
}
