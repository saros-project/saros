package de.fu_berlin.inf.dpp.project.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.SharedProject;

/**
 * Class responsible for mapping global project IDs to local {@link IProject}s,
 * {@link IProject}s to a List of {@link IResource}s and project IDs to local
 * {@link SharedProject}s.
 * 
 * The IDs are used to identify shared projects across the network, even when
 * the local names of a shared project are different. The ID is determined by
 * the project/file-host.
 */
class SarosProjectMapper {

    /**
     * mapping of project IDs --> {@link IProject}s
     */
    BiMap<String, IProject> idMapping = HashBiMap.create();

    /**
     * mapping of {@link User}s --> Lists of {@link IProject}s to determine the
     * one how shared a specific project. The User is needed to avoid mixtures
     * of project files from different users in one project.
     */
    HashMap<JID, ArrayList<IProject>> userToProjectIDMapping = new HashMap<JID, ArrayList<IProject>>();

    /**
     * mapping of {@link IProject}s --> Lists of project dependent
     * {@link IResource}s
     */
    HashMap<IProject, List<IResource>> resourceMapping = new HashMap<IProject, List<IResource>>();

    /**
     * mapping of project IDs --> {@link SharedProject}s
     */
    Map<String, SharedProject> sharedProjects = new HashMap<String, SharedProject>();

    /**
     * List of completely shared projects from project-host
     */
    List<IProject> completeProjectsList = new ArrayList<IProject>();

    public synchronized void addMapping(String id, IProject localProject,
        SharedProject sharedProject) {
        if (!idMapping.containsValue(localProject))
            idMapping.put(id, localProject);

        if (!sharedProjects.containsValue(sharedProject))
            sharedProjects.put(id, sharedProject);
    }

    public synchronized void addUserToProjectMapping(JID senderJID,
        IProject project, String projectID) {
        if (userToProjectIDMapping.containsValue(project))
            return;
        ArrayList<IProject> ownedProjects = userToProjectIDMapping
            .get(senderJID);
        if (ownedProjects == null)
            ownedProjects = new ArrayList<IProject>();
        ownedProjects.add(project);
        userToProjectIDMapping.put(senderJID, ownedProjects);
    }

    public synchronized void addResourceMapping(IProject localProject,
        List<IResource> dependentResources) {
        if (!completeProjectsList.contains(localProject)) {
            if (dependentResources != null) {
                resourceMapping.put(localProject, dependentResources);
            } else {
                resourceMapping.put(localProject, null);
                completeProjectsList.add(localProject);
            }
        }
    }

    public synchronized void removeMapping(String id) {
        idMapping.remove(id);
        sharedProjects.remove(id);
    }

    public synchronized void removeMapping(IProject localProject) {
        idMapping.inverse().remove(localProject);
    }

    public synchronized String getID(IProject localProject) {
        return idMapping.inverse().get(localProject);
    }

    public synchronized IProject getProject(String id) {
        return idMapping.get(id);
    }

    public synchronized boolean isShared(IResource localResource) {
        if (localResource instanceof IProject)
            return idMapping.containsValue(localResource);
        return isSharedResource(localResource);
    }

    public boolean isSharedResource(IResource resource) {
        if (resource == null)
            return false;
        IProject project = resource.getProject();
        if (idMapping.containsValue(project)) {
            if (isCompletelyShared(project))
                return true;
            List<IResource> resources = resourceMapping.get(project);
            return resources.contains(resource);
        }
        return false;
    }

    public synchronized Set<IProject> getProjects() {
        return Collections.unmodifiableSet(idMapping.values());
    }

    public synchronized List<IResource> getResources(IProject localProject) {
        return resourceMapping.get(localProject);
    }

    public synchronized Collection<List<IResource>> getResources() {
        return resourceMapping.values();
    }

    public synchronized int size() {
        return idMapping.size();
    }

    public synchronized SharedProject getSharedProject(String projectID) {
        return sharedProjects.get(projectID);
    }

    public synchronized List<SharedProject> getSharedProjects() {
        return (ArrayList<SharedProject>) sharedProjects.values();
    }

    public synchronized HashMap<IProject, List<IResource>> getProjectResourceMapping() {
        return resourceMapping;
    }

    public synchronized boolean isCompletelyShared(IProject project) {
        return completeProjectsList.contains(project);
    }

    public synchronized ArrayList<IProject> getOwnedProjectIDs(JID id) {
        return userToProjectIDMapping.get(id);
    }
}
