package saros.editor.remote;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import org.apache.log4j.Logger;
import saros.activities.EditorActivity;
import saros.activities.SPath;
import saros.activities.TextSelectionActivity;
import saros.activities.ViewportActivity;
import saros.editor.text.LineRange;
import saros.editor.text.TextSelection;
import saros.session.AbstractActivityConsumer;
import saros.session.IActivityConsumer;

/**
 * Instances of this class represent the state of the editors of one user, including the viewports
 * and selections in each of these.
 */
public class UserEditorState {

  private static final Logger LOG = Logger.getLogger(UserEditorState.class);

  private final LinkedHashMap<SPath, EditorState> openEditors =
      new LinkedHashMap<SPath, EditorState>();

  private EditorState activeEditor;

  final IActivityConsumer consumer =
      new AbstractActivityConsumer() {

        @Override
        public void receive(EditorActivity editorActivity) {
          SPath sPath = editorActivity.getPath();

          switch (editorActivity.getType()) {
            case ACTIVATED:
              activated(sPath);
              break;
            case SAVED:
              break;
            case CLOSED:
              closed(sPath);
              break;
            default:
              LOG.warn("Unexpected type: " + editorActivity.getType());
              assert false;
          }
        }

        @Override
        public void receive(ViewportActivity viewportActivity) {
          SPath path = viewportActivity.getPath();

          if (!openEditors.containsKey(path)) {
            LOG.warn("Viewport for editor which was never activated: " + path);
            return;
          }

          LineRange lineRange =
              new LineRange(viewportActivity.getStartLine(), viewportActivity.getNumberOfLines());

          updateEditorState(path).setViewport(lineRange);
        }

        @Override
        public void receive(TextSelectionActivity textSelectionActivity) {
          SPath path = textSelectionActivity.getPath();

          if (!openEditors.containsKey(path)) {
            LOG.warn("received selection for editor which was never activated: " + path);
            return;
          }

          TextSelection selection =
              new TextSelection(
                  textSelectionActivity.getOffset(), textSelectionActivity.getLength());

          updateEditorState(path).setSelection(selection);
        }
      };

  /* Helper */

  private synchronized EditorState updateEditorState(SPath path) {
    EditorState result = openEditors.get(path);
    if (result == null) {
      result = new EditorState(path);
      openEditors.put(path, result);
    }
    return result;
  }

  private synchronized void activated(SPath path) {
    if (path == null) {
      activeEditor = null;
    } else {
      activeEditor = openEditors.get(path);

      // Create new or reinsert at the end of the Map
      if (activeEditor == null) {
        activeEditor = new EditorState(path);
      } else {
        openEditors.remove(path);
      }
      openEditors.put(path, activeEditor);
    }
  }

  private synchronized void closed(SPath path) {
    EditorState state = openEditors.remove(path);

    if (state == null) {
      LOG.warn("Removing an editor which has never been added: " + path);
      return;
    }

    if (state.equals(activeEditor)) {
      activeEditor = null;
    }
  }

  /* Public methods */

  /** @return a list of path referenced by the open editors */
  public synchronized Set<SPath> getOpenEditors() {
    return new HashSet<SPath>(openEditors.keySet());
  }

  /**
   * Returns an {@link EditorState} representing an editor
   *
   * @param path The path of the file for which the editor state should be retrieved
   * @return An immutable {@link EditorState}, or <code>null</code> if there is no open editor for
   *     the given path (i.e. it's not in {@link #getOpenEditors()})
   */
  public synchronized EditorState getEditorState(SPath path) {
    EditorState result = openEditors.get(path);

    if (result == null) {
      return null;
    }

    return new EditorState(result.getPath(), result.getViewport(), result.getSelection());
  }

  /**
   * Returns the status of the currently active editor.
   *
   * @return An immutable {@link EditorState}, or <code>null</code> if the user has no currently
   *     active editor
   */
  public synchronized EditorState getActiveEditorState() {
    if (activeEditor == null) return null;

    return new EditorState(
        activeEditor.getPath(), activeEditor.getViewport(), activeEditor.getSelection());
  }
}
