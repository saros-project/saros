package de.fu_berlin.inf.dpp.session.internal;

import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.session.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * This class is responsible for mapping global reference point IDs to local {@linkplain
 * IReferencePoint referencePoints}, as well as storing which resources are shared from each
 * reference point. On the host, it also tracks which users have already received which shared
 * reference point.
 *
 * <p>The reference point IDs are used to identify shared reference point across the network, even
 * when the local names of shared reference point are different. The ID is determined by the
 * reference point/file-host.
 */
class SharedReferencePointMapper {

  private static final Logger LOG = Logger.getLogger(SharedReferencePointMapper.class);

  /** Mapping from reference point IDs to currently registered shared reference point. */
  private final Map<String, IReferencePoint> idToReferencePointMapping;

  /** Mapping from currently registered shared reference point to their id's. */
  private final Map<IReferencePoint, String> referencePointToIDMapping;

  /**
   * Map for storing which clients have which reference point. Used by the host to determine who can
   * currently process an activity related to a particular reference point. (Non-hosts don't
   * maintain this map.)
   */
  private final Map<User, List<String>> referencePointsOfUsers;

  /**
   * Map for storing the set of resources shared for each shared reference point. Maps to <code>null
   * </code> for completely shared reference points.
   */
  private final Map<IReferencePoint, Set<IResource>> partiallySharedResourceMapping;

  /** Set containing the currently completely shared reference points. */
  private final Set<IReferencePoint> completelySharedReferencePoints;

  /** Set containing the currently partially shared reference points. */
  private final Set<IReferencePoint> partiallySharedReferencePoints;

  public SharedReferencePointMapper() {
    idToReferencePointMapping = new HashMap<String, IReferencePoint>();
    referencePointToIDMapping = new HashMap<IReferencePoint, String>();
    referencePointsOfUsers = new HashMap<User, List<String>>();
    partiallySharedResourceMapping = new HashMap<IReferencePoint, Set<IResource>>();
    completelySharedReferencePoints = new HashSet<IReferencePoint>();
    partiallySharedReferencePoints = new HashSet<IReferencePoint>();
  }

  /**
   * Adds a reference point to the set of currently shared reference points.
   *
   * <p>It is possible to "upgrade" a partially shared reference point to a completely shared
   * reference point by just adding the same reference point with the same ID again that must now
   * marked as not partially shared.
   *
   * @param id the ID for the reference points
   * @param referencePoint the reference points to add
   * @param isPartially <code>true</code> if the reference point should be treated as a partially
   *     shared reference point, <code>false</code> if it should be treated as completely shared
   * @throws NullPointerException if the ID or reference point is <code>null</code>
   * @throws IllegalStateException if the ID is already in use or the reference point was already
   *     added
   */
  public synchronized void addReferencePoint(
      String id, IReferencePoint referencePoint, boolean isPartially) {
    boolean upgrade = false;

    if (id == null) throw new NullPointerException("ID is null");

    if (referencePoint == null) throw new NullPointerException("referencePoint is null");

    String currentReferencePointID = referencePointToIDMapping.get(referencePoint);
    IReferencePoint currentReferencePoint = idToReferencePointMapping.get(id);

    if (currentReferencePointID != null && !id.equals(currentReferencePointID)) {
      throw new IllegalStateException(
          "cannot assign ID "
              + id
              + " to referencePoint "
              + referencePoint
              + " because it is already registered with ID "
              + currentReferencePointID);
    }

    if (currentReferencePoint != null && !referencePoint.equals(currentReferencePoint)) {
      throw new IllegalStateException(
          "ID "
              + id
              + " for referencePoint "
              + referencePoint
              + " is already used by referencePoint "
              + currentReferencePoint);
    }

    if (isPartially && partiallySharedReferencePoints.contains(referencePoint))
      throw new IllegalStateException(
          "referencePoint " + referencePoint + " is already partially shared");

    if (!isPartially && completelySharedReferencePoints.contains(referencePoint))
      throw new IllegalStateException(
          "referencePoint " + referencePoint + " is already completely shared");

    if (isPartially && completelySharedReferencePoints.contains(referencePoint))
      throw new IllegalStateException(
          "referencePoint "
              + referencePoint
              + " is already completely shared (cannot downgrade a completely shared referencePoint)");

    if (!isPartially && partiallySharedReferencePoints.contains(referencePoint)) {
      partiallySharedReferencePoints.remove(referencePoint);
      upgrade = true;
    }

    if (isPartially) partiallySharedReferencePoints.add(referencePoint);
    else completelySharedReferencePoints.add(referencePoint);

    assert Collections.disjoint(completelySharedReferencePoints, partiallySharedReferencePoints);

    if (upgrade) {
      // release resources
      partiallySharedResourceMapping.put(referencePoint, null);

      LOG.debug(
          "upgraded partially shared referencePoint "
              + referencePoint
              + " with ID "
              + id
              + " to a completely shared referencePoint");
      return;
    }

    idToReferencePointMapping.put(id, referencePoint);
    referencePointToIDMapping.put(referencePoint, id);

    if (isPartially) partiallySharedResourceMapping.put(referencePoint, new HashSet<IResource>());
    else partiallySharedResourceMapping.put(referencePoint, null);

    LOG.debug(
        "added referencePoint "
            + referencePoint
            + " with ID "
            + id
            + " [completely shared:"
            + !isPartially
            + "]");
  }

