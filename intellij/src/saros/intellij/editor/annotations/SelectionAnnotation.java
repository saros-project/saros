package saros.intellij.editor.annotations;

import static com.intellij.openapi.editor.colors.EditorColors.CARET_COLOR;
import static saros.intellij.editor.annotations.AnnotationHighlighterLayers.CARET_HIGHLIGHTER_LAYER;
import static saros.intellij.editor.annotations.AnnotationHighlighterLayers.SELECTION_HIGHLIGHTER_LAYER;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import java.awt.Color;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.filesystem.IFile;
import saros.intellij.editor.colorstorage.ColorManager;
import saros.intellij.editor.colorstorage.ColorManager.ColorKeys;
import saros.session.User;

/**
 * Represents a Saros selection annotation. Selection annotations are used during a session to
 * highlight the text sections that are currently selected by other participants.
 *
 * <p>Selection annotations also include a caret annotation that is displayed either at the start or
 * end of the annotation, depending on {@link #isBackwardsSelection}. These caret annotations are
 * only held as a range highlighter when there is an open editor for the file and are dependent on
 * the start or end of the selection. Therefore, they don't have to be updated when the document
 * content changes without an open editor.
 */
class SelectionAnnotation extends AbstractEditorAnnotation {
  private static final Logger log = Logger.getLogger(SelectionAnnotation.class);

  /**
   * Whether the selection is a backwards selection. Backwards selections display the caret at the
   * start of the selection.
   */
  private final boolean isBackwardsSelection;

  /**
   * The caret annotation range highlighter. This reference is not <code>null</code> iff the held
   * editor reference is not <code>null</code>.
   */
  private RangeHighlighter caretHighlighter;

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
   * @param isBackwardsSelection whether the annotation represents a backwards selection
   * @see AbstractEditorAnnotation#AbstractEditorAnnotation(User, IFile, Editor, List)
   * @throws IllegalStateException if some parts of the annotation are located after the file end
   */
  SelectionAnnotation(
      @NotNull User user,
      @NotNull IFile file,
      int start,
      int end,
      @Nullable Editor editor,
      boolean isBackwardsSelection) {

    super(user, file, editor, prepareAnnotationRange(user, file, start, end, editor));

    this.isBackwardsSelection = isBackwardsSelection;

    if (editor != null) {
      List<AnnotationRange> annotationRanges = getAnnotationRanges();

      int numberOfAnnotations = annotationRanges.size();
      if (numberOfAnnotations != 1) {
        throw new IllegalStateException(
            "Encountered selection annotation with "
                + numberOfAnnotations
                + " annotation ranges - "
                + this);
      }

      AnnotationRange annotationRange = annotationRanges.get(0);

      addCaretRangeHighlighter(annotationRange, editor, user, file);
    }
  }

