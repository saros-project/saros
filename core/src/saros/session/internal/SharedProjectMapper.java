package saros.session.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.session.User;

/**
 * This class is responsible for mapping global project IDs to local {@linkplain IProject projects},
 * as well as storing which resources are shared from each project. On the host, it also tracks
 * which users have already received which shared projects.
 *
 * <p>The project IDs are used to identify shared projects across the network, even when the local
 * names of shared projects are different. The ID is determined by the project/file-host.
 */
class SharedProjectMapper {

  private static final Logger LOG = Logger.getLogger(SharedProjectMapper.class);

  /** Mapping from project IDs to currently registered shared projects. */
  private Map<String, IProject> idToProjectMapping = new HashMap<String, IProject>();

  /** Mapping from currently registered shared projects to their id's. */
  private Map<IProject, String> projectToIDMapping = new HashMap<IProject, String>();

  /**
   * Map for storing which clients have which projects. Used by the host to determine who can
   * currently process an activity related to a particular project. (Non-hosts don't maintain this
   * map.)
   */
  private Map<User, List<String>> projectsOfUsers = new HashMap<User, List<String>>();

  /**
   * Map for storing the set of resources shared for each shared project. Maps to <code>null</code>
   * for completely shared projects.
   */
  private Map<IProject, Set<IResource>> partiallySharedResourceMapping =
      new HashMap<IProject, Set<IResource>>();

  /** Set containing the currently completely shared projects. */
  private Set<IProject> completelySharedProjects = new HashSet<IProject>();

  /** Set containing the currently partially shared projects. */
  private Set<IProject> partiallySharedProjects = new HashSet<IProject>();

  /**
   * Adds a project to the set of currently shared projects.
   *
   * <p>It is possible to "upgrade" a partially shared project to a completely shared project by
   * just adding the same project with the same ID again that must now marked as not partially
   * shared.
   *
   * @param id the ID for the project
   * @param project the project to add
   * @param isPartially <code>true</code> if the project should be treated as a partially shared
   *     project, <code>false</code> if it should be treated as completely shared
   * @throws NullPointerException if the ID or project is <code>null</code>
   * @throws IllegalStateException if the ID is already in use or the project was already added
   */
  public synchronized void addProject(String id, IProject project, boolean isPartially) {
    boolean upgrade = false;

    if (id == null) throw new NullPointerException("ID is null");

    if (project == null) throw new NullPointerException("project is null");

    String currentProjectID = projectToIDMapping.get(project);
    IProject currentProject = idToProjectMapping.get(id);

    if (currentProjectID != null && !id.equals(currentProjectID)) {
      throw new IllegalStateException(
          "cannot assign ID "
              + id
              + " to project "
              + project
              + " because it is already registered with ID "
              + currentProjectID);
    }

    if (currentProject != null && !project.equals(currentProject)) {
      throw new IllegalStateException(
          "ID " + id + " for project " + project + " is already used by project " + currentProject);
    }

    if (isPartially && partiallySharedProjects.contains(project))
      throw new IllegalStateException("project " + project + " is already partially shared");

    if (!isPartially && completelySharedProjects.contains(project))
      throw new IllegalStateException("project " + project + " is already completely shared");

    if (isPartially && completelySharedProjects.contains(project))
      throw new IllegalStateException(
          "project "
              + project
              + " is already completely shared (cannot downgrade a completely shared project)");

    if (!isPartially && partiallySharedProjects.contains(project)) {
      partiallySharedProjects.remove(project);
      upgrade = true;
    }

    if (isPartially) partiallySharedProjects.add(project);
    else completelySharedProjects.add(project);

    assert Collections.disjoint(completelySharedProjects, partiallySharedProjects);

    if (upgrade) {
      // release resources
      partiallySharedResourceMapping.put(project, null);

      LOG.debug(
          "upgraded partially shared project "
              + project
              + " with ID "
              + id
              + " to a completely shared project");
      return;
    }

    idToProjectMapping.put(id, project);
    projectToIDMapping.put(project, id);

    if (isPartially) partiallySharedResourceMapping.put(project, new HashSet<IResource>());
    else partiallySharedResourceMapping.put(project, null);

    LOG.debug(
        "added project "
            + project
            + " with ID "
            + id
            + " [completely shared:"
            + !isPartially
            + "]");
  }

