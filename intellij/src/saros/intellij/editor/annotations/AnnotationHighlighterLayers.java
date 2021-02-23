package saros.intellij.editor.annotations;

import com.intellij.openapi.editor.markup.HighlighterLayer;

/**
 * Constants defining the different highlighter layers used by Saros editor annotations.
 *
 * <p>Annotations on a layer with a higher number overshadow annotations on a layer with a lower
 * number.
 *
 * @see HighlighterLayer
 */
class AnnotationHighlighterLayers {

  private AnnotationHighlighterLayers() {}

  /**
   * Base highlighter layer used for Saros editor annotations. All Saros annotations must be
   * displayed on or below this highlighter layer.
   *
   * @see HighlighterLayer
   */
  private static final int SAROS_BASE_HIGHLIGHTER_LAYER = HighlighterLayer.CARET_ROW - 1;

  /**
   * Highlighter layer for the caret annotations displayed as part of the selection annotations.
   *
   * @see SelectionAnnotation
   */
  static final int CARET_HIGHLIGHTER_LAYER = SAROS_BASE_HIGHLIGHTER_LAYER - 1;

  /**
   * Highlighter layer for the selection annotations.
   *
   * @see SelectionAnnotation
   */
  static final int SELECTION_HIGHLIGHTER_LAYER = SAROS_BASE_HIGHLIGHTER_LAYER - 2;

  /**
   * Highlighter layer for the contribution annotations.
   *
   * @see ContributionAnnotation
   */
  static final int CONTRIBUTION_HIGHLIGHTER_LAYER = SAROS_BASE_HIGHLIGHTER_LAYER - 3;
}
