/**
 *
 *
 * <h1>Editor Internal Overview</h1>
 *
 * Internal mechanics for the editor views, including the handling of reverting files, providing
 * awareness information by annotations etc.
 *
 * <ul>
 *   <li>the {@link saros.editor.internal.ContributionAnnotationManager} keeps history about added
 *       annotations and removes old ones
 *   <li>the {@link saros.editor.internal.EditorAPI} realizes basic text editor interactions.
 *   <li>the {@link saros.editor.internal.SafePartListener2} calls to another IPartListener2, e.g.
 *       like {@link saros.editor.EditorPartListener}, to catch all exceptions and print them to the
 *       log
 *   <li>the {@link saros.editor.internal.SharedDocumentProvider} informs users that they need to
 *       have write access to edit the shared document.
 * </ul>
 */
package saros.editor.internal;
