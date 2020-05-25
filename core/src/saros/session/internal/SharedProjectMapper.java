package saros.session.internal;

import static saros.filesystem.IResource.Type.REFERENCE_POINT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;
import saros.session.User;

/**
 * This class is responsible for mapping global project IDs to local {@linkplain IReferencePoint
 * projects}. On the host, it also tracks which users have already received which shared projects.
 *
 * <p>The project IDs are used to identify shared projects across the network, even when the local
 * names of shared projects are different. The ID is determined by the project/file-host.
 */
class SharedProjectMapper {

  private static final Logger log = Logger.getLogger(SharedProjectMapper.class);

  /** Mapping from project IDs to currently registered shared projects. */
  private final Map<String, IReferencePoint> idToProjectMapping =
      new HashMap<String, IReferencePoint>();

  /** Mapping from currently registered shared projects to their id's. */
  private final Map<IReferencePoint, String> projectToIDMapping =
      new HashMap<IReferencePoint, String>();

  /**
   * Map for storing which clients have which projects. Used by the host to determine who can
   * currently process an activity related to a particular project. (Non-hosts don't maintain this
   * map.)
   */
  private final Map<User, List<String>> projectsOfUsers = new HashMap<User, List<String>>();

  /**
   * Adds a project to the set of currently shared projects.
   *
   * @param id the ID for the project
   * @param project the project to add
   * @throws NullPointerException if the ID or project is <code>null</code>
   * @throws IllegalStateException if the ID is already in use or the project was already added
   */
  public synchronized void addProject(String id, IReferencePoint project) {
    if (id == null) throw new NullPointerException("ID is null");

    if (project == null) throw new NullPointerException("project is null");

    String currentProjectID = projectToIDMapping.get(project);
    IReferencePoint currentProject = idToProjectMapping.get(id);

    if (id.equals(currentProjectID) && project.equals(currentProject)) {
      throw new IllegalStateException(
          "ID - project mapping (" + id + " - " + project + ") already present.");
    }

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
          "cannot assign project "
              + project
              + " to ID "
              + id
              + " because it is already registered with project "
              + currentProject);
    }

    idToProjectMapping.put(id, project);
    projectToIDMapping.put(project, id);

    log.debug("added project " + project + " with ID " + id);
  }

  /**
   * Removes a project from the set of currently shared projects. Does nothing if the project is not
   * shared.
   *
   * @param id the ID of the project to remove
   */
  public synchronized void removeProject(String id) {
    IReferencePoint project = idToProjectMapping.get(id);

    if (project == null) {
      log.warn("could not remove project, no project is registerid with ID: " + id);
      return;
    }

    idToProjectMapping.remove(id);
    projectToIDMapping.remove(project);

    log.debug("removed project " + project + " with ID " + id);
  }

  /**
   * Returns the ID assigned to the given shared project.
   *
   * @param project the project to look up the ID for
   * @return the shared project's ID or <code>null</code> if the project is not shared
   */
  public synchronized String getID(IReferencePoint project) {
    return projectToIDMapping.get(project);
  }

  /**
   * Returns the shared project with the given ID.
   *
   * @param id the ID to look up the project for
   * @return the shared project for the given ID or <code>null</code> if no shared project is
   *     registered with this ID
   */
  public synchronized IReferencePoint getProject(String id) {
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

    if (resource.getType() == REFERENCE_POINT) return idToProjectMapping.containsValue(resource);

    IReferencePoint project = resource.getReferencePoint();

    if (!idToProjectMapping.containsValue(project)) return false;

    return !resource.isIgnored();
  }

  /**
   * Returns the currently shared projects.
   *
   * @return a newly created {@link Set} with the shared projects
   */
  public synchronized Set<IReferencePoint> getProjects() {
    return new HashSet<IReferencePoint>(idToProjectMapping.values());
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
   * Checks if the given user already has the given project, and can thus process activities related
   * to that project.
   *
   * <p>This method should only be called by the session's host.
   *
   * @param user The user to be checked
   * @param project The project to be checked
   * @return <code>true</code> if the user currently has the project, <code>false</code> if not
   */
  public synchronized boolean userHasProject(User user, IReferencePoint project) {
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
   * @see #userHasProject(User, IReferencePoint)
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
   * @see #userHasProject(User, IReferencePoint)
   */
  public void userLeft(User user) {
    projectsOfUsers.remove(user);
  }
}
