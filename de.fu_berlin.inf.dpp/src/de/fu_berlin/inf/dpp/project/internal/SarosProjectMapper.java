package de.fu_berlin.inf.dpp.project.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SharedProject;
import de.fu_berlin.inf.dpp.session.User;

/**
 * This class is responsible for mapping global project IDs to local
 * {@linkplain IProject projects}.
 * 
 * The IDs are used to identify shared projects across the network, even when
 * the local names of shared projects are different. The ID is determined by the
 * project/file-host.
 */

/*
 * FIXME: the partial sharing stuff should not be handled here but in the
 * SharedProject class
 */

class SarosProjectMapper {

    private static final Logger LOG = Logger
        .getLogger(SarosProjectMapper.class);

    /**
     * Mapping from project IDs to currently registered shared projects.
     */
    private Map<String, IProject> idToProjectMapping = new HashMap<String, IProject>();

    /**
     * Mapping from currently registered shared projects to their id's.
     */
    private Map<IProject, String> projectToIDMapping = new HashMap<IProject, String>();
    /**
     * Mapping of which user shared which project in the session. Needed for
     * partial sharing when the Needbased Feature is enabled.
     */

    /*
     * FIXME: the is NOT correctly transmitted during a project negotiation
     * 
     * e.g:
     * 
     * Session Alice-Bob Bob adds project FOO Alice invites Carl The project
     * owner for project FOO on Carls side is Alice but should be Bob.
     * 
     * FIXME: why is a JID used here ?
     */
    private HashMap<JID, List<IProject>> projectOwnershipMapping = new HashMap<JID, List<IProject>>();

    /**
     * Map containing the projects of the clients. Used by the host to determine
     * which Activities can be send.
     */
    private HashMap<User, List<String>> projectsOfUsers = new HashMap<User, List<String>>();

    /**
     * Map containing the partially shared resources for each shared project.
     * The value is <code>null</code> for completely shared projects.
     */
    private HashMap<IProject, Set<IResource>> partiallySharedResourceMapping = new HashMap<IProject, Set<IResource>>();

    /** Mapping from project IDs to {@link SharedProject} instances */
    /*
     * FIXME the naming is bad ... but as long as this "mapper" also tracks the
     * resources of partially shared projects their is no easy way to use
     * meaningful names without creating ambiguity.
     */
    private Map<String, SharedProject> sharedProjectResources = new HashMap<String, SharedProject>();

    /** Set containing the currently completely shared projects. */
    private Set<IProject> completelySharedProjects = new HashSet<IProject>();

    /** Set containing the currently partially shared projects. */
    private Set<IProject> partiallySharedProjects = new HashSet<IProject>();

    private final ISarosSession session;

    SarosProjectMapper(ISarosSession session) {
        this.session = session;
    }

    /**
     * Adds a project to the currently shared projects.
     * <p>
     * It is possible to "upgrade" a partially shared project to a completely
     * shared project by just adding the same project with the same ID again
     * that must now marked as not partially shared.
     * </p>
     * 
     * @param id
     *            the ID for the project
     * @param project
     *            the project to add
     * @param isPartially
     *            <code>true</code> if the project should be treated as a
     *            partially shared project, <code>false</code> if it should be
     *            treated as completely shared
     * 
     * @throws NullPointerException
     *             if the id or project is <code>null</code>
     * 
     * @throws IllegalStateException
     *             if the id is already in use or the project was already added
     */
    public synchronized void addProject(String id, IProject project,
        boolean isPartially) {
        boolean upgrade = false;

        if (id == null)
            throw new NullPointerException("id is null");

        if (project == null)
            throw new NullPointerException("project is null");

        String currentProjectID = projectToIDMapping.get(project);
        IProject currentProject = idToProjectMapping.get(id);

        if (currentProjectID != null && !id.equals(currentProjectID)) {
            throw new IllegalStateException("cannot assign ID " + id
                + " to project " + project
                + " because it is already registered with ID "
                + currentProjectID);
        }

        if (currentProject != null && !project.equals(currentProject)) {
            throw new IllegalStateException("ID " + id + " for project "
                + project + " is already used by project " + currentProject);
        }

        if (isPartially && partiallySharedProjects.contains(project))
            throw new IllegalStateException("project " + project
                + " is already partially shared");

        if (!isPartially && completelySharedProjects.contains(project))
            throw new IllegalStateException("project " + project
                + " is already completely shared");

        if (isPartially && completelySharedProjects.contains(project))
            throw new IllegalStateException(
                "project "
                    + project
                    + " is already completely shared (cannot downgrade a completely shared project)");

        if (!isPartially && partiallySharedProjects.contains(project)) {
            partiallySharedProjects.remove(project);
            upgrade = true;
        }

        if (isPartially)
            partiallySharedProjects.add(project);
        else
            completelySharedProjects.add(project);

        assert Collections.disjoint(completelySharedProjects,
            partiallySharedProjects);

        if (upgrade) {
            // release resources
            partiallySharedResourceMapping.put(project, null);

            LOG.debug("upgraded partially shared project " + project
                + " with ID " + id + " to a completely shared project");
            return;
        }

        idToProjectMapping.put(id, project);
        projectToIDMapping.put(project, id);

        if (isPartially)
            partiallySharedResourceMapping.put(project,
                new HashSet<IResource>());
        else
            partiallySharedResourceMapping.put(project, null);

        sharedProjectResources.put(id, new SharedProject(project, session));

        LOG.debug("added project " + project + " with ID " + id
            + " [completely shared:" + !isPartially + "]");
    }

