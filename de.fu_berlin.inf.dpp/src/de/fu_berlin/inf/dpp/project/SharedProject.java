package de.fu_berlin.inf.dpp.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSAdapterFactory;
import de.fu_berlin.inf.dpp.vcs.VCSProjectInformation;

public class SharedProject {
    // private static final Logger log = Logger.getLogger(SharedProject.class);

    ISarosSession sarosSession;

    IProject project;

    private VCSProjectInformation projectInformation;

    public SharedProject(IProject project, ISarosSession sarosSession) {
        this.sarosSession = sarosSession;
        this.project = project;

        if (sarosSession.isHost() && sarosSession.useVersionControl()) {
            initializeVCSInformation();
        }
    }

    protected void initializeVCSInformation() {
        VCSAdapter vcs = VCSAdapterFactory.getAdapter(project);
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
            }
            projectInformation = newProjectInformation;
        }
    }
}
