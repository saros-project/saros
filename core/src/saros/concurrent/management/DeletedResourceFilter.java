package saros.concurrent.management;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import saros.activities.ChecksumActivity;
import saros.activities.FileActivity;
import saros.activities.FileActivity.Type;
import saros.activities.IActivity;
import saros.activities.IResourceActivity;
import saros.activities.SPath;

/** Class to handle resource deletions and filter out activities for already deleted resources. */
class DeletedResourceFilter {
  /** Method passed by the CTOR caller that can be used to react to resource deletions. */
  private final Consumer<SPath> resourceDeletionHandler;

  /**
   * A set of shared resources that were deleted during the session. It is used to filter out
   * activities for such resources that were created before the other participants ran the
   * corresponding resource deletion activity locally.
   *
   * <p>The set is updated once the resource is recreated as we then want to handle activities for
   * it again.
   *
   * <p>This way of filtering can lead to issues when the order of activities is not preserved, e.g.
   * when we receive the content change for a new file is received before the file creation. Such
   * activities will be dropped, leading to inconsistencies.
   */
  // FIXME this set is never pruned for resources whose deletion was already processed by all
  //  participants
  private final Set<SPath> deletedResources;

  /**
   * Creates a new deleted resource filter. The passed method is called every time a resource
   * deletion is detected <b>after</b> the resource is added to the set of deleted resources.
   *
   * @param resourceDeletionHandler method that is called every time a resource deletion is detected
   */
  DeletedResourceFilter(Consumer<SPath> resourceDeletionHandler) {
    this.resourceDeletionHandler = resourceDeletionHandler;

    this.deletedResources = new CopyOnWriteArraySet<>();
  }

  /**
   * Adds the deleted resource to the held set of deleted shared resources. This causes activities
   * for such resources to be detected as filtered out by {@link #isFiltered(IActivity)} until it is
   * created again. Subsequently calls {@link #resourceDeletionHandler} with the deleted resource.
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

    deletedResources.add(removedFile);
    resourceDeletionHandler.accept(removedFile);
  }

  /**
   * Removes the created resource from the held set of deleted shared resources. This causes
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

      deletedResources.remove(addedFile);
    }
  }

  /**
   * Returns whether the passed activity is filtered out. This is determined by the held set of
   * deleted resources.
   *
   * <p>Non-resource activities are never detected as being filtered. Furthermore, resource
   * activities of the type {@link ChecksumActivity} that confirm the file deletion are never
   * detected as being filtered.
   *
   * @param activity the activity to check
   * @return whether the passed activity is filtered out
   * @see #handleResourceDeletion(IActivity)
   * @see #handleResourceCreation(IActivity)
   */
  boolean isFiltered(IActivity activity) {
    if (!(activity instanceof IResourceActivity)) {
      return false;
    }

    IResourceActivity resourceActivity = (IResourceActivity) activity;

    boolean pathIsFiltered = deletedResources.contains(resourceActivity.getPath());

    if (pathIsFiltered && activity instanceof ChecksumActivity) {
      ChecksumActivity checksumActivity = (ChecksumActivity) activity;

      return checksumActivity.getHash() != ChecksumActivity.NON_EXISTING_DOC
          && checksumActivity.getLength() != ChecksumActivity.NON_EXISTING_DOC;
    }

    return pathIsFiltered;
  }
}
