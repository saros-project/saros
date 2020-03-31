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
import saros.activities.EditorActivity;
import saros.activities.FileActivity;
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

/** Class to handle file deletions and filter out resource activities for already deleted files. */
class ResourceActivityFilter {
  private static final Logger log = Logger.getLogger(ResourceActivityFilter.class);

  private final ISarosSession sarosSession;

  /** Method passed by the constructor caller that can be used to react to file deletions. */
  private final Consumer<SPath> fileDeletionHandler;

  /**
   * A map of shared files that were deleted during the session onto the pending acknowledgments
   * from other participants. It is used to filter out activities for such files that were created
   * before the other participants ran the corresponding file deletion activity locally.
   *
   * <p>The set is updated once the file is recreated as we then want to handle activities for it
   * again. Furthermore, it is updated once all acknowledgments for a file deletion were received as
   * the filter is then no longer necessary.
   *
   * <p>This way of filtering can lead to issues when the order of activities is not preserved, e.g.
   * when we receive the content change for a new file is received before the file creation. Such
   * activities will be dropped, leading to inconsistencies.
   */
  private final Map<SPath, List<User>> deletedFileFilter;

  /** Activity consumer processing received deletion acknowledgments. */
  private final IActivityConsumer activityConsumer =
      new AbstractActivityConsumer() {
        @Override
        public void receive(DeletionAcknowledgmentActivity deletionAcknowledgmentActivity) {
          User source = deletionAcknowledgmentActivity.getSource();
          SPath file = deletionAcknowledgmentActivity.getPath();

          List<User> remainingUsers = deletedFileFilter.get(file);

          if (remainingUsers == null) {
            log.warn(
                "Received unexpected deletion acknowledgment for file that is not filtered: "
                    + source
                    + " - "
                    + file);

            return;

          } else if (!remainingUsers.contains(source)) {
            log.warn(
                "Received acknowledgment for file deletion from unexpected user: "
                    + source
                    + " - "
                    + file);

            return;
          }

          log.debug("Received deletion acknowledgment from " + source + " for " + file);

          remainingUsers.remove(source);

          if (remainingUsers.isEmpty()) {
            log.debug(
                "Dropping activity filter for " + file + " as all acknowledgments were received");

            deletedFileFilter.remove(file);
          }
        }
      };

  /**
   * Session listener updating the held map of filtered files when participants leave the session or
   * project are removed from the session.
   */
  private final ISessionListener sessionListener =
      new ISessionListener() {
        @Override
        public void userLeft(User user) {
          Iterator<Entry<SPath, List<User>>> iterator = deletedFileFilter.entrySet().iterator();
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
          Iterator<Entry<SPath, List<User>>> iterator = deletedFileFilter.entrySet().iterator();
          while (iterator.hasNext()) {
            SPath file = iterator.next().getKey();

            if (file.getProject().equals(project)) {
              log.debug(
                  "Dropping activity filter for "
                      + file
                      + " as it is no longer part of the session");

              iterator.remove();
            }
          }
        }
      };

  /**
   * Creates a new deleted file filter. The passed method is called every time a file deletion is
   * detected <b>after</b> the file is added to the map of deleted file.
   *
   * @param sarosSession the current saros session
   * @param fileDeletionHandler method that is called every time a file deletion is detected
   */
  ResourceActivityFilter(ISarosSession sarosSession, Consumer<SPath> fileDeletionHandler) {
    this.sarosSession = sarosSession;
    this.fileDeletionHandler = fileDeletionHandler;

    this.deletedFileFilter = new ConcurrentHashMap<>();
  }

  /**
   * Adds the deleted file to the held map of deleted shared files. This causes activities for such
   * files to be detected as filtered out by {@link #isFiltered(IActivity)} until it is created
   * again (or all deletion acknowledgments were received). Subsequently calls {@link
   * #fileDeletionHandler} with the deleted file.
   *
   * <p>Does nothing if the passed activity is not a {@link FileActivity} or does not have the type
   * {@link FileActivity.Type#REMOVED} or {@link FileActivity.Type#MOVED}.
   *
   * <p>Ignores file move activities where the origin and destination path is the same.
   *
   * @param activity the activity to handle
   * @see #deletedFileFilter
   * @see #handleFileCreation(IActivity)
   */
  void handleFileDeletion(IActivity activity) {
    if (!(activity instanceof FileActivity)) {
      return;
    }

    FileActivity fileActivity = (FileActivity) activity;

    SPath removedFile;
    if (fileActivity.getType() == FileActivity.Type.REMOVED) {
      removedFile = fileActivity.getPath();

    } else if (fileActivity.getType() == FileActivity.Type.MOVED
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

      deletedFileFilter.put(removedFile, remoteUsers);
    }

    fileDeletionHandler.accept(removedFile);
  }

  /**
   * Removes the created file from the held map of deleted shared files. This causes activities for
   * the files to no longer be detected as filtered out by {@link #isFiltered(IActivity)}.
   *
   * <p>Does nothing if the passed activity is not a {@link FileActivity} or does not have the type
   * {@link FileActivity.Type#CREATED} or {@link FileActivity.Type#MOVED}.
   *
   * @param activity the activity to handle
   * @see #deletedFileFilter
   * @see #handleFileDeletion(IActivity)
   */
  void handleFileCreation(IActivity activity) {
    if (!(activity instanceof FileActivity)) {
      return;
    }

    FileActivity fileActivity = (FileActivity) activity;

    if (fileActivity.getType() == FileActivity.Type.MOVED
        || fileActivity.getType() == FileActivity.Type.CREATED) {

      SPath addedFile = fileActivity.getPath();

      if (deletedFileFilter.containsKey(addedFile)) {
        log.debug("Removing activity filter for re-created file " + addedFile);

        deletedFileFilter.remove(addedFile);
      }
    }
  }

  /**
   * Returns whether the passed activity is filtered out. This is determined by the held map of
   * deleted files.
   *
   * <p>Non-resource activities are never detected as being filtered. Furthermore, resource
   * activities dealing with the tear-down of the internal Saros state related to the deleted
   * resource are never detected as being filtered. This applies to the following kinds of
   * activities:
   *
   * <ul>
   *   <li>a {@link ChecksumActivity} with the content {@link ChecksumActivity#NON_EXISTING_DOC} (as
   *       it is used to confirm the file deletion; necessary for the consistency logic)
   *   <li>a {@link EditorActivity} of the type {@link EditorActivity.Type#CLOSED} (as it is used to
   *       confirm that the editor was closed; necessary for the user editor state logic)
   *   <li>a {@link DeletionAcknowledgmentActivity} (as it is used confirm that the file deletion
   *       was processed; necessary for the resource activity filter logic)
   * </ul>
   *
   * @param activity the activity to check
   * @return whether the passed activity is filtered out
   * @see #handleFileDeletion(IActivity)
   * @see #handleFileCreation(IActivity)
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

    boolean pathIsFiltered = deletedFileFilter.containsKey(path);

    if (pathIsFiltered) {
      if (activity instanceof ChecksumActivity) {
        ChecksumActivity checksumActivity = (ChecksumActivity) activity;

        return checksumActivity.getHash() != ChecksumActivity.NON_EXISTING_DOC
            && checksumActivity.getLength() != ChecksumActivity.NON_EXISTING_DOC;

      } else if (activity instanceof EditorActivity) {
        EditorActivity editorActivity = (EditorActivity) activity;

        return EditorActivity.Type.CLOSED != editorActivity.getType();
      }
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