    /**
     * Removes a project from the currently shared projects. Does nothing if the
     * project is not shared.
     * 
     * @param id
     *            the id of the project to remove
     */
    public synchronized void removeProject(String id) {
        IProject project = idToProjectMapping.get(id);

        if (project == null) {
            LOG.warn("could not remove project, no project is registerid with ID: "
                + id);
            return;
        }

        if (partiallySharedProjects.contains(project))
            partiallySharedProjects.remove(project);
        else
            completelySharedProjects.remove(project);

        idToProjectMapping.remove(id);
        projectToIDMapping.remove(project);
        partiallySharedResourceMapping.remove(project);
        SharedProject sharedProject = sharedProjectResources.remove(id);
        sharedProject.delete();

        LOG.debug("removed project " + project + " with ID " + id);

    }

    public synchronized void addOwnership(JID senderJID, IProject project) {

        List<IProject> ownedProjects = projectOwnershipMapping.get(senderJID);

        if (ownedProjects == null)
            ownedProjects = new ArrayList<IProject>();

        if (!ownedProjects.contains(project))
            ownedProjects.add(project);

        projectOwnershipMapping.put(senderJID, ownedProjects);
    }

    public synchronized void removeOwnership(JID senderJID, IProject project) {

        List<IProject> ownedProjects = projectOwnershipMapping.get(senderJID);

        if (ownedProjects == null)
            return;

        if (!ownedProjects.contains(project))
            return;

        ownedProjects.remove(project);

        projectOwnershipMapping.put(senderJID, ownedProjects);
    }

    public synchronized List<IProject> getOwnedProjects(JID jid) {

        List<IProject> result = new ArrayList<IProject>();

        List<IProject> ownedProjects = projectOwnershipMapping.get(jid);

        if (ownedProjects != null)
            result.addAll(ownedProjects);

        return result;
    }

    /**
     * Adds the given resources to a <b>partially</b> shared project.
     * 
     * @param project
     *            a project that was added as a partially shared project
     * @param resources
     *            the resources to add
     */

    /*
     * TODO needs proper sync. in the SarosSession class
     * 
     * @throws IllegalStateException if the project is completely or not shared
     * at all
     */
    public synchronized void addResources(IProject project,
        Collection<? extends IResource> resources) {

        if (projectToIDMapping.get(project) == null) {
            LOG.warn("could not add resources to project " + project
                + " because it is not shared");
            // throw new IllegalStateException(
            // "could not add resources to project " + project
            // + " because it is not shared");
            return;
        }

        if (completelySharedProjects.contains(project)) {
            LOG.warn("cannot add resources to completely shared project: "
                + project);
            // throw new IllegalStateException(
            // "cannot add resources to completely shared project: " + project);
            return;
        }

        Set<IResource> partiallySharedResources = partiallySharedResourceMapping
            .get(project);

        if (partiallySharedResources.isEmpty()) {
            partiallySharedResources = new HashSet<IResource>(Math.max(1024,
                (resources.size() * 3) / 2));

            partiallySharedResourceMapping.put(project,
                partiallySharedResources);
        }

        partiallySharedResources.addAll(resources);
    }

    /**
     * Removes the given resources from a <b>partially</b> shared project.
     * 
     * @param project
     *            a project that was added as a partially shared project
     * @param resources
     *            the resources to remove
     */
    /*
     * TODO needs proper sync. in the SarosSession class
     * 
     * @throws IllegalStateException if the project is completely or not shared
     * at all
     */
    public synchronized void removeResources(IProject project,
        Collection<? extends IResource> resources) {

        if (projectToIDMapping.get(project) == null) {
            LOG.warn("could not remove resources from project " + project
                + " because it is not shared");
            // throw new IllegalStateException(
            // "could not remove resources from project " + project
            // + " because it is not shared");
            return;
        }

        if (completelySharedProjects.contains(project)) {
            LOG.warn("cannot remove resources from completely shared project: "
                + project);
            // throw new IllegalStateException(
            // "cannot remove resources from completely shared project: " +
            // project);
            return;
        }

        Set<IResource> partiallySharedResources = partiallySharedResourceMapping
            .get(project);

        partiallySharedResources.removeAll(resources);
    }

