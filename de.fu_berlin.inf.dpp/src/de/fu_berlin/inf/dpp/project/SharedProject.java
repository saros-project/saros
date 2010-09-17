package de.fu_berlin.inf.dpp.project;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.subscribers.ISubscriberChangeEvent;
import org.eclipse.team.core.subscribers.ISubscriberChangeListener;
import org.eclipse.team.core.subscribers.Subscriber;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInformation;

/**
 * A SharedProject stores the state of a project (and its resources) shared in a
 * Saros session.<br>
 * <br>
 * Saros replicates a shared project, i.e. keeps copies of the project on the
 * peers in sync with the local project. A SharedProject represents the state
 * that these remote copies are supposed to be in. Whenever we detect a mismatch
 * between the IProject and the corresponding SharedProject, we know that we
 * need to send activities.
 */
// TODO Rename to SharedProjectState?
public class SharedProject {
    /**
     * A value of type E with a convenient update method to check if the value
     * was changed.
     */
    static class UpdatableValue<E> {
        private E value;

        UpdatableValue(E value) {
            this.value = value;
        }

        /** Updates the value, and returns true if the value changed. */
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

        public E getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value == null ? null : value.toString();
        }
    }

    private static final Logger log = Logger.getLogger(SharedProject.class);

    protected final ISarosSession sarosSession;

    protected final IProject project;

    /* Stored state values: */
    protected UpdatableValue<Boolean> projectIsOpen = new UpdatableValue<Boolean>(
        false);

    protected UpdatableValue<VCSAdapter> vcs = new UpdatableValue<VCSAdapter>(
        null);

    class ResourceInfo {
        public ResourceInfo(String vcsUrl, String vcsRevision) {
            this.vcsUrl.update(vcsUrl);
            this.vcsRevision.update(vcsRevision);
        }

        protected UpdatableValue<String> vcsUrl = new UpdatableValue<String>(
            null);

        protected UpdatableValue<String> vcsRevision = new UpdatableValue<String>(
            null);

        @Override
        public String toString() {
            return MessageFormat.format("R[{0}, {1}]", vcsUrl.toString(),
                vcsRevision.toString());
        }
    }

    /** Maps the full path of a resource. */
    protected Map<IPath, ResourceInfo> resourceMap = new HashMap<IPath, SharedProject.ResourceInfo>();

    protected UpdatableValue<Boolean> isDriver = new UpdatableValue<Boolean>(
        false);

    protected ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {
        @Override
        public void roleChanged(User user) {
            boolean driver = sarosSession.isDriver();
            // The order of the operands is supposed to be like that, we want to
            // call update even if driver is false.
            if (isDriver.update(driver) && driver) {
                initializeVCSInformation();
            }
        }
    };

    /** Used only for logging. */
    private ISubscriberChangeListener subscriberChangeListener = new ISubscriberChangeListener() {
        public void subscriberResourceChanged(ISubscriberChangeEvent[] deltas) {
            String s = "subscriberResourceChanged:\n";
            for (ISubscriberChangeEvent delta : deltas) {
                int flags = delta.getFlags();
                boolean syncChanged = (flags & ISubscriberChangeEvent.SYNC_CHANGED) != 0;
                if (flags == ISubscriberChangeEvent.NO_CHANGE)
                    s += "0";
                if (syncChanged)
                    s += "S";
                if ((flags & ISubscriberChangeEvent.ROOT_ADDED) != 0)
                    s += "+";
                if ((flags & ISubscriberChangeEvent.ROOT_REMOVED) != 0)
                    s += "-";
                IResource resource = delta.getResource();
                s += " " + resource.getFullPath().toPortableString();
                if (syncChanged) {
                    VCSAdapter vcs = VCSAdapter.getAdapter(resource
                        .getProject());
                    if (vcs.isManaged(resource)) {
                        VCSResourceInformation info = vcs
                            .getResourceInformation(resource);
                        s += MessageFormat.format(" ({0} {1}:{2})",
                            info.repositoryRoot, info.path, info.revision);
                    }
                }
                s += "\n";
            }
            log.trace(s);
        }
    };

    public SharedProject(IProject project, ISarosSession sarosSession) {
        assert sarosSession != null;
        assert project != null;
        assert project.isOpen();

        this.sarosSession = sarosSession;
        this.project = project;

        try {
            addAll(project);
        } catch (CoreException e) {
            log.debug("Couldn't add all members of " + project.getName() + ".",
                e);
        }

        projectIsOpen = new UpdatableValue<Boolean>(project.isOpen());
        boolean useVersionControl = sarosSession.useVersionControl();
        if (useVersionControl) {
            sarosSession.addListener(sharedProjectListener);
            boolean isDriver = sarosSession.isDriver();
            this.isDriver.update(isDriver);
            if (isDriver)
                initializeVCSInformation();
        }
    }

    protected void initializeVCSInformation() {
        VCSAdapter vcs = VCSAdapter.getAdapter(project);
        this.vcs.update(vcs);
        if (vcs == null)
            return;
        if (log.isTraceEnabled()) {
            RepositoryProvider provider = RepositoryProvider
                .getProvider(project);
            Subscriber subscriber = provider.getSubscriber();
            if (subscriber != null)
                subscriber.addListener(subscriberChangeListener);
            else
                log.error("Could not add this SharedProject as an ISubscriberChangeListener.");
        }
        Set<IPath> keySet = resourceMap.keySet();
        for (IPath path : keySet) {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IWorkspaceRoot root = workspace.getRoot();
            IResource resource = root.findMember(path);
            assert resource != null : "Resource not found";
            VCSResourceInformation info = vcs.getResourceInformation(resource);
            updateVcsUrl(resource, info.repositoryRoot + info.path);
            updateRevision(resource, info.revision);
        }
    }

    /** Updates the current VCSAdapter, and returns true if the value changed. */
    public boolean updateVcs(VCSAdapter newValue) {
        return vcs.update(newValue);
    }

    /** Updates the current VCS URL, and returns true if the value changed. */
    public boolean updateVcsUrl(String newValue) {
        return updateVcsUrl(project, newValue);
    }

    /** Updates the current VCS URL, and returns true if the value changed. */
    public boolean updateVcsUrl(IResource resource, String newValue) {
        IPath fullPath = resource.getFullPath();
        ResourceInfo resourceInfo = resourceMap.get(fullPath);
        assert resourceInfo != null : fullPath.toString() + " not found";
        return resourceInfo.vcsUrl.update(newValue);
    }

    /** Updates the current VCS revision, and returns true if the value changed. */
    public boolean updateRevision(String newValue) {
        return updateRevision(project, newValue);
    }

    /** Updates the current VCS revision, and returns true if the value changed. */
    public boolean updateRevision(IResource resource, String newValue) {
        IPath fullPath = resource.getFullPath();
        ResourceInfo resourceInfo = resourceMap.get(fullPath);
        assert resourceInfo != null : fullPath.toString() + " not found";
        return resourceInfo.vcsRevision.update(newValue);
    }

    /**
     * Updates if the project is currently open, and returns true if the value
     * changed.
     */
    public boolean updateProjectIsOpen(boolean newValue) {
        return projectIsOpen.update(newValue);
    }

    /** Removes the resource from the project. */
    // TODO This name might be confusing since the resource is not actually
    // deleted.
    public void remove(IResource resource) {
        assert resource.getProject() == project;
        IPath fullPath = resource.getFullPath();
        resourceMap.remove(fullPath);
    }

    /** Adds the resource to the project. */
    public void add(IResource resource) {
        assert resource.getProject() == project;
        ResourceInfo resourceInfo = new ResourceInfo(null, null);
        resourceMap.put(resource.getFullPath(), resourceInfo);
    }

    /**
     * Adds the resource, and recursively adds its decendants if it's a
     * IContainer.
     */
    protected void addAll(IResource resource) throws CoreException {
        add(resource);
        if (resource instanceof IContainer) {
            IContainer container = (IContainer) resource;
            IResource[] members = container.members(IContainer.EXCLUDE_DERIVED);
            for (IResource child : members) {
                addAll(child);
            }
        }
    }

    /**
     * Moves the resource to the project. Does <b>not</b> change the resource
     * information associated with the resource.
     */
    // TODO Update information associated with the resource?
    public void move(IPath oldFullPath, IPath newFullPath) {
        resourceMap.put(newFullPath, resourceMap.remove(oldFullPath));
    }

    /** Returns the current VCSAdapter. */
    public VCSAdapter getVCSAdapter() {
        return vcs.getValue();
    }

    /**
     * Returns true if this SharedProject tracks the state of the project.
     */
    public boolean belongsTo(IProject project) {
        return this.project.equals(project);
    }
}
