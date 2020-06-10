package saros.negotiation;

import java.util.HashSet;
import java.util.Set;
import saros.filesystem.IReferencePoint;

/**
 * This class collects resources that should be added to a session to add them at once in
 * preparation of a new single resource negotiation. The reason is to prevent multiple concurrent
 * running resource negotiations per user.
 */
public class ResourceNegotiationCollector {
  private Set<IReferencePoint> referencePoints = new HashSet<>();

  /**
   * Add reference points for the next resource negotiation.
   *
   * @param referencePointsToAdd reference points to add
   */
  public synchronized void addReferencePoints(Set<IReferencePoint> referencePointsToAdd) {
    referencePoints.addAll(referencePointsToAdd);
  }

  /**
   * Returns a set of reference points that should be handled by a resource negotiation.
   *
   * <p>Resets the Collector for next additions.
   *
   * @return reference points to add
   */
  public synchronized Set<IReferencePoint> getReferencePoints() {
    Set<IReferencePoint> tmp = referencePoints;
    referencePoints = new HashSet<>();

    return tmp;
  }
}
