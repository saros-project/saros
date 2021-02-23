package saros.intellij.editor.annotations;

import static saros.intellij.editor.annotations.AnnotationHighlighterLayers.CONTRIBUTION_HIGHLIGHTER_LAYER;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
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
  private static Logger log = Logger.getLogger(ContributionAnnotation.class);

  /**
   * Creates a contribution annotation with the given arguments.
   *
   * <p>The contribution annotation must be split into one character wide ranges for easier
   * handling. This means all annotation ranges for a contribution annotation must be exactly one
   * character long.
   *
   * @param user the user the annotation belongs to and whose color is used
   * @param file the file the annotation belongs to
   * @param start the start offset of the annotation
   * @param end the end offset of the annotation
   * @param editor the editor the annotation is displayed in
   * @see AbstractEditorAnnotation#AbstractEditorAnnotation(User, IFile, Editor, List)
   * @throws IllegalStateException if some parts of the annotation are located after the file end
   */
  ContributionAnnotation(
      @NotNull User user, @NotNull IFile file, int start, int end, @Nullable Editor editor) {

    super(user, file, editor, prepareAnnotationRanges(start, end, editor, user, file));
  }

  /**
   * Creates a list of one character long annotation ranges covering the given range. None of the
   * annotation ranges has a range highlighter associated with it.
   *
   * @param start the start of the range
   * @param end the end of the range
   * @return a list of one character long annotation ranges covering the given range
   */
  private static List<AnnotationRange> prepareAnnotationRanges(
      int start, int end, @Nullable Editor editor, @NotNull User user, @NotNull IFile file) {

    checkRange(start, end);

    List<AnnotationRange> annotationRanges = new ArrayList<>();

    TextAttributes contributionTextAttributes;
    if (editor != null) {
      contributionTextAttributes = getContributionTextAttributes(editor, user);
    } else {
      contributionTextAttributes = null;
    }

    for (int currentStart = start; currentStart < end; currentStart++) {
      int currentEnd = currentStart + 1;

      AnnotationRange annotationRange;
      if (editor != null) {
        RangeHighlighter rangeHighlighter =
            addRangeHighlighter(
                currentStart,
                currentEnd,
                editor,
                contributionTextAttributes,
                CONTRIBUTION_HIGHLIGHTER_LAYER,
                file);

        if (rangeHighlighter == null) {
          throw new IllegalStateException(
              "Failed to create range highlighter for range ("
                  + currentStart
                  + ","
                  + currentEnd
                  + ") for file "
                  + file);
        }

        annotationRange = new AnnotationRange(currentStart, currentEnd, rangeHighlighter);

      } else {

        annotationRange = new AnnotationRange(currentStart, currentEnd);
      }

      annotationRanges.add(annotationRange);
    }

    return annotationRanges;
  }

  // TODO check whether local representation already added; see #958
  @Override
  void addLocalRepresentation(@NotNull Editor editor) {
    User user = getUser();

    TextAttributes textAttributes = getContributionTextAttributes(editor, user);

    addEditor(editor);

    IFile file = getFile();

    for (AnnotationRange annotationRange : getAnnotationRanges()) {
      int start = annotationRange.getStart();
      int end = annotationRange.getEnd();

      RangeHighlighter rangeHighlighter =
          AbstractEditorAnnotation.addRangeHighlighter(
              start, end, editor, textAttributes, CONTRIBUTION_HIGHLIGHTER_LAYER, file);

      if (rangeHighlighter != null) {
        annotationRange.addRangeHighlighter(rangeHighlighter);

      } else {
        log.warn(
            "Could not create range highlighter for range " + annotationRange + " for " + this);
      }
    }
  }

  /**
   * Returns the text attributes used for contribution annotation range highlighters.
   *
   * @param editor the editor to which the annotation belongs
   * @param user the user to whom the annotation belongs
   * @return the text attributes used for contribution annotation range highlighters
   */
  private static TextAttributes getContributionTextAttributes(
      @NotNull Editor editor, @NotNull User user) {

    // Retrieve color keys based on the color ID selected by this user. This will automatically
    // fall back to default colors, if no colors for the given ID are available.
    ColorKeys colorKeys = ColorManager.getColorKeys(user.getColorID());

    TextAttributesKey highlightColorKey = colorKeys.getContributionColorKey();

    // Resolve the correct text attributes based on the currently configured IDE scheme.
    return editor.getColorsScheme().getAttributes(highlightColorKey);
  }
}
