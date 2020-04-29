package saros.intellij.editor.annotations;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.filesystem.IFile;
import saros.intellij.editor.colorstorage.ColorManager;
import saros.intellij.editor.colorstorage.ColorManager.ColorKeys;
import saros.session.User;

/**
 * Represents a Saros contribution annotation. Contribution annotations are used during a session to
 * highlight the text additions that were made by other participants.
 *
 * <p>To avoid additional management logic when a contribution annotation is split (when a
 * participant types inside a section currently highlighted by a contribution annotation from
 * another user), contribution annotations are initially split into one character long <code>
 * AnnotationRange</code>s. This splitting is <b>not</b> done automatically but has to be done by
 * the creator of the contribution annotation.
 */
class ContributionAnnotation extends AbstractEditorAnnotation {

  /**
   * Creates a contribution annotation with the given arguments.
   *
   * <p>The contribution annotation must be split into one character wide ranges for easier
   * handling. This means all annotation ranges for a contribution annotation must be exactly one
   * character long.
   *
   * @param user the user the annotation belongs to and whose color is used
   * @param file the file the annotation belongs to
   * @param editor the editor the annotation is displayed in
   * @param annotationRanges the range of the annotation
   * @see AbstractEditorAnnotation#AbstractEditorAnnotation(User, IFile, Editor, List)
   */
  ContributionAnnotation(
      @NotNull User user,
      @NotNull IFile file,
      @Nullable Editor editor,
      @NotNull List<AnnotationRange> annotationRanges) {

    super(user, file, editor, annotationRanges);

    annotationRanges.forEach(
        annotationRange -> {
          int length = annotationRange.getLength();

          if (length != 1) {
            throw new IllegalArgumentException(
                "Each AnnotationRange for a ContributionAnnotation has to "
                    + "be exactly one character long. Found length: "
                    + length);
          }
        });
  }

  /**
   * Returns the text attributes used for contribution annotation range highlighters.
   *
   * @param editor the editor to which the annotation belongs
   * @param user the user to whom the annotation belongs
   * @return the text attributes used for contribution annotation range highlighters
   */
  // TODO make private once migration is complete
  static TextAttributes getContributionTextAttributes(@NotNull Editor editor, @NotNull User user) {

    // Retrieve color keys based on the color ID selected by this user. This will automatically
    // fall back to default colors, if no colors for the given ID are available.
    ColorKeys colorKeys = ColorManager.getColorKeys(user.getColorID());

    TextAttributesKey highlightColorKey = colorKeys.getContributionColorKey();

    // Resolve the correct text attributes based on the currently configured IDE scheme.
    return editor.getColorsScheme().getAttributes(highlightColorKey);
  }
}