  /**
   * Removes a project from the set of currently shared projects. Does nothing if the project is not
   * shared.
   *
   * @param id the ID of the project to remove
   */
  public synchronized void removeProject(String id) {
    IProject project = idToProjectMapping.get(id);

    if (project == null) {
      LOG.warn("could not remove project, no project is registerid with ID: " + id);
      return;
    }

    if (partiallySharedProjects.contains(project)) partiallySharedProjects.remove(project);
    else completelySharedProjects.remove(project);

    idToProjectMapping.remove(id);
    projectToIDMapping.remove(project);
    partiallySharedResourceMapping.remove(project);

    LOG.debug("removed project " + project + " with ID " + id);
  }

  /**
   * Adds the given resources to a <b>partially</b> shared project.
   *
   * @param project a project that was added as a partially shared project
   * @param resources the resources to add
   */
  /*
   * TODO needs proper sync. in the SarosSession class
   *
   * @throws IllegalStateException if the project is completely or not shared
   * at all
   */
  public synchronized void addResources(
      IProject project, Collection<? extends IResource> resources) {

    if (projectToIDMapping.get(project) == null) {
      LOG.warn("could not add resources to project " + project + " because it is not shared");
      // throw new IllegalStateException(
      // "could not add resources to project " + project
      // + " because it is not shared");
      return;
    }

    if (completelySharedProjects.contains(project)) {
      LOG.warn("cannot add resources to completely shared project: " + project);
      // throw new IllegalStateException(
      // "cannot add resources to completely shared project: " + project);
      return;
    }

    Set<IResource> partiallySharedResources = partiallySharedResourceMapping.get(project);

    if (partiallySharedResources.isEmpty()) {
      partiallySharedResources = new HashSet<IResource>(Math.max(1024, (resources.size() * 3) / 2));

      partiallySharedResourceMapping.put(project, partiallySharedResources);
    }

    partiallySharedResources.addAll(resources);
  }

  /**
   * Removes the given resources from a <b>partially</b> shared project.
   *
   * @param project a project that was added as a partially shared project
   * @param resources the resources to remove
   */
  /*
   * TODO needs proper sync. in the SarosSession class
   *
   * @throws IllegalStateException if the project is completely or not shared
   * at all
   */
  public synchronized void removeResources(
      IProject project, Collection<? extends IResource> resources) {

    if (projectToIDMapping.get(project) == null) {
      LOG.warn("could not remove resources from project " + project + " because it is not shared");
      // throw new IllegalStateException(
      // "could not remove resources from project " + project
      // + " because it is not shared");
      return;
    }

    if (completelySharedProjects.contains(project)) {
      LOG.warn("cannot remove resources from completely shared project: " + project);
      // throw new IllegalStateException(
      // "cannot remove resources from completely shared project: " +
      // project);
      return;
    }

    Set<IResource> partiallySharedResources = partiallySharedResourceMapping.get(project);

    partiallySharedResources.removeAll(resources);
  }

  /**
   * Atomically removes and adds resources. The resources to remove will be removed first before the
   * resources to add will be added.
   *
   * @param project a project that was added as a partially shared project
   * @param resourcesToRemove the resources to remove
   * @param resourcesToAdd the resources to add
   */
  /*
   * TODO needs proper sync. in the SarosSession class
   *
   * @throws IllegalStateException if the project is completely or not shared
   * at all
   */
  public synchronized void removeAndAddResources(
      IProject project,
      Collection<? extends IResource> resourcesToRemove,
      Collection<? extends IResource> resourcesToAdd) {

    removeResources(project, resourcesToRemove);
    addResources(project, resourcesToAdd);
  }

  /**
   * Returns the ID assigned to the given shared project.
   *
   * @param project the project to look up the ID for
   * @return the shared project's ID or <code>null</code> if the project is not shared
   */
  public synchronized String getID(IProject project) {
    return projectToIDMapping.get(project);
  }

