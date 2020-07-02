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
 * This class is responsible for mapping global reference point IDs to local {@linkplain
 * IReferencePoint reference points}. On the host, it also tracks which users have already received
 * which shared reference points.
 *
 * <p>The reference point IDs are used to identify shared reference points across the network, even
 * when the local names of shared reference points are different. The ID is determined by the
 * reference point/file-host.
 */
class SharedReferencePointMapper {

  private static final Logger log = Logger.getLogger(SharedReferencePointMapper.class);

  /** Mapping from reference point IDs to currently registered shared reference points. */
  private final Map<String, IReferencePoint> idToReferencePointMapping =
      new HashMap<String, IReferencePoint>();

  /** Mapping from currently registered shared reference points to their id's. */
  private final Map<IReferencePoint, String> referencePointToIDMapping =
      new HashMap<IReferencePoint, String>();

  /**
   * Map for storing which clients have which reference points. Used by the host to determine who
   * can currently process an activity related to a particular reference point. (Non-hosts don't
   * maintain this map.)
   */
  private final Map<User, List<String>> referencePointsOfUsers = new HashMap<User, List<String>>();

  /**
   * Adds a reference point to the set of currently shared reference points.
   *
   * @param id the ID for the reference point
   * @param referencePoint the reference point to add
   * @throws NullPointerException if the ID or reference point is <code>null</code>
   * @throws IllegalStateException if the ID is already in use or the reference point was already
   *     added
   */
  public synchronized void addReferencePoint(String id, IReferencePoint referencePoint) {
    if (id == null) throw new NullPointerException("ID is null");

    if (referencePoint == null) throw new NullPointerException("reference point is null");

    String currentReferencePointID = referencePointToIDMapping.get(referencePoint);
    IReferencePoint currentReferencePoin = idToReferencePointMapping.get(id);

    if (id.equals(currentReferencePointID) && referencePoint.equals(currentReferencePoin)) {
      throw new IllegalStateException(
          "ID - reference point mapping (" + id + " - " + referencePoint + ") already present.");
    }

    if (currentReferencePointID != null && !id.equals(currentReferencePointID)) {
      throw new IllegalStateException(
          "cannot assign ID "
              + id
              + " to reference point "
              + referencePoint
              + " because it is already registered with ID "
              + currentReferencePointID);
    }

    if (currentReferencePoin != null && !referencePoint.equals(currentReferencePoin)) {
      throw new IllegalStateException(
          "cannot assign reference point "
              + referencePoint
              + " to ID "
              + id
              + " because it is already registered with reference point "
              + currentReferencePoin);
    }

    checkForNestedReferencePoints(referencePoint);

    idToReferencePointMapping.put(id, referencePoint);
    referencePointToIDMapping.put(referencePoint, id);

    log.debug("added reference point " + referencePoint + " with ID " + id);
  }

  /**
   * Checks whether adding the given reference point to the mapping would create shared nested
   * reference points.
   *
   * @param addedReferencePoint the reference point to add to the mapping
   * @throws IllegalStateException if nested reference points are detected
   */
  private void checkForNestedReferencePoints(IReferencePoint addedReferencePoint) {
    for (IReferencePoint sharedReferencePoint : idToReferencePointMapping.values()) {
      if (addedReferencePoint.isNested(sharedReferencePoint)) {
        throw new IllegalStateException(
            "Reference point "
                + addedReferencePoint
                + " can not be added to the mapping as this would create nested reference points "
                + "in combination with the already shared reference point "
                + sharedReferencePoint);
      }
    }
  }

  /**
   * Removes a reference point from the set of currently shared reference points. Does nothing if
   * the reference point is not shared.
   *
   * @param id the ID of the reference point to remove
   */
  public synchronized void removeReferencePoint(String id) {
    IReferencePoint referencePoint = idToReferencePointMapping.get(id);

    if (referencePoint == null) {
      log.warn("could not remove reference point, no reference point is registered with ID: " + id);
      return;
    }

    idToReferencePointMapping.remove(id);
    referencePointToIDMapping.remove(referencePoint);

    log.debug("removed reference point " + referencePoint + " with ID " + id);
  }

  /**
   * Returns the ID assigned to the given shared reference point.
   *
   * @param referencePoint the reference point to look up the ID for
   * @return the shared reference point's ID or <code>null</code> if the reference point is not
   *     shared
   */
  public synchronized String getID(IReferencePoint referencePoint) {
    return referencePointToIDMapping.get(referencePoint);
  }

  /**
   * Returns the shared reference point with the given ID.
   *
   * @param id the ID to look up the reference point for
   * @return the shared reference point for the given ID or <code>null</code> if no shared reference
   *     point is registered with this ID
   */
  public synchronized IReferencePoint getReferencePoint(String id) {
    return idToReferencePointMapping.get(id);
  }

  /**
   * Returns whether the given resource is included in one of the currently shared reference points.
   *
   * @param resource the resource to check for
   * @return <code>true</code> if the resource is shared, <code>false</code> otherwise
   */
  public synchronized boolean isShared(IResource resource) {
    if (resource == null) return false;

    if (resource.getType() == REFERENCE_POINT)
      return idToReferencePointMapping.containsValue(resource);

    IReferencePoint referencePoint = resource.getReferencePoint();

    if (!idToReferencePointMapping.containsValue(referencePoint)) return false;

    return !resource.isIgnored();
  }

  /**
   * Returns the currently shared reference points.
   *
   * @return a newly created {@link Set} with the shared reference points
   */
  public synchronized Set<IReferencePoint> getReferencePoints() {
    return new HashSet<IReferencePoint>(idToReferencePointMapping.values());
  }

  /**
   * Returns the current number of shared reference points.
   *
   * @return number of shared reference points
   */
  public synchronized int size() {
    return idToReferencePointMapping.size();
  }

  /**
   * Checks if the given user already has the given reference point, and can thus process activities
   * related to that reference point.
   *
   * <p>This method should only be called by the session's host.
   *
   * @param user The user to be checked
   * @param referencePoint The reference point to be checked
   * @return <code>true</code> if the user currently has the reference point, <code>false</code> if
   *     not
   */
  public synchronized boolean userHasReferencePoint(User user, IReferencePoint referencePoint) {
    if (referencePointsOfUsers.containsKey(user)) {
      return referencePointsOfUsers.get(user).contains(getID(referencePoint));
    }
    return false;
  }

  /**
   * Tells the mapper that the given user now has all currently shared reference points.
   *
   * <p>This method should only be called by the session's host.
   *
   * @param user user who now has all reference points
   * @see #userHasReferencePoint(User, IReferencePoint)
   */
  public synchronized void addMissingReferencePointsToUser(User user) {
    List<String> referencePointIds = new ArrayList<String>();
    for (String referencePointId : idToReferencePointMapping.keySet()) {
      referencePointIds.add(referencePointId);
    }

    this.referencePointsOfUsers.put(user, referencePointIds);
  }

  /**
   * Removes the user-reference point mapping of the user that left the session.
   *
   * <p>This method should only be called by the session's host.
   *
   * @param user user who left the session
   * @see #userHasReferencePoint(User, IReferencePoint)
   */
  public void userLeft(User user) {
    referencePointsOfUsers.remove(user);
  }
}
