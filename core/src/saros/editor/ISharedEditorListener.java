package saros.editor;

import saros.activities.SPath;
import saros.activities.TextEditActivity;
import saros.activities.TextSelectionActivity;
import saros.session.User;

/**
 * Listener type for events related to local and remote {@link IEditorManager editors} which belong
 * to the current session.
 */
public interface ISharedEditorListener {

  /**
   * Fired when a user activates (moves focus to) an editor. Implies that the editor has been opened
   * if it wasn't open already.
   *
   * @param filePath project-relative path of the file associated with the editor, or <code>null
   *     </code> if the user activated an editor that is not associated with a shared file (or has
   *     no open editor at all)
   * @param user the user who activated the editor (may be the local user)
   */
  public default void editorActivated(User user, SPath filePath) {
    // NOP
  }

  /**
   * Fired when a user closes an editor.
   *
   * @param user the user who closed the editor (may be the local user)
   * @param filePath project-relative path of the file associated with the closed editor
   */
  public default void editorClosed(User user, SPath filePath) {
    // NOP
  }

  /**
   * Fired when the local user starts or stops following a remote user.
   *
   * @param target the user who will be or was being followed
   * @param isFollowed <code>true</code> if following has started, <code>false</code> if it has
   *     ended
   * @deprecated Implement {@link IFollowModeListener} instead and register to the {@link
   *     FollowModeManager}.
   */
  @Deprecated
  public default void followModeChanged(User target, boolean isFollowed) {
    // NOP
  }

  /**
   * Fired when a user changes an editor's text content. This should only be treated as an event
   * notification; it should not be assumed that the change has already been applied locally when
   * this method is called.
   */
  public default void textEdited(TextEditActivity textEdit) {
    // NOP
  }

  /**
   * Fired when a user changes the text selection in an editor. The change has already been applied
   * locally when this method is called.
   *
   * @param selection The text selection as a {@link TextSelectionActivity}
   */
  public default void textSelectionChanged(TextSelectionActivity selection) {
    // NOP
  }

  /**
   * Fired when the local user uses the "jump to user" feature to jump to the active editor and
   * viewport of a remote user.
   *
   * @param target the user being jumped to
   */
  public default void jumpedToUser(User target) {
    // NOP
  }
}