    /**
     * Atomically removes and adds resources. The resources to remove will be
     * removed first before the resources to add will be added.
     * 
     * @param project
     *            a project that was added as a partially shared project
     * @param resourcesToRemove
     *            the resources to remove
     * @param resourcesToAdd
     *            the resources to add
     */
    /*
     * TODO needs proper sync. in the SarosSession class
     * 
     * @throws IllegalStateException if the project is completely or not shared
     * at all
     */
    public synchronized void removeAndAddResources(IProject project,
        Collection<? extends IResource> resourcesToRemove,
        Collection<? extends IResource> resourcesToAdd) {

        removeResources(project, resourcesToRemove);
        addResources(project, resourcesToAdd);
    }

    /**
     * Returns the project ID for the shared project.
     * 
     * @param project
     *            the project to lookup the ID for
     * @return the ID for the shared project or <code>null</code> if the project
     *         is not shared
     */
    public synchronized String getID(IProject project) {
        return projectToIDMapping.get(project);
    }

    /**
     * Returns the shared project for the given ID.
     * 
     * @param id
     * @return the shared project for the given ID or <code>null</code> if no
     *         shared project is registered with this ID
     */
    public synchronized IProject getProject(String id) {
        return idToProjectMapping.get(id);
    }

    /**
     * Returns if the given resource is included in one of the currently shared
     * projects.
     * 
     * @param resource
     * @return
     */
    public synchronized boolean isShared(IResource resource) {
        if (resource == null)
            return false;

        if (resource.getType() == IResource.PROJECT)
            return idToProjectMapping.containsValue(resource);

        IProject project = resource.getProject();

        if (!idToProjectMapping.containsValue(project))
            return false;

        if (isCompletelyShared(project))
            // TODO how should partial sharing handle this case ?
            return !resource.isDerived(true);
        else
            return partiallySharedResourceMapping.get(project).contains(
                resource);
    }

    /**
     * Returns the currently shared projects.
     * 
     * @return
     */
    public synchronized Set<IProject> getProjects() {
        return new HashSet<IProject>(idToProjectMapping.values());
    }

    /**
     * Returns all resources from all partially shared projects.
     * 
     * @return
     */
    public synchronized List<IResource> getPartiallySharedResources() {

        int size = 0;

        for (Set<IResource> resources : partiallySharedResourceMapping.values())
            if (resources != null)
                size += resources.size();

        List<IResource> partiallySharedResources = new ArrayList<IResource>(
            size);

        for (Set<IResource> resources : partiallySharedResourceMapping.values())
            if (resources != null)
                partiallySharedResources.addAll(resources);

        return partiallySharedResources;
    }

    /**
     * Returns the current amount of shared projects.
     * 
     * @return
     */
    public synchronized int size() {
        return idToProjectMapping.size();
    }

    public synchronized SharedProject getSharedProject(String projectID) {
        return sharedProjectResources.get(projectID);
    }

    public synchronized List<SharedProject> getSharedProjects() {
        return new ArrayList<SharedProject>(sharedProjectResources.values());
    }

    /**
     * Returns a mapping for each shared project and its containing resources.
     * The resource list is <b>always</b> <code>null</code> for completely
     * shared projects.
     * 
     * @return
     */
    public synchronized Map<IProject, List<IResource>> getProjectResourceMapping() {

        Map<IProject, List<IResource>> result = new HashMap<IProject, List<IResource>>();

        for (Map.Entry<IProject, Set<IResource>> entry : partiallySharedResourceMapping
            .entrySet()) {

            List<IResource> partiallySharedResources = null;

            if (entry.getValue() != null)
                partiallySharedResources = new ArrayList<IResource>(
                    entry.getValue());

            result.put(entry.getKey(), partiallySharedResources);
        }

        return result;
    }

    /**
     * Checks if a project is completely shared.
     * 
     * @param project
     *            the project to check
     * @return <code>true</code> if the project is completely shared,
     *         <code>false</code> if the project is not or partially shared
     */
    public synchronized boolean isCompletelyShared(IProject project) {
        return completelySharedProjects.contains(project);
    }

    /**
     * Checks if a project is partially shared.
     * 
     * @param project
     *            the project to check
     * @return <code>true</code> if the project is partially shared,
     *         <code>false</code> if the project is not or completely shared
     */
    public synchronized boolean isPartiallyShared(IProject project) {
        return partiallySharedProjects.contains(project);
    }

    /**
     * Checks if the given user already has the given project.
     * 
     * @param user
     *            The user to be checked
     * @param project
     *            The project to be checked
     */
    public synchronized boolean userHasProject(User user, IProject project) {
        if (projectsOfUsers.containsKey(user)) {
            return projectsOfUsers.get(user).contains(getID(project));
        }
        return false;
    }

    /**
     * Adds all missing projects to the projects of the given user. This should
     * be called once the user started queuing.
     * 
     * @param user
     */
    public synchronized void addMissingProjectsToUser(User user) {
        List<String> projects = new ArrayList<String>();
        for (String project : idToProjectMapping.keySet()) {
            projects.add(project);
        }

        this.projectsOfUsers.put(user, projects);
    }

    /**
     * Removes the user-project mapping of the user that left the session.
     */
    public void userLeft(User user) {
        projectsOfUsers.remove(user);
    }
}
