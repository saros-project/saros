package de.fu_berlin.inf.dpp.editor;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.editor.text.LineRange;
import de.fu_berlin.inf.dpp.editor.text.TextSelection;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.session.User;
import java.util.Set;

/**
 * Tracks and provides access to editors for the set of files shared in the currently running
 * session. In this context, <i>editor</i> refers to an open text file displayed to the user for
 * viewing and editing.
 *
 * <p>Editors are referred to by {@link SPath paths} to the files they are associated with. Whenever
 * possible, {@link IEditorManager} will make it transparent to the caller whether there is actually
 * an open editor for the file; for instance, {@link #getContent(SPath)} automatically falls back to
 * retrieving the content directly from the file instead of from the editor.
 *
 * <p>{@link IEditorManager} tracks locally open editors. Changes to local editors (such as editors
 * getting opened, closed, or their content changed) are reported to remote sites as {@link
 * de.fu_berlin.inf.dpp.activities.IActivity activities}. Objects can react to both local and remote
 * editor events by installing an {@link ISharedEditorListener}.
 *
 * <p>All methods of {@link IEditorManager} automatically take care of any required synchronization
 * with the UI thread and can be safely called from any thread.
 */
public interface IEditorManager {

  /**
   * Open the editor window of given {@link SPath}.
   *
   * @param path Path of the Editor to open.
   * @param activate Determines whether the newly opened editor should get the focus or should be
   *     opened in the background.
   */
  public void openEditor(SPath path, boolean activate);

  /**
   * Returns the paths of all shared files for which an editor is currently open locally.
   *
   * @return paths of locally open shared files
   */
  public Set<SPath> getOpenEditors();

  /**
   * Returns the text content of the local editor associated with the specified file, or the content
   * of the file itself if there is currently no open editor for it.
   *
   * @param path path of the file whose content should be returned
   * @return the text content of the matching local editor or file, or <code>null</code> if no file
   *     with the given path exists locally
   */
  public String getContent(SPath path);

  /**
   * Saves the local editors of all shared files belonging to the given project. If <code>null
   * </code> is passed, the shared files of all projects will be saved.
   *
   * @param project the project whose editors should be saved, or <code>null</code> to save all
   *     editors
   */
  public void saveEditors(IProject project);

  /**
   * Close the editor of given {@link SPath}.
   *
   * @param path Path of the file of which the editor should be closed
   */
  public void closeEditor(SPath path);

  /**
   * Adjusts viewport. Focus is set on the center of the range, but priority
   * is given to selected lines. Either range or selection can be
   * <code>null</null>, but
   * not both.
   *
   * @param path
   *            Path of the open Editor
   * @param range
   *            viewport of the followed user. Can be <code>null</code>.
   * @param selection
   *            text selection of the followed user. Can be <code>null</code>.
   *
   */
  public void adjustViewport(SPath path, LineRange range, TextSelection selection);

  /**
   * Locally opens the editor that the User {@code target} has currently open, adjusts the viewport,
   * and calls {@link ISharedEditorListener#jumpedToUser(User)} to inform the session participants
   * of the jump.
   */
  public void jumpToUser(User target);

  /**
   * Adds an {@link ISharedEditorListener} to listen for changes such as editors getting opened,
   * closed or their content changed.
   *
   * @param listener editor listener to add
   */
  public void addSharedEditorListener(ISharedEditorListener listener);

  /**
   * Removes an {@link ISharedEditorListener} that was previously added with {@link
   * #addSharedEditorListener(ISharedEditorListener)}.
   *
   * @param listener editor listener to remove
   */
  public void removeSharedEditorListener(ISharedEditorListener listener);
}
