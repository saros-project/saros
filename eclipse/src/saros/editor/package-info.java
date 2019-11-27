/**
 *
 *
 * <h1>Editor Overview</h1>
 *
 * contains the whole editor handling and synchronization.
 *
 * <p>This package comprises of the following subpackages:
 *
 * <ul>
 *   <li>package annotations --- this package contains functions for the annotation of text,
 *       selection and viewport
 *   <li>package internal--- manages the editors, their views and the needed listeners
 *   <li>package remote -- holds the state of the Editors of the other users in the session
 *   <li>the {@link saros.editor.DirtyStateListener} synchronizes file save events with shared
 *       documents.
 *   <li>the {@link saros.editor.EditorManager} handles and synchronizes all editors in a session.
 *   <li>the {@link saros.editor.EditorPool} manages EditorParts of the local users.
 *   <li>the {@link saros.editor.RemoteWriteAccessManager} locally executes remote user activities
 *       (like editing in an editor or closing it) for open editor views
 *   <li>the {@link saros.editor.SharedEditorListenerDispatch} dispatches to a changing set of
 *       {@link saros.editor.ISharedEditorListener}s
 *   <li>the {@link saros.editor.StoppableDocumentListener} informs the given EditorManager of
 *       changes before they occur in a document (using documentAboutToBeChanged) *
 *   <li>the {@link saros.editor.EditorPartListener} listens to the editor's view state
 *       changes.Transmit the result to {@link saros.editor.EditorManager} *
 *   <li>the {@link saros.editor.EditorListener} listen for selection and viewport changes in the
 *       text content of the editor view. Transmit the result to {@link saros.editor.EditorManager}
 * </ul>
 */
package saros.editor;
