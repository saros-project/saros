package saros.negotiation;

import java.util.HashSet;
import java.util.Set;
import saros.filesystem.IReferencePoint;

/**
 * This class collects resources that should be added to a session to add them at once in
 * preparation of a new single project negotiation. The reason is to prevent multiple concurrent
 * running project negotiations per user.
 */
public class ProjectNegotiationCollector {
  private Set<IReferencePoint> projects = new HashSet<>();

  /**
   * Add projects for the next project negotiation.
   *
   * @param projectsToAdd projects to add
   */
  public synchronized void addProjects(Set<IReferencePoint> projectsToAdd) {
    projects.addAll(projectsToAdd);
  }

  /**
   * Returns a set of projects that should be handled by a project negotiation.
   *
   * <p>Resets the Collector for next additions.
   *
   * @return projects to add
   */
  public synchronized Set<IReferencePoint> getProjects() {
    Set<IReferencePoint> tmp = projects;
    projects = new HashSet<>();

    return tmp;
  }
}
