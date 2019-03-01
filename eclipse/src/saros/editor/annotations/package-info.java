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
 *   <li>the {@link SarosAnnotation} extends {@link org.eclipse.jface.text.source.Annotation}.
 *       Manages the coloring of text-highlighting.
 *   <li>the {@link ContributionAnnotation} used by {@link
 *       saros.editor.internal.ContributionAnnotationManager}. Marks text contribution done by a
 *       user
 *   <li>the {@link SelectionAnnotation} extends {@link SarosAnnotation}. Marks selected text done
 *       by user
 *   <li>the {@link ViewportAnnotation} extends {@link SarosAnnotation}. Marks the viewport on the
 *       side of the editor view.
 * </ul>
 */
package saros.editor.annotations;