  private static List<AnnotationRange> prepareAnnotationRange(
      @NotNull User user, @NotNull IFile file, int start, int end, @Nullable Editor editor) {

    checkRange(start, end);

    AnnotationRange annotationRange;

    if (editor != null) {
      TextAttributes selectionTextAttributes = getSelectionTextAttributes(editor, user);

      RangeHighlighter rangeHighlighter =
          addRangeHighlighter(
              start, end, editor, selectionTextAttributes, SELECTION_HIGHLIGHTER_LAYER, file);

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

  // TODO check whether local representation already added; see #958
  @Override
  void addLocalRepresentation(@NotNull Editor editor) {
    User user = getUser();

    TextAttributes textAttributes = getSelectionTextAttributes(editor, user);

    addEditor(editor);

    IFile file = getFile();

    List<AnnotationRange> annotationRanges = getAnnotationRanges();

    int numberOfAnnotations = annotationRanges.size();

    if (numberOfAnnotations != 1) {
      throw new IllegalStateException(
          "Encountered selection annotation with "
              + numberOfAnnotations
              + " annotation ranges - "
              + this);
    }

    AnnotationRange annotationRange = annotationRanges.get(0);

    int start = annotationRange.getStart();
    int end = annotationRange.getEnd();

    RangeHighlighter rangeHighlighter =
        AbstractEditorAnnotation.addRangeHighlighter(
            start, end, editor, textAttributes, SELECTION_HIGHLIGHTER_LAYER, file);

    if (rangeHighlighter != null) {
      annotationRange.addRangeHighlighter(rangeHighlighter);

    } else {
      log.warn("Could not create range highlighter for range " + annotationRange + " for " + this);
    }

    addCaretRangeHighlighter(annotationRange, editor, user, file);
  }

  /**
   * Adds a range highlighter for the caret annotation to the given editor and stores it in {@link
   * #caretHighlighter}.
   *
   * <p>The highlighter is added at the start of the given annotation range if this is a backwards
   * selection and at the end of the annotation range otherwise.
   *
   * @param annotationRange the annotation range of this annotations
   * @param editor the editor of this annotation
   * @param user the user of this annotation
   * @param file the file of this annotation
   */
  private void addCaretRangeHighlighter(
      @NotNull AnnotationRange annotationRange,
      @NotNull Editor editor,
      @NotNull User user,
      @NotNull IFile file) {

    int caretPosition;

    if (isBackwardsSelection) {
      caretPosition = annotationRange.getStart();
    } else {
      caretPosition = annotationRange.getEnd();
    }

    TextAttributes caretTextAttributes = getCaretTextAttributes(editor, user);

    caretHighlighter =
        AbstractEditorAnnotation.addRangeHighlighter(
            caretPosition,
            caretPosition,
            editor,
            caretTextAttributes,
            CARET_HIGHLIGHTER_LAYER,
            file);

    if (caretHighlighter == null) {
      log.warn("Could not create caret highlighter for position " + caretPosition + " for " + this);
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Also removes the additional caret range highlighter from the editor and drops the local
   * reference.
   */
  @Override
  void removeLocalRepresentation() {
    Editor editor = getEditor();

    if (editor != null) {
      removeRangeHighlighter(editor, caretHighlighter);

      caretHighlighter = null;
    }

    super.removeLocalRepresentation();
  }

  /**
   * Returns the text attributes used for selection annotation range highlighters.
   *
   * @param editor the editor to which the annotation belongs
   * @param user the user to whom the annotation belongs
   * @return the text attributes used for selection annotation range highlighters
   */
  private static TextAttributes getSelectionTextAttributes(
      @NotNull Editor editor, @NotNull User user) {

    // Retrieve color keys based on the color ID selected by this user. This will automatically
    // fall back to default colors, if no colors for the given ID are available.
    ColorKeys colorKeys = ColorManager.getColorKeys(user.getColorID());

    TextAttributesKey highlightColorKey = colorKeys.getSelectionColorKey();

    // Resolve the correct text attributes based on the currently configured IDE scheme.
    return editor.getColorsScheme().getAttributes(highlightColorKey);
  }

  /**
   * Returns the text attributes for caret annotation range highlighters.
   *
   * <p>Caret annotations are mimicking a real caret in the local editor by adding a zero-width
   * range highlighter only consisting of a colored border.
   *
   * <p>The color of the caret range highlighter is determined by the caret color in the local
   * editor.
   *
   * @param editor the editor of the annotation
   * @param user the user of the annotation
   * @return the text attributes for caret annotation range highlighters
   */
  private static TextAttributes getCaretTextAttributes(@NotNull Editor editor, @NotNull User user) {
    ColorKeys colorKeys = ColorManager.getColorKeys(user.getColorID());
    TextAttributesKey highlightColorKey = colorKeys.getSelectionColorKey();
    TextAttributes defaultTextAttributes =
        editor.getColorsScheme().getAttributes(highlightColorKey);

    int font = defaultTextAttributes.getFontType();

    Color caretColor = editor.getColorsScheme().getColor(CARET_COLOR);

    return new TextAttributes(null, null, caretColor, EffectType.BOXED, font);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "[user="
        + getUser()
        + ", file="
        + getFile()
        + ", editor="
        + getEditor()
        + ", annotationRanges="
        + getAnnotationRanges()
        + ", caretHighlighter="
        + caretHighlighter
        + "]";
  }
}
