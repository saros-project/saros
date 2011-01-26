package de.fu_berlin.inf.dpp.project;

import static java.text.MessageFormat.format;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.subscribers.ISubscriberChangeEvent;
import org.eclipse.team.core.subscribers.ISubscriberChangeListener;
import org.eclipse.team.core.subscribers.Subscriber;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInfo;

/**
 * A SharedProject stores the state of a project (and its resources) shared in a
 * Saros session.<br>
 * <br>
 * Saros replicates a shared project, i.e. keeps copies of the project on the
 * peers in sync with the local project. A SharedProject represents the state
 * that these remote copies are supposed to be in. Whenever a user with
 * {@link User.Permission#WRITE_ACCESS} detects a mismatch between the IProject
 * and the corresponding SharedProject, we know that we need to send activities.<br>
 * <br>
 * Currently, the SharedProject is only accessed (updated) when the client has
 * {@link User.Permission#WRITE_ACCESS}.
 */
/*
 * What if SharedProject became a little smarter, what if SharedProject actually
 * represented the shared project, not only its state? E.g. if
 * SharedResourceManager detects that a file was added to the local project, it
 * notifies the corresponding SharedProject, which then creates and sends a
 * FileActivity if we have write access. On peers, the SharedProject would be
 * responsible for updating the local project upon receiving resource
 * activities, possibly even reverting changes.
 */
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
            return format("R[{0}@{1}]", vcsUrl.toString(),
                vcsRevision.toString());
        }
    }

    /** Maps the project relative path of a resource. */
    @SuppressWarnings("serial")
    protected Map<IPath, ResourceInfo> resourceMap = new HashMap<IPath, ResourceInfo>() {
        /**
         * This override is intended for debugging output only.
         */
        @Override
        public String toString() {
            Map<String, String> sorted = new TreeMap<String, String>();
            Set<Map.Entry<IPath, ResourceInfo>> entrySet = this.entrySet();
            for (Map.Entry<IPath, ResourceInfo> entry : entrySet) {
                sorted.put(entry.getKey().toString(), entry.getValue()
                    .toString());
            }
            String result = "";
            boolean addNewLine = false;
            String fullPath = project.getFullPath().toString() + "/";
            for (Map.Entry<String, String> entry : sorted.entrySet()) {
                if (addNewLine)
                    result += "\n";
                result += fullPath + entry.getKey() + " -> " + entry.getValue();
                addNewLine = true;
            }
            return result;
        }
    };

    protected UpdatableValue<Boolean> hasWriteAccess = new UpdatableValue<Boolean>(
        false);

    /**
     * Note that this listener is only registered if VCS support is enabled for
     * the session.
     */
    protected ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {
        @Override
        public void permissionChanged(User user) {
            boolean writeAccess = sarosSession.hasWriteAccess();
            if (!hasWriteAccess.update(writeAccess))
                return;
            if (writeAccess) {
                resourceMap.clear();
                initializeResources();
            }
        }
    };

    /** Used only for logging. */
    private ISubscriberChangeListener subscriberChangeListener = new ISubscriberChangeListener() {
        public void subscriberResourceChanged(ISubscriberChangeEvent[] deltas) {
            if (!log.isTraceEnabled())
                return;
            String result = "subscriberResourceChanged:\n";
            for (ISubscriberChangeEvent delta : deltas) {
                int flags = delta.getFlags();
                boolean syncChanged = (flags & ISubscriberChangeEvent.SYNC_CHANGED) != 0;
                if (flags == ISubscriberChangeEvent.NO_CHANGE)
                    result += "0";
                if (syncChanged)
                    result += "S";
                if ((flags & ISubscriberChangeEvent.ROOT_ADDED) != 0)
                    result += "+";
                if ((flags & ISubscriberChangeEvent.ROOT_REMOVED) != 0)
                    result += "-";
                IResource resource = delta.getResource();
                result += " " + resource.getFullPath().toPortableString();
                if (syncChanged) {
                    VCSAdapter vcs = VCSAdapter.getAdapter(resource
                        .getProject());
                    if (vcs.isManaged(resource)) {
                        VCSResourceInfo info = vcs.getResourceInfo(resource);
                        result += format(" ({0}:{1})", info.url, info.revision);
                    }
                }
                result += "\n";
            }
            log.trace(result);
        }
    };

    public SharedProject(IProject project, ISarosSession sarosSession) {
        assert sarosSession != null;
        assert project != null;
        assert project.isAccessible();

        this.sarosSession = sarosSession;
        this.project = project;

        projectIsOpen.update(project.isOpen());

        boolean hasWriteAccess = sarosSession.hasWriteAccess();
        this.hasWriteAccess.update(hasWriteAccess);

        if (sarosSession.useVersionControl()) {
            sarosSession.addListener(sharedProjectListener);
        }
        if (hasWriteAccess) {
            initializeResources();
        }
    }

    /** Initialize the ResourceInfo for every resource in the SharedProject. */
    protected void initializeResources() {
        try {
            addAll(project);
        } catch (CoreException e) {
            log.debug("Couldn't add all members of " + project.getName() + ".",
                e);
        }

        if (!sarosSession.useVersionControl())
            return;

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
        Set<IPath> paths = resourceMap.keySet();
        for (IPath path : paths) {
            IResource resource = project.findMember(path);
            assert resource != null : "Resource not found at " + path;
            VCSResourceInfo info = vcs.getResourceInfo(resource);

            updateVcsUrl(resource, info.url);
            updateRevision(resource, info.revision);
        }

        assert checkIntegrity();
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
        checkResource(resource);
        IPath path = resource.getProjectRelativePath();
        ResourceInfo resourceInfo = resourceMap.get(path);
        return resourceInfo.vcsUrl.update(newValue);
    }

    /** Updates the current VCS revision, and returns true if the value changed. */
    public boolean updateRevision(String newValue) {
        return updateRevision(project, newValue);
    }

    /** Updates the current VCS revision, and returns true if the value changed. */
    public boolean updateRevision(IResource resource, String newValue) {
        checkResource(resource);
        IPath path = resource.getProjectRelativePath();
        ResourceInfo resourceInfo = resourceMap.get(path);
        return resourceInfo.vcsRevision.update(newValue);
    }

    /**
     * @throws IllegalArgumentException
     *             if the resource is null or if this shared project doesn't
     *             contain it.
     */
    protected void checkResource(IResource resource) {
        if (resource == null)
            throw new IllegalArgumentException("Resource is null");
        else if (!contains(resource))
            throw new IllegalArgumentException("Resource not in map "
                + resource.toString());
    }

    /**
     * Updates if the SharedProject is currently open, and returns true if the
     * value changed.
     */
    public boolean updateProjectIsOpen(boolean newValue) {
        return projectIsOpen.update(newValue);
    }

    /** Removes the resource from the SharedProject. */
    public void remove(IResource resource) {
        checkResource(resource);
        IPath path = resource.getProjectRelativePath();
        resourceMap.remove(path);
    }

    /** Adds the resource to the SharedProject. */
    public void add(IResource resource) {
        if (resource == null)
            throw new IllegalArgumentException("Resource is null");
        if (resource.getProject() != project)
            throw new IllegalArgumentException(format(
                "resource {0} is not in project {1}", resource, project));
        ResourceInfo resourceInfo = new ResourceInfo(null, null);
        final IPath path = resource.getProjectRelativePath();
        resourceMap.put(path, resourceInfo);
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
     * Moves the resource within the shared project. Does <b>not</b> change the
     * resource information associated with the resource.
     */
    public void move(IResource resource, IPath oldFullPath) {
        IPath oldPath = oldFullPath.removeFirstSegments(1);
        assert containsKey(oldPath);
        resourceMap.remove(oldPath);
        add(resource);
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

    /**
     * Returns true if this SharedProject contains the resource.
     */
    public boolean contains(IResource resource) {
        final IPath projectRelativePath = resource.getProjectRelativePath();
        return containsKey(projectRelativePath);
    }

    private boolean containsKey(IPath path) {
        return resourceMap.containsKey(path);
    }

    /**
     * Returns true if the SharedProject is in sync with the local project, i.e.
     * if every resource in the IProject is in the SharedProject and vice versa,
     * and if the resource information associated with the resources are up to
     * date.<br>
     * It's intended for debugging only.<br>
     * TODO Do unit tests instead
     */
    public boolean checkIntegrity() {
        boolean illegalState = false;
        Set<Entry<IPath, ResourceInfo>> entrySet = resourceMap.entrySet();
        final VCSAdapter vcs = this.vcs.getValue();
        final String projectName = project.getName();
        for (Entry<IPath, ResourceInfo> entry : entrySet) {
            IPath path = entry.getKey();
            IResource resource = project.findMember(path);
            if (resource == null) {
                String msg = format("Resource {0} in map doesn''t exist"
                    + " in project {1}.", path, project.getName());
                logIllegalStateException(msg);
                illegalState = true;
                resourceMap.remove(path);
                continue;
            }
            assert resource.exists();
            if (vcs == null)
                continue;

            VCSResourceInfo expected = vcs.getResourceInfo(resource);
            ResourceInfo found = entry.getValue();
            String foundUrl = found.vcsUrl.getValue();
            String foundRevision = found.vcsRevision.getValue();
            if (found.vcsRevision.update(expected.revision)) {
                String msg = format(
                    "Revision out of sync on {0} in project {1} - found {2}, expected {3}.",
                    path, projectName, foundRevision, expected.revision);
                logIllegalStateException(msg);
                illegalState = true;
            }
            if (found.vcsUrl.update(expected.url)) {
                String msg = format(
                    "VCS URL out of sync on {0} in project {1}\n"
                        + "found \"{2}\"\nexpected \"{3}\".", path,
                    projectName, foundUrl, expected.url);
                logIllegalStateException(msg);
                illegalState = true;
            }
        }
        IResourceVisitor visitor = new IResourceVisitor() {
            boolean result = false;

            public boolean visit(IResource resource) {
                if (resource == null)
                    return result;
                IPath path = resource.getProjectRelativePath();
                assert path != null : "Path of " + resource + " is null!";
                if (!contains(resource)) {
                    final String msg = format(
                        "Resource map of {0} doesn''t contain {1}.",
                        projectName, path.toString());
                    logIllegalStateException(msg);
                    result = true;
                    add(resource);
                    if (vcs != null) {
                        final VCSResourceInfo info = vcs
                            .getResourceInfo(resource);
                        updateRevision(resource, info.revision);
                        updateVcsUrl(resource, info.url);
                    }
                }
                return true;
            }
        };
        try {
            project.accept(visitor, IResource.DEPTH_INFINITE,
                IContainer.EXCLUDE_DERIVED);
            illegalState = illegalState || visitor.visit(null);
        } catch (CoreException e) {
            return false;
        }
        return !illegalState;
    }

    private void logIllegalStateException(final String msg) {
        // Should never happen.
        final IllegalStateException e = new IllegalStateException();
        log.error(msg, e);
        // throw e;
    }

    public String getName() {
        return this.project.getName();
    }
}
