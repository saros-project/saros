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
 *   <li>the {@link AbstractSharedEditorListener} is an empty stub
 *   <li>the {@link DirtyStateListener} synchronizes file save events with shared documents.
 *   <li>the {@link EditorManager} handles and synchronizes all editors in a session.
 *   <li>the {@link EditorPool} manages EditorParts of the local users.
 *   <li>the {@link ISharedEditorListener} is an interface for synchronizing editor tabs
 *   <li>the {@link RemoteWriteAccessManager} locally executes remote user activities (like editing
 *       in an editor or closing it) for open editor views
 *   <li>the {@link SharedEditorListenerDispatch} dispatchs to a changing set of {@link
 *       ISharedEditorListener}s
 *   <li>the {@link StoppableDocumentListener} informs the given EditorManager of changes before
 *       they occur in a document (using documentAboutToBeChanged)
 * </ul>
 */
package saros.editor;
