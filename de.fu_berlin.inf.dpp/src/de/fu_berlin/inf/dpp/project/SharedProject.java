package de.fu_berlin.inf.dpp.project;

import org.eclipse.core.resources.IProject;

import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSAdapterFactory;
import de.fu_berlin.inf.dpp.vcs.VCSProjectInformation;

public class SharedProject {
    // private static final Logger log = Logger.getLogger(SharedProject.class);

    protected ISarosSession sarosSession;

    protected IProject project;

    private boolean open;

    public String vcsUrl;

    private VCSAdapter vcs;

    public SharedProject(IProject project, ISarosSession sarosSession) {
        this.sarosSession = sarosSession;
        this.project = project;

        boolean host = sarosSession.isHost();
        boolean useVersionControl = sarosSession.useVersionControl();
        if (host && useVersionControl) {
            initializeVCSInformation();
        }
    }

    protected void initializeVCSInformation() {
        vcs = VCSAdapterFactory.getAdapter(project);
        if (vcs == null)
            return;
        VCSProjectInformation projectInformation = vcs
            .getProjectInformation(project);
        vcsUrl = projectInformation.repositoryURL
            + projectInformation.projectPath;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isOpen() {
        return open;
    }

    public void setVCSAdapter(VCSAdapter vcs) {
        this.vcs = vcs;
    }

    public VCSAdapter getVCSAdapter() {
        return vcs;
    }
}
