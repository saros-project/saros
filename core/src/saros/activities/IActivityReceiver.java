package saros.activities;

/**
 * This interface is for implementing multiple dispatch in Java (see <a
 * href="http://en.wikipedia.org/wiki/Multiple_dispatch">Wikipedia entry</a>).
 *
 * <p>In our case we want to call one of the specialized {@code receive()} methods in {@link
 * IActivityReceiver} for a given {@link IActivity}. For instance, if an activity is a {@link
 * TextSelectionActivity}, we want the method {@link #receive(TextSelectionActivity)} to be called.
 */
public interface IActivityReceiver {
  /**
   * @JTourBusStop 3, Creating a new Activity type, Triple dispatch interface:
   *
   * <p>This interface provides a receive() method for each activity implementation. This is part of
   * the so-called "triple dispatch" (see "Activity sending" tour). For now, it is enough to know
   * that you need to declare a new receive() method with your new Activity type in this interface
   * (follow the alphabet, please), and then go to the next stop in this tour.
   */

  /** */
  default void receive(ChangeColorActivity changeColorActivity) {
    /*NOP*/
  }

  default void receive(ChecksumActivity checksumActivity) {
    /*NOP*/
  }

  default void receive(ChecksumErrorActivity checksumErrorActivity) {
    /*NOP*/
  }

  default void receive(DeletionAcknowledgmentActivity deletionAcknowledgmentActivity) {
    /*NOP*/
  }

  default void receive(EditorActivity editorActivity) {
    /*NOP*/
  }

  default void receive(FileActivity fileActivity) {
    /*NOP*/
  }

  default void receive(FolderCreatedActivity folderCreatedActivity) {
    /*NOP*/
  }

  default void receive(FolderDeletedActivity folderDeletedActivity) {
    /*NOP*/
  }

  default void receive(FolderMovedActivity folderMovedActivity) {
    /*NOP*/
  }

  default void receive(JupiterActivity jupiterActivity) {
    /*NOP*/
  }

  default void receive(NOPActivity nopActivity) {
    /*NOP*/
  }

  default void receive(PermissionActivity permissionActivity) {
    /*NOP*/
  }

  default void receive(ProgressActivity progressActivity) {
    /*NOP*/
  }

  default void receive(StartFollowingActivity startFollowingActivity) {
    /*NOP*/
  }

  default void receive(StopActivity stopActivity) {
    /*NOP*/
  }

  default void receive(StopFollowingActivity stopFollowingActivity) {
    /*NOP*/
  }

  default void receive(TextEditActivity textEditActivity) {
    /*NOP*/
  }

  default void receive(TextSelectionActivity textSelectionActivity) {
    /*NOP*/
  }

  default void receive(ViewportActivity viewportActivity) {
    /*NOP*/
  }
}
