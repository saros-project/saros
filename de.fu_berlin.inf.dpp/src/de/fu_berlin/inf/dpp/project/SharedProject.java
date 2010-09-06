package de.fu_berlin.inf.dpp.project;

import org.eclipse.core.resources.IProject;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSAdapterFactory;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInformation;

public class SharedProject {
    static class UpdatableValue<E> {
        private E value;

        UpdatableValue(E value) {
            this.value = value;
        }

        public boolean update(E newValue) {
            if (newValue == null) {
                if (value == null)
                    return false;
                value = null;
                return true;
            }
            if (newValue.equals(value)) {
                return false;
            }
            value = newValue;
            return true;
        }

        public E value() {
            return value;
        }
    }

    protected final ISarosSession sarosSession;

    protected final IProject project;

    protected boolean open;

    protected UpdatableValue<VCSAdapter> vcs;

    protected UpdatableValue<String> vcsUrl;

    protected UpdatableValue<String> vcsRevision;

    protected ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {
        @Override
        public void roleChanged(User user) {
            if (sarosSession.isDriver())
                initializeVCSInformation();
        }
    };

    public SharedProject(IProject project, ISarosSession sarosSession) {
        assert sarosSession != null;
        assert project != null;

        this.sarosSession = sarosSession;
        this.project = project;

        open = project.isOpen();
        boolean useVersionControl = sarosSession.useVersionControl();
        if (useVersionControl) {
            sarosSession.addListener(sharedProjectListener);
            if (sarosSession.isDriver())
                initializeVCSInformation();
        }
    }

    protected void initializeVCSInformation() {
        VCSAdapter vcs = VCSAdapterFactory.getAdapter(project);
        this.vcs = new UpdatableValue<VCSAdapter>(vcs);
        if (vcs == null)
            return;
        VCSResourceInformation info = vcs.getResourceInformation(project);
        vcsUrl = new UpdatableValue<String>(info.repositoryRoot + info.path);
        vcsRevision = new UpdatableValue<String>(info.revision);
    }

    /**
     * Updates the VCS.
     * 
     * @return true if the value was changed.
     */
    public boolean updateVcs(VCSAdapter newValue) {
        return vcs.update(newValue);
    }

    /**
     * Updates the VCS URL.
     * 
     * @return true if the value was changed.
     */
    public boolean updateVcsUrl(String newValue) {
        return vcsUrl.update(newValue);
    }

    public boolean updateRevision(String newValue) {
        return vcsRevision.update(newValue);
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isOpen() {
        return open;
    }

    public VCSAdapter getVCSAdapter() {
        return vcs.value();
    }

    // TODO find a less stupid name ._.
    public boolean isRepresentationOf(IProject project) {
        return this.project.equals(project);
    }
}
