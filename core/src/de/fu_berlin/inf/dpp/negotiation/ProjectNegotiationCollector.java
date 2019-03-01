package de.fu_berlin.inf.dpp.negotiation;

import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class collects resources that should be added to a session to add them at once in
 * preparation of a new single project negotiation. The reason is to prevent multiple concurrent
 * running project negotiations per user.
 */
public class ProjectNegotiationCollector {
  private Map<IProject, List<IResource>> mapping = new HashMap<IProject, List<IResource>>();

  /**
   * Add project resource mappings for the next project negotiation.
   *
   * @param projectResourcesMapping project resource mappings to add
   */
  public synchronized void add(Map<IProject, List<IResource>> projectResourcesMapping) {
    for (Entry<IProject, List<IResource>> mapEntry : projectResourcesMapping.entrySet()) {
      final IProject project = mapEntry.getKey();
      final List<IResource> resources = mapEntry.getValue();

      boolean alreadyFullShared = mapping.containsKey(project) && mapping.get(project) == null;

      /* full shared project */
      if (resources == null || alreadyFullShared) {
        mapping.put(project, null);
        continue;
      }

      /* update partial shared project */
      List<IResource> mappedResources = mapping.get(project);
      if (mappedResources == null) {
        mapping.put(project, new ArrayList<IResource>(resources));
      } else {
        mappedResources.addAll(resources);
      }
    }
  }

  /**
   * Returns a list of project resource mappings that should be handled by a project negotiation.
   * Resets the Collector for next additions.
   *
   * @return project resource mappings to add
   */
  public synchronized Map<IProject, List<IResource>> get() {
    Map<IProject, List<IResource>> tmp = mapping;
    mapping = new HashMap<IProject, List<IResource>>();
    return tmp;
  }
}