  /**
   * Removes a reference point from the set of currently shared referencePoint. Does nothing if the
   * referencePoint is not shared.
   *
   * @param id the ID of the referencePoint to remove
   */
  public synchronized void removeReferencePoint(String id) {
    IReferencePoint referencePoint = idToReferencePointMapping.get(id);

    if (referencePoint == null) {
      LOG.warn("could not remove referencePoint, no referencePoint is registerid with ID: " + id);
      return;
    }

    if (partiallySharedReferencePoints.contains(referencePoint))
      partiallySharedReferencePoints.remove(referencePoint);
    else completelySharedReferencePoints.remove(referencePoint);

    idToReferencePointMapping.remove(id);
    referencePointToIDMapping.remove(referencePoint);
    partiallySharedResourceMapping.remove(referencePoint);

    LOG.debug("removed referencePoint " + referencePoint + " with ID " + id);
  }

  /**
   * Adds the given resources to a <b>partially</b> shared reference point.
   *
   * @param referencePoint a referencePoint that was added as a partially shared referencePoint
   * @param resources the resources to add
   */
  /*
   * TODO needs proper sync. in the SarosSession class
   *
   * @throws IllegalStateException if the project is completely or not shared
   * at all
   */
  public synchronized void addResources(
      IReferencePoint referencePoint, Collection<? extends IResource> resources) {

    if (referencePointToIDMapping.get(referencePoint) == null) {
      LOG.warn(
          "could not add resources to referencePoint "
              + referencePoint
              + " because it is not shared");
      // throw new IllegalStateException(
      // "could not add resources to project " + project
      // + " because it is not shared");
      return;
    }

    if (completelySharedReferencePoints.contains(referencePoint)) {
      LOG.warn("cannot add resources to completely shared referencePoint: " + referencePoint);
      // throw new IllegalStateException(
      // "cannot add resources to completely shared project: " + project);
      return;
    }

    Set<IResource> partiallySharedResources = partiallySharedResourceMapping.get(referencePoint);

    if (partiallySharedResources.isEmpty()) {
      partiallySharedResources = new HashSet<IResource>(Math.max(1024, (resources.size() * 3) / 2));

      partiallySharedResourceMapping.put(referencePoint, partiallySharedResources);
    }

    partiallySharedResources.addAll(resources);
  }

  /**
   * Removes the given resources from a <b>partially</b> shared reference point.
   *
   * @param referencePoint a reference point that was added as a partially shared reference point
   * @param resources the resources to remove
   */
  /*
   * TODO needs proper sync. in the SarosSession class
   *
   * @throws IllegalStateException if the referencePoint is completely or not
   * shared at all
   */
  public synchronized void removeResources(
      IReferencePoint referencePoint, Collection<? extends IResource> resources) {

    if (referencePointToIDMapping.get(referencePoint) == null) {
      LOG.warn(
          "could not remove resources from referencePoint "
              + referencePoint
              + " because it is not shared");
      // throw new IllegalStateException(
      // "could not remove resources from project " + project
      // + " because it is not shared");
      return;
    }

    if (completelySharedReferencePoints.contains(referencePoint)) {
      LOG.warn("cannot remove resources from completely shared reference point: " + referencePoint);
      // throw new IllegalStateException(
      // "cannot remove resources from completely shared project: " +
      // project);
      return;
    }

    Set<IResource> partiallySharedResources = partiallySharedResourceMapping.get(referencePoint);

    partiallySharedResources.removeAll(resources);
  }

