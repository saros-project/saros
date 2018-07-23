package de.fu_berlin.inf.dpp.session.internal;

import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IResourceActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.session.IActivityQueuer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class enables queuing of {@link IResourceActivity} for provided resources.
 *
 * <p>TODO Discard obsolete Editor Events #158
 */
public class ResourceActivityQueuer implements IActivityQueuer {

  /** resources that are unavailable for activity processing */
  private final Set<SPath> unavailableResources = ConcurrentHashMap.newKeySet();

  /** resource activities queued for later processing */
  private final List<IResourceActivity> activityQueue = new LinkedList<>();

  /**
   * Enables activity queuing for provided resources.
   *
   * @param resources {@code Set} of {@link SPath}s to queue
   */
  public void enableQueuing(Set<SPath> resources) {
    unavailableResources.addAll(resources);
  }

  /**
   * Disables activity queuing for provided resource.
   *
   * @param resource {@link SPath} of resource
   */
  public void disableQueuing(SPath resource) {
    unavailableResources.remove(resource);
  }

  @Override
  public synchronized List<IActivity> process(List<IActivity> activities) {
    if (unavailableResources.isEmpty() && activityQueue.isEmpty()) {
      return activities;
    }

    List<IActivity> returnList = new LinkedList<>();
    Set<SPath> resourcesInQueue = new HashSet<>();
    processQueued(returnList, resourcesInQueue);
    processNew(returnList, resourcesInQueue, activities);

    return returnList;
  }

  private void processQueued(List<IActivity> returnList, Set<SPath> resourcesInQueue) {
    for (Iterator<IResourceActivity> queueIter = activityQueue.iterator(); queueIter.hasNext(); ) {
      IResourceActivity activity = queueIter.next();

      if (isAvailable(activity)) {
        // dequeue
        returnList.add(activity);
        queueIter.remove();
      } else {
        resourcesInQueue.add(activity.getPath());
      }
    }
  }

  private void processNew(
      List<IActivity> returnList, Set<SPath> resourcesInQueue, List<IActivity> activities) {
    for (IActivity activity : activities) {
      if (isAvailableAndNotQueued(activity, resourcesInQueue)) {
        returnList.add(activity);
      } else {
        activityQueue.add((IResourceActivity) activity);
      }
    }
  }

  private boolean isAvailable(IResourceActivity activity) {
    SPath path = activity.getPath();
    if (path != null && unavailableResources.contains(path)) {
      return false;
    }
    return true;
  }

  /** @return true if resource is available and no activities for this {@code SPath} queued */
  private boolean isAvailableAndNotQueued(IActivity activity, Set<SPath> resourcesInQueue) {
    if (activity instanceof IResourceActivity) {
      IResourceActivity resourceActivity = (IResourceActivity) activity;
      SPath path = resourceActivity.getPath();

      return !resourcesInQueue.contains(path) && isAvailable(resourceActivity);
    }
    return true;
  }
}
