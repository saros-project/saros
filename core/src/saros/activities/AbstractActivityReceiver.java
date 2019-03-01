package saros.activities;

/**
 * Abstract implementation of IActivityReceiver, which does nothing.
 *
 * <p>Useful, if just interested in some particular IActivity
 */
public abstract class AbstractActivityReceiver implements IActivityReceiver {

  /**
   * @JTourBusStop 4, Creating a new Activity type, Triple dispatch abstract class:
   *
   * <p>Instead of creating stubs for all receive() variants for IActivity types you're not even
   * interested in, you can extend this abstract class and just override the one (or few) method(s)
   * you actually care about.
   *
   * <p>So once you added a new receive() variant to IActivityReceiver, make sure to add a
   * null-implementation for your new activity type here, to unburden all *other* IActivityReceiver
   * implementations from having to implement the receive() method for *your* new activity.
   */

  /** */
  @Override
  public void receive(ChangeColorActivity changeColorActivity) {
    // do nothing
  }

  @Override
  public void receive(ChecksumActivity checksumActivity) {
    // do nothing
  }

  @Override
  public void receive(ChecksumErrorActivity checksumErrorActivity) {
    // do nothing
  }

  @Override
  public void receive(EditorActivity editorActivity) {
    // do nothing
  }

  @Override
  public void receive(FileActivity fileActivity) {
    // do nothing
  }

  @Override
  public void receive(FolderCreatedActivity folderCreatedActivity) {
    // do nothing
  }

  @Override
  public void receive(FolderDeletedActivity folderDeletedActivity) {
    // do nothing
  }

  @Override
  public void receive(FolderMovedActivity folderMovedActivity) {
    // do nothing
  }

  @Override
  public void receive(JupiterActivity jupiterActivity) {
    // do nothing
  }

  @Override
  public void receive(NOPActivity nopActivity) {
    // do nothing
  }

  @Override
  public void receive(PermissionActivity permissionActivity) {
    // do nothing
  }

  @Override
  public void receive(ProgressActivity progressActivity) {
    // do nothing
  }

  @Override
  public void receive(StartFollowingActivity startFollowingActivity) {
    // do nothing
  }

  @Override
  public void receive(StopActivity stopActivity) {
    // do nothing
  }

  @Override
  public void receive(StopFollowingActivity stopFollowingActivity) {
    // do nothing
  }

  @Override
  public void receive(TextEditActivity textEditActivity) {
    // do nothing
  }

  @Override
  public void receive(TextSelectionActivity textSelectionActivity) {
    // do nothing
  }

  @Override
  public void receive(ViewportActivity viewportActivity) {
    // do nothing
  }
}
