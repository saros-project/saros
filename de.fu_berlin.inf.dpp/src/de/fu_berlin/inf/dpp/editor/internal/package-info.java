/**
 * <h1>Editor Internal Overview</h1>
 * 
 * Internal mechanics for the editor views, including the handling of reverting files, providing awareness information by annotations etc.
 *
 * <ul>
 * 
 * <li>the {@link AbstractFileBufferListener} is an empty stub. </li>
 * 
 * <li>the {@link ContributionAnnotationManager} keeps history about added annotations and removes old ones</li>
 * 
 * <li>the {@link IEditorAPI} implemented by {@link EditorAPI}. The least functionality to use the editor.</li>
 * 
 * <li>the {@link EditorAPI} realises basic text editor interactions.</li>
 * 
 * <li>the {@link EditorListener} listen for selection and viewport changes in the text content of the editor view. Transmit the result to {@link de.fu_berlin.inf.dpp.editor.EditorManager}</li>
 * 
 * <li>the {@link EditorPartListener} listens to the editor's view state changes.Transmit the result to {@link de.fu_berlin.inf.dpp.editor.EditorManager}</li>
 * 
 * <li>the {@link RevertBufferListener} listens for buffers of changes in saros-session-documments to react for "bufferDispose()" events and get control over the file revert operation</li>
 * 
 * <li>the {@link SafeDelegatingFileBufferListener} makes calls to the listener safe and is used by {@link RevertBufferListener}</li>
 * 
 * <li>the {@link SafePartListener2} calls to another IPartListener2, e.g. like {@link EditorPartListener}, to catch all exceptions and print them to the log</li>
 * 
 * <li>the {@link SharedDocumentProvider} informs users that they need to have write access to edit the shared document.</li>
 * 
 * </ul>
 */
package de.fu_berlin.inf.dpp.editor.internal;

