package saros.negotiation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import saros.filesystem.IReferencePoint;

/**
 * Defines which reference points (and which of their resources) to share during a particular {@link
 * AbstractOutgoingResourceNegotiation}.
 */
public class ResourceSharingData implements Iterable<IReferencePoint> {

  private Map<String, IReferencePoint> referencePointsById = new HashMap<>();
  private Map<IReferencePoint, String> idsByReferencePoint = new HashMap<>();

  /**
   * Declares that the passed reference point should be shared with the specified ID.
   *
   * @param referencePoint reference point that should be shared
   * @param referencePointId session-wide ID assigned to the reference point
   */
  public void addReferencePoint(IReferencePoint referencePoint, String referencePointId) {
    referencePointsById.put(referencePointId, referencePoint);
    idsByReferencePoint.put(referencePoint, referencePointId);
  }

  /**
   * Returns the to-be-shared reference point with the passed ID.
   *
   * @param id reference point ID
   * @return reference point with the ID
   */
  public IReferencePoint getReferencePoint(String id) {
    return referencePointsById.get(id);
  }

  /**
   * Returns if the reference point is already contained in this collection
   *
   * @param id reference point ID
   * @return boolean indicating if the reference point is already contained in this collection
   */
  public boolean hasReferencePointById(String id) {
    return referencePointsById.containsKey(id);
  }

  /**
   * Returns if the reference point is already contained in this collection
   *
   * @param referencePoint reference point
   * @return boolean indicating if the reference point is already contained in this collection
   */
  public boolean hasReferencePoint(IReferencePoint referencePoint) {
    return idsByReferencePoint.containsKey(referencePoint);
  }

  /**
   * Returns the ID of the given to-be-shared reference point.
   *
   * @param referencePoint one of the to-be-shared reference points
   * @return matching reference point ID
   */
  public String getReferencePointID(IReferencePoint referencePoint) {
    return idsByReferencePoint.get(referencePoint);
  }

  /**
   * Returns the number of to-be-shared reference points added to this {@link ResourceSharingData}
   * instance.
   *
   * @return number of reference points
   */
  public int size() {
    return referencePointsById.size();
  }

  /**
   * Returns whether this {@link ResourceSharingData} instance contains any to-be-shared reference
   * points.
   *
   * @return true if there are no reference points, false if there are
   */
  public boolean isEmpty() {
    return referencePointsById.isEmpty();
  }

  @Override
  public Iterator<IReferencePoint> iterator() {
    return Collections.unmodifiableCollection(referencePointsById.values()).iterator();
  }
}
