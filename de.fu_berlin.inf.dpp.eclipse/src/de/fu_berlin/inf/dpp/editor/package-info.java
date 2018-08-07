/**
 * <h1>Editor Overview</h1>
 * 
 * contains the whole editor handling and synchronization.
 * 
 * This package comprises of the following subpackages:
 * 
 * <ul>
 * <li>package annotations --- this package contains functions for the annotation of text, selection and viewport</li>
 * 
 * <li>package internal--- manages the editors, their views and the needed listeners</li>
 * 
 * <li>package remote -- holds the state of the Editors of the other users in the session</li>
 * 
 * <li>the {@link AbstractSharedEditorListener} is an empty stub</li>
 * 
 * <li>the {@link DirtyStateListener} synchronizes file save events with shared documents.</li>
 * 
 * <li>the {@link EditorManager} handles and synchronizes all editors in a session.</li>
 * 
 * <li>the {@link EditorPool} manages EditorParts of the local users.</li>
 * 
 * <li>the {@link ISharedEditorListener} is an interface for synchronizing editor tabs</li>
 * 
 * <li>the {@link RemoteWriteAccessManager} locally executes remote user activities (like editing in an editor or closing it) for open editor views</li>
 * 
 * <li>the {@link SharedEditorListenerDispatch} dispatchs to a changing set of {@link ISharedEditorListener}s</li>
 * 
 * <li>the {@link StoppableDocumentListener} informs the given EditorManager of changes before they occur in a document (using documentAboutToBeChanged)</li>
 * 
 * </ul>
 */
package de.fu_berlin.inf.dpp.editor;

