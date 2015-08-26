package de.fu_berlin.inf.dpp.core.project;

import de.fu_berlin.inf.dpp.filesystem.IContainer;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.session.AbstractSessionListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISessionListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.vcs.VCSProvider;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static java.text.MessageFormat.format;

/**
 * A SharedProject stores the state of a project (and its resources) shared in a
 * Saros session.<br>
 * <br>
 * Saros replicates a shared project, i.e. keeps copies of the project on the
 * peers in sync with the local project. A SharedProject represents the state
 * that these remote copies are supposed to be in. Whenever a user with
 * {@link Permission#WRITE_ACCESS} detects a mismatch between the IProject and
 * the corresponding SharedProject, we know that we need to send activities.<br>
 * <br>
 * Currently, the SharedProject is only accessed (updated) when the client has
 * {@link Permission#WRITE_ACCESS}.
 */
/*
 * What if SharedProject became a little smarter, what if SharedProject actually
 * represented the shared project, not only its state? E.g. if
 * SharedResourcesManager detects that a file was added to the local project, it
 * notifies the corresponding SharedProject, which then creates and sends a
 * FileActivity if we have write access. On peers, the SharedProject would be
 * responsible for updating the local project upon receiving resource
 * activities, possibly even reverting changes.
 */
public class SharedProject {
    private static final Logger log = Logger.getLogger(SharedProject.class);
    protected final ISarosSession sarosSession;
    protected final IProject project;
    /* Stored state values: */
    protected UpdatableValue<Boolean> projectIsOpen = new UpdatableValue<Boolean>(
        false);
    protected UpdatableValue<VCSProvider> vcs = new UpdatableValue<VCSProvider>(
        null);
    /**
     * Maps the project relative path of a resource.
     */
    @SuppressWarnings("serial")
    protected Map<IPath, ResourceInfo> resourceMap = new HashMap<IPath, ResourceInfo>() {
        /**
         * This override is intended for debugging output only.
         */
        @Override
        public String toString() {

            Map<String, String> sortedMap = new TreeMap<String, String>();

            for (Map.Entry<IPath, ResourceInfo> entry : this.entrySet())
                sortedMap.put(entry.getKey().toString(),
                    entry.getValue().toString());

            StringBuilder result = new StringBuilder(512);

            String fullPath =
                project.getFullPath().toString() + "/"; //$NON-NLS-1$

            for (Map.Entry<?, ?> entry : sortedMap.entrySet())
                result.append(fullPath).append(entry.getKey()).append(
                    " -> ") //$NON-NLS-1$
                    .append(entry.getValue()).append('\n');

            result.setLength(result.length() - 1);
            return result.toString();
        }
    };
    protected UpdatableValue<Boolean> hasWriteAccess = new UpdatableValue<Boolean>(
        false);
    /**
     * Note that this listener is only registered if VCS support is enabled for
     * the session.
     */
    protected ISessionListener sessionListener = new AbstractSessionListener() {
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
            sarosSession.addListener(sessionListener);
        }
        if (hasWriteAccess) {
            initializeResources();
        }
    }

    /**
     * Initialize the ResourceInfo for every resource in the SharedProject.
     */
    protected void initializeResources() {
        try {
            addAll(project);
        } catch (Exception e) {
            log.debug("Couldn't add all members of " + project.getName() + ".",
                e);
        }

        if (!sarosSession.useVersionControl())
            return;
    }

    /**
     * Updates the current VCSProvider, and returns true if the value changed.
     */
    public boolean updateVcs(VCSProvider newValue) {
        return vcs.update(newValue);
    }

    /**
     * Updates the current VCS URL, and returns true if the value changed.
     */
    public boolean updateVcsUrl(String newValue) {
        return updateVcsUrl(project, newValue);
    }

    /**
     * Updates the current VCS URL, and returns true if the value changed.
     */
    public boolean updateVcsUrl(IResource resource, String newValue) {
        checkResource(resource);
        IPath path = resource.getProjectRelativePath();
        ResourceInfo resourceInfo = resourceMap.get(path);
        return resourceInfo.vcsUrl.update(newValue);
    }

    /**
     * Updates the current VCS revision, and returns true if the value changed.
     */
    public boolean updateRevision(String newValue) {
        return updateRevision(project, newValue);
    }

    /**
     * Updates the current VCS revision, and returns true if the value changed.
     */
    public boolean updateRevision(IResource resource, String newValue) {
        checkResource(resource);
        IPath path = resource.getProjectRelativePath();
        ResourceInfo resourceInfo = resourceMap.get(path);
        return resourceInfo.vcsRevision.update(newValue);
    }

    /**
     * @throws IllegalArgumentException if the resource is null or if this shared project doesn't
     *                                  contain it.
     */
    protected void checkResource(IResource resource) {
        if (resource == null)
            throw new IllegalArgumentException(
                Messages.SharedProject_resource_is_null);
        else if (!contains(resource))
            throw new IllegalArgumentException(
                Messages.SharedProject_resource_not_in_map + resource.toString()
            );
    }

    /**
     * Updates if the SharedProject is currently open, and returns true if the
     * value changed.
     */
    public boolean updateProjectIsOpen(boolean newValue) {
        return projectIsOpen.update(newValue);
    }

    /**
     * Removes the resource from the SharedProject.
     */
    public void remove(IResource resource) {
        // checkResource(resource);
        if (contains(resource)) {
            IPath path = resource.getProjectRelativePath();
            resourceMap.remove(path);
        }
    }

    /**
     * Adds the resource to the SharedProject.
     */
    public void add(IResource resource) {
        if (resource == null)
            throw new IllegalArgumentException(
                Messages.SharedProject_resource_is_null);
        if (resource.getProject() != project)
            throw new IllegalArgumentException(
                format(Messages.SharedProject_resource_not_in_project, resource,
                    project));
        if (!contains(resource)) {
            ResourceInfo resourceInfo = new ResourceInfo(null, null);
            final IPath path = resource.getProjectRelativePath();
            resourceMap.put(path, resourceInfo);
        }
    }

    /**
     * Adds the resource, and recursively adds its decendants if it's a
     * IContainer.
     */
    protected void addAll(IResource resource) throws Exception {
        add(resource);
        if (resource instanceof IContainer) {
            IContainer container = (IContainer) resource;
            IResource[] members = container.members(IResource.NONE);
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
        // assert containsKey(oldPath);
        if (resourceMap.containsKey(oldPath))
            resourceMap.remove(oldPath);
        add(resource);
    }

    /**
     * Returns the current VCSProvider
     */
    public VCSProvider getVCSProvider() {
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

    public String getName() {
        return this.project.getName();
    }

    public void delete() {
        if (sarosSession.useVersionControl())
            sarosSession.removeListener(sessionListener);
    }

    /**
     * A value of type E with a convenient update method to check if the value
     * was changed.
     */
    static class UpdatableValue<E> {
        private E value;

        UpdatableValue(E value) {
            this.value = value;
        }

        /**
         * Updates the value, and returns true if the value changed.
         */
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
            return value == null ? "null" : value.toString(); //$NON-NLS-1$
        }
    }

    static class ResourceInfo {
        protected UpdatableValue<String> vcsUrl = new UpdatableValue<String>(
            null);
        protected UpdatableValue<String> vcsRevision = new UpdatableValue<String>(
            null);

        public ResourceInfo(String vcsUrl, String vcsRevision) {
            this.vcsUrl.update(vcsUrl);
            this.vcsRevision.update(vcsRevision);
        }

        @Override
        public String toString() {
            return format("R[{0}@{1}]", vcsUrl.toString(), //$NON-NLS-1$
                vcsRevision.toString());
        }
    }
}
