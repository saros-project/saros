package de.fu_berlin.inf.dpp.negotiation;

import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
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
  private Map<IReferencePoint, List<IResource>> mapping =
      new HashMap<IReferencePoint, List<IResource>>();

  /**
   * Add reference point resource mappings for the next project negotiation.
   *
   * @param referencePointResourcesMapping reference point resource mappings to add
   */
  public synchronized void add(
      Map<IReferencePoint, List<IResource>> referencePointResourcesMapping) {
    for (Entry<IReferencePoint, List<IResource>> mapEntry :
        referencePointResourcesMapping.entrySet()) {
      final IReferencePoint referencePoint = mapEntry.getKey();
      final List<IResource> resources = mapEntry.getValue();

      boolean alreadyFullShared =
          mapping.containsKey(referencePoint) && mapping.get(referencePoint) == null;

      /* full shared project / reference point */
      if (resources == null || alreadyFullShared) {
        mapping.put(referencePoint, null);
        continue;
      }

      /* update partial shared project / reference point */
      List<IResource> mappedResources = mapping.get(referencePoint);
      if (mappedResources == null) {
        mapping.put(referencePoint, new ArrayList<IResource>(resources));
      } else {
        mappedResources.addAll(resources);
      }
    }
  }

  /**
   * Returns a list of reference point resource mappings that should be handled by a project
   * negotiation. Resets the Collector for next additions.
   *
   * @return reference point resource mappings to add
   */
  public synchronized Map<IReferencePoint, List<IResource>> get() {
    Map<IReferencePoint, List<IResource>> tmp = mapping;
    mapping = new HashMap<IReferencePoint, List<IResource>>();
    return tmp;
  }
}
