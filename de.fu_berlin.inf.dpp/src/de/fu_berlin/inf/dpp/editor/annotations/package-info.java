/**
 * <h1>Editor Annotations Overview</h1>
 * 
 * The Editor Annotation manages the color highlighting of text done by a user.
 * 
 * The Editor Annotation comprises of four classes:
 * 
 * <ul>
 *
 * <li>the {@link SarosAnnotation} extends {@link org.eclipse.jface.text.source.Annotation}. Manages the coloring of text-highlighting. </li>
 * 
 * <li>the {@link ContributionAnnotation} used by {@link de.fu_berlin.inf.dpp.editor.internal.ContributionAnnotationManager}. Marks text contribution done by a user</li>
 * 
 * <li>the {@link SelectionAnnotation} extends {@link SarosAnnotation}. Marks selected text done by user</li>
 * 
 * <li>the {@link ViewportAnnotation} extends {@link SarosAnnotation}. Marks the viewport on the side of the editor view. </li>
 * 
 * </ul>
 */
package de.fu_berlin.inf.dpp.editor.annotations;

