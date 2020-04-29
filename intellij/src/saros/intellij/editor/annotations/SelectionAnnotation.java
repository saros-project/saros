package saros.intellij.editor.annotations;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.filesystem.IFile;
import saros.intellij.editor.colorstorage.ColorManager;
import saros.intellij.editor.colorstorage.ColorManager.ColorKeys;
import saros.session.User;

/**
 * Represents a Saros selection annotation. Selection annotations are used during a session to
 * highlight the text sections that are currently selected by other participants.
 */
class SelectionAnnotation extends AbstractEditorAnnotation {

  /**
   * Creates a selection annotation with the given parameters.
   *
   * <p>A selection annotation must have exactly one annotation range.
   *
   * @param user the user the annotation belongs to and whose color is used
   * @param file the file the annotation belongs to
   * @param start the start offset of the annotation
   * @param end the end offset of the annotation
   * @param editor the editor the annotation is displayed in
   * @see AbstractEditorAnnotation#AbstractEditorAnnotation(User, IFile, Editor, List)
   * @throws IllegalStateException if some parts of the annotation are located after the file end
   */
  SelectionAnnotation(
      @NotNull User user, @NotNull IFile file, int start, int end, @Nullable Editor editor) {

    super(user, file, editor, prepareAnnotationRange(user, file, start, end, editor));
  }

  private static List<AnnotationRange> prepareAnnotationRange(
      @NotNull User user, @NotNull IFile file, int start, int end, @Nullable Editor editor) {

    AnnotationRange annotationRange;

    if (editor != null) {
      TextAttributes selectionTextAttributes = getSelectionTextAttributes(editor, user);

      RangeHighlighter rangeHighlighter =
          addRangeHighlighter(start, end, editor, selectionTextAttributes, file);

      if (rangeHighlighter == null) {
        throw new IllegalStateException(
            "Failed to create range highlighter for range ("
                + start
                + ","
                + end
                + ") for file "
                + file);
      }

      annotationRange = new AnnotationRange(start, end, rangeHighlighter);

    } else {
      annotationRange = new AnnotationRange(start, end);
    }

    return Collections.singletonList(annotationRange);
  }

  /**
   * Returns the text attributes used for selection annotation range highlighters.
   *
   * @param editor the editor to which the annotation belongs
   * @param user the user to whom the annotation belongs
   * @return the text attributes used for selection annotation range highlighters
   */
  // TODO make private once migration is complete
  static TextAttributes getSelectionTextAttributes(@NotNull Editor editor, @NotNull User user) {

    // Retrieve color keys based on the color ID selected by this user. This will automatically
    // fall back to default colors, if no colors for the given ID are available.
    ColorKeys colorKeys = ColorManager.getColorKeys(user.getColorID());

    TextAttributesKey highlightColorKey = colorKeys.getSelectionColorKey();

    // Resolve the correct text attributes based on the currently configured IDE scheme.
    return editor.getColorsScheme().getAttributes(highlightColorKey);
  }
}