  /**
   * Atomically removes and adds resources. The resources to remove will be removed first before the
   * resources to add will be added.
   *
   * @param referencePoint a reference point that was added as a partially shared reference point
   * @param resourcesToRemove the resources to remove
   * @param resourcesToAdd the resources to add
   */
  /*
   * TODO needs proper sync. in the SarosSession class
   *
   * @throws IllegalStateException if the referencePoint is completely or not
   * shared at all
   */
  public synchronized void removeAndAddResources(
      IReferencePoint referencePoint,
      Collection<? extends IResource> resourcesToRemove,
      Collection<? extends IResource> resourcesToAdd) {

    removeResources(referencePoint, resourcesToRemove);
    addResources(referencePoint, resourcesToAdd);
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
    return isShared(resource, resource.getReferencePoint());
  }

  /**
   * Returns whether the given resource is included in one of the currently shared reference points.
   *
   * @param resource the resource to check for
   * @param referencePoint of resource
   * @return <code>true</code> if the resource is shared, <code>false</code> otherwise
   */
  public synchronized boolean isShared(IResource resource, IReferencePoint referencePoint) {
    if (resource == null) return false;

    if (!idToReferencePointMapping.containsValue(referencePoint)) return false;

    if (isCompletelyShared(referencePoint))
      // TODO how should partial sharing handle this case ?
      return !resource.isDerived(true);
    else return partiallySharedResourceMapping.get(referencePoint).contains(resource);
  }

  public synchronized boolean isShared(IReferencePoint referencePoint) {
    return idToReferencePointMapping.containsValue(referencePoint);
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
   * Returns all resources from all partially shared reference points.
   *
   * @return a newly created {@link List} with all of the partially shared reference points'
   *     resources
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
   * Returns the current number of shared reference points.
   *
   * @return number of shared reference points
   */
  public synchronized int size() {
    return idToReferencePointMapping.size();
  }

  /**
   * Returns a mapping for each shared reference point and its containing resources. The resource
   * list is <b>always</b> <code>null</code> for completely shared reference points.
   *
   * @return a map from reference point to resource list (partially shared) or <code>null</code>
   *     (completely shared)
   */
  public synchronized Map<IReferencePoint, List<IResource>> getReferencePointResourceMapping() {

    Map<IReferencePoint, List<IResource>> result = new HashMap<IReferencePoint, List<IResource>>();

    for (Map.Entry<IReferencePoint, Set<IResource>> entry :
        partiallySharedResourceMapping.entrySet()) {

      List<IResource> partiallySharedResources = null;

      if (entry.getValue() != null)
        partiallySharedResources = new ArrayList<IResource>(entry.getValue());

      result.put(entry.getKey(), partiallySharedResources);
    }

    return result;
  }

  /**
   * Checks whether a reference point is completely shared.
   *
   * @param referencePoint the referencePoint to check for
   * @return <code>true</code> if the reference point is completely shared, <code>false</code> if
   *     the reference point is not or partially shared
   */
  public synchronized boolean isCompletelyShared(IReferencePoint referencePoint) {
    return completelySharedReferencePoints.contains(referencePoint);
  }

  /**
   * Checks if a reference point is partially shared.
   *
   * @param referencePoint the reference point to check
   * @return <code>true</code> if the reference point is partially shared, <code>false</code> if the
   *     reference point is not or completely shared
   */
  public synchronized boolean isPartiallyShared(IReferencePoint referencePoint) {
    return partiallySharedReferencePoints.contains(referencePoint);
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
   * Tells the mapper that the given user now has all currently shared reference point.
   *
   * <p>This method should only be called by the session's host.
   *
   * @param user user who now has all reference points
   * @see #userHasReferencePoint(User, IReferencePoint)
   */
  public synchronized void addMissingReferencePointsToUser(User user) {
    List<String> referencePoints = new ArrayList<String>();
    for (String referencePoint : idToReferencePointMapping.keySet()) {
      referencePoints.add(referencePoint);
    }

    this.referencePointsOfUsers.put(user, referencePoints);
  }

  /**
   * Removes the user-referencePoint mapping of the user that left the session.
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
