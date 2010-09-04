package de.fu_berlin.inf.dpp.project;

import org.eclipse.core.resources.IProject;

import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSAdapterFactory;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInformation;

public class SharedProject {
    // private static final Logger log = Logger.getLogger(SharedProject.class);

    protected ISarosSession sarosSession;

    protected IProject project;

    protected boolean open;

    protected String vcsUrl;

    protected VCSAdapter vcs;

    protected String vcsRevision;

    public SharedProject(IProject project, ISarosSession sarosSession) {
        this.sarosSession = sarosSession;
        this.project = project;

        open = project.isOpen();
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
        VCSResourceInformation info = vcs.getResourceInformation(project);
        vcsUrl = info.repositoryRoot + info.path;
        vcsRevision = info.revision;
    }

    /**
     * Updates the VCS URL.
     * 
     * @return true if the value was changed.
     */
    public boolean updateVcsUrl(String newValue) {
        if (newValue == null) {
            if (vcsUrl == null)
                return false;
            vcsUrl = null;
            return true;
        }
        if (newValue.equals(vcsUrl)) {
            return false;
        }
        vcsUrl = newValue;
        return true;
    }

    public boolean updateRevision(String newValue) {
        if (newValue == null) {
            if (vcsRevision == null)
                return false;
            vcsRevision = null;
            return true;
        }
        if (newValue.equals(vcsRevision)) {
            return false;
        }
        vcsRevision = newValue;
        return true;
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
