/**
 *
 *
 * <h1>Editor Annotations Overview</h1>
 *
 * The Editor Annotation manages the color highlighting of text done by a user.
 *
 * <p>The Editor Annotation comprises of four classes:
 *
 * <ul>
 *   <li>the {@link saros.editor.annotations.SarosAnnotation} extends {@link
 *       org.eclipse.jface.text.source.Annotation}. Manages the coloring of text-highlighting.
 *   <li>the {@link saros.editor.annotations.ContributionAnnotation} used by {@link
 *       saros.editor.internal.ContributionAnnotationManager}. Marks text contribution done by a
 *       user
 *   <li>the {@link saros.editor.annotations.SelectionAnnotation} extends {@link
 *       saros.editor.annotations.SarosAnnotation}. Marks selected text done by user
 *   <li>the {@link saros.editor.annotations.ViewportAnnotation} extends {@link
 *       saros.editor.annotations.SarosAnnotation}. Marks the viewport on the side of the editor
 *       view.
 * </ul>
 */
package saros.editor.annotations;
