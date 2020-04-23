package saros.editor.remote;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import org.apache.log4j.Logger;
import saros.activities.EditorActivity;
import saros.activities.TextSelectionActivity;
import saros.activities.ViewportActivity;
import saros.editor.text.LineRange;
import saros.editor.text.TextSelection;
import saros.filesystem.IFile;
import saros.session.AbstractActivityConsumer;
import saros.session.IActivityConsumer;

/**
 * Instances of this class represent the state of the editors of one user, including the viewports
 * and selections in each of these.
 */
public class UserEditorState {

  private static final Logger log = Logger.getLogger(UserEditorState.class);

  private final LinkedHashMap<IFile, EditorState> openEditors = new LinkedHashMap<>();

  private EditorState activeEditor;

  final IActivityConsumer consumer =
      new AbstractActivityConsumer() {

        @Override
        public void receive(EditorActivity editorActivity) {
          IFile file = editorActivity.getResource();

          switch (editorActivity.getType()) {
            case ACTIVATED:
              activated(file);
              break;
            case SAVED:
              break;
            case CLOSED:
              closed(file);
              break;
            default:
              log.warn("Unexpected type: " + editorActivity.getType());
              assert false;
          }
        }

        @Override
        public void receive(ViewportActivity viewportActivity) {
          IFile file = viewportActivity.getResource();

          if (!openEditors.containsKey(file)) {
            log.warn("Viewport for editor which was never activated: " + file);
            return;
          }

          LineRange lineRange =
              new LineRange(viewportActivity.getStartLine(), viewportActivity.getNumberOfLines());

          updateEditorState(file).setViewport(lineRange);
        }

        @Override
        public void receive(TextSelectionActivity textSelectionActivity) {
          IFile file = textSelectionActivity.getResource();

          if (!openEditors.containsKey(file)) {
            log.warn("received selection for editor which was never activated: " + file);
            return;
          }

          TextSelection selection = textSelectionActivity.getSelection();

          updateEditorState(file).setSelection(selection);
        }
      };

  /* Helper */

  private synchronized EditorState updateEditorState(IFile file) {
    EditorState result = openEditors.get(file);
    if (result == null) {
      result = new EditorState(file);
      openEditors.put(file, result);
    }
    return result;
  }

  private synchronized void activated(IFile file) {
    if (file == null) {
      activeEditor = null;
    } else {
      activeEditor = openEditors.get(file);

      // Create new or reinsert at the end of the Map
      if (activeEditor == null) {
        activeEditor = new EditorState(file);
      } else {
        openEditors.remove(file);
      }
      openEditors.put(file, activeEditor);
    }
  }

  private synchronized void closed(IFile file) {
    EditorState state = openEditors.remove(file);

    if (state == null) {
      log.warn("Removing an editor which has never been added: " + file);
      return;
    }

    if (state.equals(activeEditor)) {
      activeEditor = null;
    }
  }

  /* Public methods */

  /** @return a list of files referenced by the open editors */
  public synchronized Set<IFile> getOpenEditors() {
    return new HashSet<>(openEditors.keySet());
  }

  /**
   * Returns an {@link EditorState} representing an editor
   *
   * @param file The file for which the editor state should be retrieved
   * @return An immutable {@link EditorState}, or <code>null</code> if there is no open editor for
   *     the given file (i.e. it's not in {@link #getOpenEditors()})
   */
  public synchronized EditorState getEditorState(IFile file) {
    EditorState result = openEditors.get(file);

    if (result == null) {
      return null;
    }

    return new EditorState(result.getFile(), result.getViewport(), result.getSelection());
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
        activeEditor.getFile(), activeEditor.getViewport(), activeEditor.getSelection());
  }
}
