package saros.negotiation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import saros.filesystem.IProject;
import saros.filesystem.IResource;

/**
 * Defines which projects and *which of their resources) to share during a particular {@link
 * AbstractOutgoingProjectNegotiation}.
 */
public class ProjectSharingData implements Iterable<IProject> {

  private Map<String, IProject> projectsById = new HashMap<>();
  private Map<IProject, String> idsByProject = new HashMap<>();
  private Map<IProject, List<IResource>> resourcesToShareByProject = new HashMap<>();

  /**
   * Declares that the passed project should be shared with the specified ID. Optionally, a subset
   * of the project's resources may be passed, which means that only those resources should be
   * shared (that is, the project should be shared partially).
   *
   * @param project project that should be shared
   * @param projectId session-wide ID assigned to the project
   * @param resourcesToShare the resources of the project to share , or null to share the project
   *     completely
   */
  public void addProject(IProject project, String projectId, List<IResource> resourcesToShare) {
    projectsById.put(projectId, project);
    idsByProject.put(project, projectId);
    resourcesToShareByProject.put(project, resourcesToShare);
  }

  /**
   * Returns the to-be-shared project with the passed ID.
   *
   * @param id project ID
   * @return project with the ID
   */
  public IProject getProject(String id) {
    return projectsById.get(id);
  }

  /**
   * Returns if the project is already contained in this collection
   *
   * @param id projectId
   * @return boolean indicating if the project is already contained in this collection
   */
  public boolean hasProjectById(String id) {
    return projectsById.containsKey(id);
  }

  /**
   * Returns if the project is already contained in this collection
   *
   * @param project
   * @return boolean indicating if the project is already contained in this collection
   */
  public boolean hasProject(IProject project) {
    return resourcesToShareByProject.containsKey(project);
  }

  /**
   * Returns the ID of the given to-be-shared project.
   *
   * @param project one of the to-be-shared projects
   * @return matching project ID
   */
  public String getProjectID(IProject project) {
    return idsByProject.get(project);
  }

  /**
   * Returns whether sharing of the specified project should be restricted to particular resources
   * (which, if true, can be queried with {@link #getResourcesToShare(IProject)}.
   *
   * @param project one of the to-be-shared projects
   * @return true if the project should be shared partially, false if completely
   */
  public boolean shouldBeSharedPartially(IProject project) {
    return getResourcesToShare(project) != null;
  }

  /**
   * Returns a list of the resources to share from the specified project if that project is to be
   * shared only partially. If not, null is returned instead.
   *
   * @param project one of the to-be-shared projects
   * @return resources to share, or null if the project should be shared completely
   */
  public List<IResource> getResourcesToShare(IProject project) {
    return resourcesToShareByProject.get(project);
  }

  /**
   * Returns the number of to-be-shared projects added to this {@link ProjectSharingData} instance.
   *
   * @return number of projects
   */
  public int size() {
    return projectsById.size();
  }

  /**
   * Returns whether this {@link ProjectSharingData} instance contains any to-be-shared projects.
   *
   * @return true if there are no projects, false if there are
   */
  public boolean isEmpty() {
    return projectsById.isEmpty();
  }

  @Override
  public Iterator<IProject> iterator() {
    return Collections.unmodifiableCollection(projectsById.values()).iterator();
  }
}
