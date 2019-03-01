package saros.editor;

import java.util.concurrent.CopyOnWriteArrayList;
import saros.activities.SPath;
import saros.activities.TextEditActivity;
import saros.activities.TextSelectionActivity;
import saros.session.User;

/** {@link ISharedEditorListener} which can dispatch events to multiple listeners. */
public class SharedEditorListenerDispatch implements ISharedEditorListener {

  private CopyOnWriteArrayList<ISharedEditorListener> editorListeners =
      new CopyOnWriteArrayList<ISharedEditorListener>();

  /**
   * Adds a listener to the dispatcher, causing all events to be forwarded to it.
   *
   * @param listener the listener to add
   */
  public void add(ISharedEditorListener listener) {
    editorListeners.addIfAbsent(listener);
  }

  /**
   * Removes a listener from the dispatcher that was previously added with {@link #add}. It will not
   * be forwarded any events afterwards.
   *
   * @param listener the listener to remove
   */
  public void remove(ISharedEditorListener listener) {
    editorListeners.remove(listener);
  }

  @Override
  public void editorActivated(User user, SPath filePath) {
    for (ISharedEditorListener listener : editorListeners) listener.editorActivated(user, filePath);
  }

  @Override
  public void editorClosed(User user, SPath filePath) {
    for (ISharedEditorListener listener : editorListeners) listener.editorClosed(user, filePath);
  }

  @Override
  public void followModeChanged(User target, boolean isFollowed) {
    for (ISharedEditorListener listener : editorListeners)
      listener.followModeChanged(target, isFollowed);
  }

  @Override
  public void textEdited(TextEditActivity textEdit) {
    for (ISharedEditorListener listener : editorListeners) listener.textEdited(textEdit);
  }

  @Override
  public void textSelectionChanged(TextSelectionActivity selection) {
    for (ISharedEditorListener listener : editorListeners) listener.textSelectionChanged(selection);
  }

  @Override
  public void jumpedToUser(User target) {
    for (ISharedEditorListener listener : editorListeners) listener.jumpedToUser(target);
  }
}
