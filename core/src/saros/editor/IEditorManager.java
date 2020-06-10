package saros.editor;

import java.util.Set;
import saros.editor.text.LineRange;
import saros.editor.text.TextSelection;
import saros.filesystem.IFile;
import saros.filesystem.IReferencePoint;
import saros.session.User;
import saros.util.LineSeparatorNormalizationUtil;

/**
 * Tracks and provides access to editors for the set of files shared in the currently running
 * session. In this context, <i>editor</i> refers to an open text file displayed to the user for
 * viewing and editing.
 *
 * <p>Editors are referred to by the {@link IFile files} they are associated with. Whenever
 * possible, {@link IEditorManager} will make it transparent to the caller whether there is actually
 * an open editor for the file; for instance, {@link #getContent(IFile)} automatically falls back to
 * retrieving the content directly from the file instead of from the editor.
 *
 * <p>{@link IEditorManager} tracks locally open editors. Changes to local editors (such as editors
 * getting opened, closed, or their content changed) are reported to remote sites as {@link
 * saros.activities.IActivity activities}. Objects can react to both local and remote editor events
 * by installing an {@link ISharedEditorListener}.
 *
 * <p>All methods of {@link IEditorManager} automatically take care of any required synchronization
 * with the UI thread and can be safely called from any thread.
 */
public interface IEditorManager {

  /**
   * Open the editor window of given {@link IFile}.
   *
   * @param file file of the editor to open.
   * @param activate Determines whether the newly opened editor should get the focus or should be
   *     opened in the background.
   */
  void openEditor(IFile file, boolean activate);

  /**
   * Returns all shared files for which an editor is currently open locally.
   *
   * @return all shared files for which an editor is currently open locally
   */
  Set<IFile> getOpenEditors();

  /**
   * Returns the text content of the local document associated with the specified file.
   *
   * <p>The document content is the content that is displayed when the file is opened in a text
   * editor.
   *
   * @param file the file whose content should be returned
   * @return the text content of the matching local document or <code>null</code> if the file does
   *     not exist or no document could be obtained for the file
   */
  String getContent(IFile file);

  /**
   * Returns the normalized result of {@link #getContent(IFile)}.
   *
   * <p>Normalized text only contains Unix line separators.
   *
   * @param file the file whose content should be returned
   * @return the normalized text content of the matching local document or <code>null</code> if the
   *     file does not exist or no document could be obtained for the file
   * @see #getContent(IFile)
   * @see LineSeparatorNormalizationUtil
   */
  String getNormalizedContent(IFile file);

  /**
   * Saves the local editors of all shared files belonging to the given reference point. If <code>
   * null</code> is passed, the shared files of all reference points will be saved.
   *
   * @param referencePoint the reference point whose editors should be saved, or <code>null</code>
   *     to save all editors
   */
  void saveEditors(IReferencePoint referencePoint);

  /**
   * Close the editor of given {@link IFile}.
   *
   * @param file the file of which the editor should be closed
   */
  void closeEditor(IFile file);

  /**
   * Adjusts viewport. Focus is set on the center of the range, but priority is given to selected
   * lines. Either range or selection can be <code>null</code>, but not both.
   *
   * @param file the file for the open Editor
   * @param range viewport of the followed user. Can be <code>null</code>.
   * @param selection text selection of the followed user. Can be <code>null</code>.
   */
  void adjustViewport(IFile file, LineRange range, TextSelection selection);

  /**
   * Locally opens the editor that the User {@code target} has currently open, adjusts the viewport,
   * and calls {@link ISharedEditorListener#jumpedToUser(User)} to inform the session participants
   * of the jump.
   */
  void jumpToUser(User target);

  /**
   * Adds an {@link ISharedEditorListener} to listen for changes such as editors getting opened,
   * closed or their content changed.
   *
   * @param listener editor listener to add
   */
  void addSharedEditorListener(ISharedEditorListener listener);

  /**
   * Removes an {@link ISharedEditorListener} that was previously added with {@link
   * #addSharedEditorListener(ISharedEditorListener)}.
   *
   * @param listener editor listener to remove
   */
  void removeSharedEditorListener(ISharedEditorListener listener);
}
