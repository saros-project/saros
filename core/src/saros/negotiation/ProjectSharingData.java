package saros.negotiation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;

/**
 * Defines which reference point and which of their resources) to share during a particular {@link
 * AbstractOutgoingProjectNegotiation}.
 */
public class ProjectSharingData implements Iterable<IReferencePoint> {

  private Map<String, IReferencePoint> referencePointsById = new HashMap<>();
  private Map<IReferencePoint, String> idsByReferencePoint = new HashMap<>();
  private Map<IReferencePoint, List<IResource>> resourcesToShareByReferencePoint = new HashMap<>();

  /**
   * Declares that the passed reference point should be shared with the specified ID. Optionally, a
   * subset of the reference point's resources may be passed, which means that only those resources
   * should be shared (that is, the reference point should be shared partially).
   *
   * @param referencePoint reference point that should be shared
   * @param referencePointId session-wide ID assigned to the reference point
   * @param resourcesToShare the resources of the reference point to share , or null to share the
   *     reference
   */
  public void addReferencePoint(
      IReferencePoint referencePoint, String referencePointId, List<IResource> resourcesToShare) {
    referencePointsById.put(referencePointId, referencePoint);
    idsByReferencePoint.put(referencePoint, referencePointId);
    resourcesToShareByReferencePoint.put(referencePoint, resourcesToShare);
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
   * @param id referencePointID
   * @return boolean indicating if the reference point is already contained in this collection
   */
  public boolean hasReferencePointById(String id) {
    return referencePointsById.containsKey(id);
  }

  /**
   * Returns if the reference point is already contained in this collection
   *
   * @param referencePoint one of the to-be-shared reference point
   * @return boolean indicating if the reference point is already contained in this collection
   */
  public boolean hasReferencePoint(IReferencePoint referencePoint) {
    return resourcesToShareByReferencePoint.containsKey(referencePoint);
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
   * Returns whether sharing of the specified reference point should be restricted to particular
   * resources (which, if true, can be queried with {@link #getResourcesToShare(IReferencePoint)}.
   *
   * @param referencePoint one of the to-be-shared referencePoints
   * @return true if the reference point should be shared partially, false if completely
   */
  public boolean shouldBeSharedPartially(IReferencePoint referencePoint) {
    return getResourcesToShare(referencePoint) != null;
  }

  /**
   * Returns a list of the resources to share from the specified reference point if that reference
   * point is to be shared only partially. If not, null is returned instead.
   *
   * @param referencePoint one of the to-be-shared reference point
   * @return resources to share, or null if the reference point should be shared completely
   */
  public List<IResource> getResourcesToShare(IReferencePoint referencePoint) {
    return resourcesToShareByReferencePoint.get(referencePoint);
  }

  /**
   * Returns the number of to-be-shared reference points added to this {@link ProjectSharingData}
   * instance.
   *
   * @return number of reference points
   */
  public int size() {
    return referencePointsById.size();
  }

  /**
   * Returns whether this {@link ProjectSharingData} instance contains any to-be-shared reference
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