  /**
   * Returns the shared project with the given ID.
   *
   * @param id the ID to look up the project for
   * @return the shared project for the given ID or <code>null</code> if no shared project is
   *     registered with this ID
   */
  public synchronized IProject getProject(String id) {
    return idToProjectMapping.get(id);
  }

  /**
   * Returns whether the given resource is included in one of the currently shared projects.
   *
   * @param resource the resource to check for
   * @return <code>true</code> if the resource is shared, <code>false</code> otherwise
   */
  public synchronized boolean isShared(IResource resource) {
    if (resource == null) return false;

    if (resource.getType() == IResource.PROJECT) return idToProjectMapping.containsValue(resource);

    IProject project = resource.getProject();

    if (!idToProjectMapping.containsValue(project)) return false;

    if (isCompletelyShared(project))
      // TODO how should partial sharing handle this case ?
      return !resource.isDerived(true);
    else return partiallySharedResourceMapping.get(project).contains(resource);
  }

  /**
   * Returns the currently shared projects.
   *
   * @return a newly created {@link Set} with the shared projects
   */
  public synchronized Set<IProject> getProjects() {
    return new HashSet<IProject>(idToProjectMapping.values());
  }

  /**
   * Returns all resources from all partially shared projects.
   *
   * @return a newly created {@link List} with all of the partially shared projects' resources
   */
  public synchronized List<IResource> getPartiallySharedResources() {

    int size = 0;

    for (Set<IResource> resources : partiallySharedResourceMapping.values())
      if (resources != null) size += resources.size();

    List<IResource> partiallySharedResources = new ArrayList<IResource>(size);

    for (Set<IResource> resources : partiallySharedResourceMapping.values())
      if (resources != null) partiallySharedResources.addAll(resources);

    return partiallySharedResources;
  }

  /**
   * Returns the current number of shared projects.
   *
   * @return number of shared projects
   */
  public synchronized int size() {
    return idToProjectMapping.size();
  }

  /**
   * Returns a mapping for each shared project and its containing resources. The resource list is
   * <b>always</b> <code>null</code> for completely shared projects.
   *
   * @return a map from project to resource list (partially shared) or <code>null</code> (completely
   *     shared)
   */
  public synchronized Map<IProject, List<IResource>> getProjectResourceMapping() {

    Map<IProject, List<IResource>> result = new HashMap<IProject, List<IResource>>();

    for (Map.Entry<IProject, Set<IResource>> entry : partiallySharedResourceMapping.entrySet()) {

      List<IResource> partiallySharedResources = null;

      if (entry.getValue() != null)
        partiallySharedResources = new ArrayList<IResource>(entry.getValue());

      result.put(entry.getKey(), partiallySharedResources);
    }

    return result;
  }

  /**
   * Checks whether a project is completely shared.
   *
   * @param project the project to check for
   * @return <code>true</code> if the project is completely shared, <code>false</code> if the
   *     project is not or partially shared
   */
  public synchronized boolean isCompletelyShared(IProject project) {
    return completelySharedProjects.contains(project);
  }

  /**
   * Checks if a project is partially shared.
   *
   * @param project the project to check
   * @return <code>true</code> if the project is partially shared, <code>false</code> if the project
   *     is not or completely shared
   */
  public synchronized boolean isPartiallyShared(IProject project) {
    return partiallySharedProjects.contains(project);
  }

  /**
   * Checks if the given user already has the given project, and can thus process activities related
   * to that project.
   *
   * <p>This method should only be called by the session's host.
   *
   * @param user The user to be checked
   * @param project The project to be checked
   * @return <code>true</code> if the user currently has the project, <code>false</code> if not
   */
  public synchronized boolean userHasProject(User user, IProject project) {
    if (projectsOfUsers.containsKey(user)) {
      return projectsOfUsers.get(user).contains(getID(project));
    }
    return false;
  }

  /**
   * Tells the mapper that the given user now has all currently shared projects.
   *
   * <p>This method should only be called by the session's host.
   *
   * @param user user who now has all projects
   * @see #userHasProject(User, IProject)
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
   *
   * <p>This method should only be called by the session's host.
   *
   * @param user user who left the session
   * @see #userHasProject(User, IProject)
   */
  public void userLeft(User user) {
    projectsOfUsers.remove(user);
  }
}
