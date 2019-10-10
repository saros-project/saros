package saros.concurrent.management;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.apache.log4j.Logger;
import saros.activities.ChecksumActivity;
import saros.activities.DeletionAcknowledgmentActivity;
import saros.activities.FileActivity;
import saros.activities.FileActivity.Type;
import saros.activities.IActivity;
import saros.activities.IResourceActivity;
import saros.activities.SPath;
import saros.filesystem.IProject;
import saros.session.AbstractActivityConsumer;
import saros.session.IActivityConsumer;
import saros.session.IActivityConsumer.Priority;
import saros.session.ISarosSession;
import saros.session.ISessionListener;
import saros.session.User;

/** Class to handle resource deletions and filter out activities for already deleted resources. */
class DeletedResourceFilter {
  private static final Logger log = Logger.getLogger(DeletedResourceFilter.class);

  private final ISarosSession sarosSession;

  /** Method passed by the constructor caller that can be used to react to resource deletions. */
  private final Consumer<SPath> resourceDeletionHandler;

  /**
   * A map of shared resources that were deleted during the session onto the pending acknowledgments
   * from other participants. It is used to filter out activities for such resources that were
   * created before the other participants ran the corresponding resource deletion activity locally.
   *
   * <p>The set is updated once the resource is recreated as we then want to handle activities for
   * it again. Furthermore, it is updated once all acknowledgments for a resource deletion were
   * received as the filter is then no longer necessary.
   *
   * <p>This way of filtering can lead to issues when the order of activities is not preserved, e.g.
   * when we receive the content change for a new file is received before the file creation. Such
   * activities will be dropped, leading to inconsistencies.
   */
  private final Map<SPath, List<User>> deletedResources;

  /** Activity consumer processing received deletion acknowledgments. */
  private final IActivityConsumer activityConsumer =
      new AbstractActivityConsumer() {
        @Override
        public void receive(DeletionAcknowledgmentActivity deletionAcknowledgmentActivity) {
          User source = deletionAcknowledgmentActivity.getSource();
          SPath resource = deletionAcknowledgmentActivity.getPath();

          List<User> remainingUsers = deletedResources.get(resource);

          if (remainingUsers == null) {
            log.warn(
                "Received unexpected deletion acknowledgment for file that is not filtered: "
                    + source
                    + " - "
                    + resource);

            return;

          } else if (!remainingUsers.contains(source)) {
            log.warn(
                "Received acknowledgment for file deletion from unexpected user: "
                    + source
                    + " - "
                    + resource);

            return;
          }

          log.debug("Received deletion acknowledgment from " + source + " for " + resource);

          remainingUsers.remove(source);

          if (remainingUsers.isEmpty()) {
            log.debug(
                "Dropping activity filter for "
                    + resource
                    + " as all acknowledgments were received");

            deletedResources.remove(resource);
          }
        }
      };

  /**
   * Session listener updating the held map of filtered resources when participants leave the
   * session or resources are removed from the session.
   */
  private final ISessionListener sessionListener =
      new ISessionListener() {
        @Override
        public void userLeft(User user) {
          Iterator<Entry<SPath, List<User>>> iterator = deletedResources.entrySet().iterator();
          while (iterator.hasNext()) {
            Entry<SPath, List<User>> entry = iterator.next();

            List<User> remainingUsers = entry.getValue();
            remainingUsers.remove(user);

            if (remainingUsers.isEmpty()) {
              log.debug(
                  "Dropping activity filter for "
                      + entry.getKey()
                      + " as there are no more pending acknowledgments");

              iterator.remove();
            }
          }
        }

        @Override
        public void projectRemoved(IProject project) {
          Iterator<Entry<SPath, List<User>>> iterator = deletedResources.entrySet().iterator();
          while (iterator.hasNext()) {
            SPath resource = iterator.next().getKey();

            if (resource.getProject().equals(project)) {
              log.debug(
                  "Dropping activity filter for "
                      + resource
                      + " as it is no longer part of the session");

              iterator.remove();
            }
          }
        }
      };

  /**
   * Creates a new deleted resource filter. The passed method is called every time a resource
   * deletion is detected <b>after</b> the resource is added to the map of deleted resources.
   *
   * @param sarosSession the current saros session
   * @param resourceDeletionHandler method that is called every time a resource deletion is detected
   */
  DeletedResourceFilter(ISarosSession sarosSession, Consumer<SPath> resourceDeletionHandler) {
    this.sarosSession = sarosSession;
    this.resourceDeletionHandler = resourceDeletionHandler;

    this.deletedResources = new ConcurrentHashMap<>();
  }

  /**
   * Adds the deleted resource to the held map of deleted shared resources. This causes activities
   * for such resources to be detected as filtered out by {@link #isFiltered(IActivity)} until it is
   * created again (or all deletion acknowledgments were received). Subsequently calls {@link
   * #resourceDeletionHandler} with the deleted resource.
   *
   * <p>Does nothing if the passed activity is not a {@link FileActivity} or does not have the type
   * {@link Type#REMOVED} or {@link Type#MOVED}.
   *
   * @param activity the activity to handle
   * @see #deletedResources
   * @see #handleResourceCreation(IActivity)
   */
  void handleResourceDeletion(IActivity activity) {
    if (!(activity instanceof FileActivity)) {
      return;
    }

    FileActivity fileActivity = (FileActivity) activity;

    SPath removedFile;
    if (fileActivity.getType() == Type.REMOVED) {
      removedFile = fileActivity.getPath();

    } else if (fileActivity.getType() == Type.MOVED
        && !fileActivity.getPath().equals(fileActivity.getOldPath())) {

      removedFile = fileActivity.getOldPath();

    } else {
      return;
    }

    List<User> remoteUsers = sarosSession.getRemoteUsers();

    remoteUsers.remove(activity.getSource());

    if (!remoteUsers.isEmpty()) {
      log.debug(
          "Adding activity filter for deleted file "
              + removedFile
              + ", waiting for acknowledgment from user(s) "
              + remoteUsers);

      deletedResources.put(removedFile, remoteUsers);
    }

    resourceDeletionHandler.accept(removedFile);
  }

  /**
   * Removes the created resource from the held map of deleted shared resources. This causes
   * activities for the resources to no longer be detected as filtered out by {@link
   * #isFiltered(IActivity)}.
   *
   * <p>Does nothing if the passed activity is not a {@link FileActivity} or does not have the type
   * {@link Type#CREATED} or {@link Type#MOVED}.
   *
   * @param activity the activity to handle
   * @see #deletedResources
   * @see #handleResourceDeletion(IActivity)
   */
  void handleResourceCreation(IActivity activity) {
    if (!(activity instanceof FileActivity)) {
      return;
    }

    FileActivity fileActivity = (FileActivity) activity;

    if (fileActivity.getType() == Type.MOVED || fileActivity.getType() == Type.CREATED) {
      SPath addedFile = fileActivity.getPath();

      if (deletedResources.containsKey(addedFile)) {
        log.debug("Removing activity filter for re-created file " + addedFile);

        deletedResources.remove(addedFile);
      }
    }
  }

  /**
   * Returns whether the passed activity is filtered out. This is determined by the held set of
   * deleted resources.
   *
   * <p>Non-resource activities are never detected as being filtered. Furthermore, resource
   * activities of the type {@link ChecksumActivity} that confirm the file deletion or activities of
   * the type {@link DeletionAcknowledgmentActivity} are never detected as being filtered.
   *
   * @param activity the activity to check
   * @return whether the passed activity is filtered out
   * @see #handleResourceDeletion(IActivity)
   * @see #handleResourceCreation(IActivity)
   */
  boolean isFiltered(IActivity activity) {

    if (!(activity instanceof IResourceActivity)
        || activity instanceof DeletionAcknowledgmentActivity) {

      return false;
    }

    SPath path = ((IResourceActivity) activity).getPath();

    if (path == null) {
      return false;
    }

    boolean pathIsFiltered = deletedResources.containsKey(path);

    if (pathIsFiltered && activity instanceof ChecksumActivity) {
      ChecksumActivity checksumActivity = (ChecksumActivity) activity;

      return checksumActivity.getHash() != ChecksumActivity.NON_EXISTING_DOC
          && checksumActivity.getLength() != ChecksumActivity.NON_EXISTING_DOC;
    }

    return pathIsFiltered;
  }

  /** Initializes all contained components. */
  public void initialize() {
    sarosSession.addActivityConsumer(activityConsumer, Priority.PASSIVE);
    sarosSession.addListener(sessionListener);
  }

  /** Disposes all contained components to prepare them for garbage collection. */
  public void dispose() {
    sarosSession.removeActivityConsumer(activityConsumer);
    sarosSession.removeListener(sessionListener);
  }
}
