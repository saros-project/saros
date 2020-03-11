package saros.intellij.editor;

import static org.junit.Assert.assertEquals;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.util.Pair;
import org.easymock.EasyMock;
import org.junit.Test;
import saros.editor.text.TextPosition;
import saros.editor.text.TextSelection;

/** Tests the position calculation logic of the editor api. */
public class EditorAPITextPositionTest {

  /**
   * Build an Editor mock that can be used to call {@link EditorAPI#calculateOffsets(Editor,
   * TextSelection)} and {@link EditorAPI#calculateSelectionPosition(Editor, int, int)}.
   *
   * <p>The needed line offset lookup answers must be set using {@link
   * #withLineOffsetLookupAnswer(int, int)} before calling {@link #build()}.
   *
   * <p>The needed line number lookup answers must be set using {@link
   * #withLineNumberLookupAnswer(int, int)} before calling {@link #build()}.
   */
  private static class EditorBuild {
    private Editor editor;

    private EditorBuild() {
      editor = EasyMock.createNiceMock(Editor.class);
    }

    /**
     * Sets the passed values for the line offset lookup.
     *
     * <p>This must be called for all needed lookup values before calling {@link #build()}.
     *
     * @param lineNumberInput the line number whose offset is looked up
     * @param lineOffsetAnswer the offset to return as the answer for the lookup
     * @return this builder
     */
    private EditorBuild withLineOffsetLookupAnswer(int lineNumberInput, int lineOffsetAnswer) {
      EasyMock.expect(editor.logicalPositionToOffset(new LogicalPosition(lineNumberInput, 0)))
          .andReturn(lineOffsetAnswer);

      return this;
    }

    /**
     * Sets the passed values for the line number lookup.
     *
     * <p>This must be called for all needed lookup values before calling {@link #build()}.
     *
     * @param offsetInput the offset whose line number to look up
     * @param lineNumberAnswer the line number to return as the answer to the lookup
     * @return this builder
     */
    private EditorBuild withLineNumberLookupAnswer(int offsetInput, int lineNumberAnswer) {
      EasyMock.expect(editor.offsetToLogicalPosition(offsetInput))
          .andReturn(new LogicalPosition(lineNumberAnswer, 0));

      return this;
    }

    /**
     * Builds the Editor mock.
     *
     * @return the Editor mock
     * @see #withLineOffsetLookupAnswer(int, int)
     * @see #withLineNumberLookupAnswer(int, int)
     */
    private Editor build() {
      EasyMock.replay(editor);

      return editor;
    }
  }

  /**
   * Ease of use method to instantiate a text selection with the given parameters.
   *
   * @param startLine the start line
   * @param startInLineOffset the start in-line offset
   * @param endLine the end line
   * @param endInLineOffset the end in-line offset
   * @return a text selection with the given parameters
   * @see TextPosition#TextPosition(int, int)
   * @see TextSelection#TextSelection(TextPosition, TextPosition)
   */
  private TextSelection selection(
      int startLine, int startInLineOffset, int endLine, int endInLineOffset) {

    return new TextSelection(
        new TextPosition(startLine, startInLineOffset), new TextPosition(endLine, endInLineOffset));
  }

  /**
   * Ease of use method to instantiate a <code>Pair</code> object holding the given start and end
   * offset.
   *
   * @param startOffset the start offset
   * @param endOffset the end offset
   * @return a <code>Pair</code> object holding the given start and end offset
   */
  private Pair<Integer, Integer> offsets(int startOffset, int endOffset) {
    return new Pair<>(startOffset, endOffset);
  }

  @Test
  public void testCalculateOffsetsStartSelection() {
    int startLine = 0;
    int endLine = 1;
    TextSelection textSelection = selection(startLine, 0, endLine, 1);

    Editor editor =
        new EditorBuild()
            .withLineOffsetLookupAnswer(startLine, 0)
            .withLineOffsetLookupAnswer(endLine, 10)
            .build();

    Pair<Integer, Integer> expectedOffsets = offsets(0, 11);

    Pair<Integer, Integer> calculatedOffsets = EditorAPI.calculateOffsets(editor, textSelection);

    assertEquals(expectedOffsets, calculatedOffsets);
  }

  @Test
  public void testCalculateOffsetsMultiLineSelection() {
    int startLine = 5;
    int endLine = 9;
    TextSelection textSelection = selection(startLine, 7, endLine, 3);

    Editor editor =
        new EditorBuild()
            .withLineOffsetLookupAnswer(startLine, 250)
            .withLineOffsetLookupAnswer(endLine, 315)
            .build();

    Pair<Integer, Integer> expectedOffsets = offsets(257, 318);

    Pair<Integer, Integer> calculatedOffsets = EditorAPI.calculateOffsets(editor, textSelection);

    assertEquals(expectedOffsets, calculatedOffsets);
  }

  @Test
  public void testCalculateOffsetsOneLineSelection() {
    int startLine = 3;
    int endLine = 3;
    TextSelection textSelection = selection(startLine, 7, endLine, 9);

    Editor editor = new EditorBuild().withLineOffsetLookupAnswer(startLine, 11).build();

    Pair<Integer, Integer> expectedOffsets = offsets(18, 20);

    Pair<Integer, Integer> calculatedOffsets = EditorAPI.calculateOffsets(editor, textSelection);

    assertEquals(expectedOffsets, calculatedOffsets);
  }

  @Test
  public void testCalculateOffsetsEmptySelection() {
    int startLine = 7;
    int endLine = 7;
    TextSelection textSelection = selection(startLine, 5, endLine, 5);

    Editor editor = new EditorBuild().withLineOffsetLookupAnswer(startLine, 23).build();

    Pair<Integer, Integer> expectedOffsets = offsets(28, 28);

    Pair<Integer, Integer> calculatedOffsets = EditorAPI.calculateOffsets(editor, textSelection);

    assertEquals(expectedOffsets, calculatedOffsets);
  }

  @Test
  public void testCalculateSelectionStartSelection() {
    int offset = 0;
    int endOffset = 15;
    int startLine = 0;
    int endLine = 1;

    Editor editor =
        new EditorBuild()
            .withLineNumberLookupAnswer(offset, startLine)
            .withLineNumberLookupAnswer(endOffset, endLine)
            .withLineOffsetLookupAnswer(startLine, 0)
            .withLineOffsetLookupAnswer(endLine, 8)
            .build();

    TextSelection expectedSelection = selection(startLine, 0, endLine, 7);

    TextSelection calculatedSelection =
        EditorAPI.calculateSelectionPosition(editor, offset, endOffset);

    assertEquals(expectedSelection, calculatedSelection);
  }

  @Test
  public void testCalculateSelectionMultipleLineSelection() {
    int offset = 75;
    int endOffset = 351;
    int startLine = 9;
    int endLine = 28;

    Editor editor =
        new EditorBuild()
            .withLineNumberLookupAnswer(offset, startLine)
            .withLineNumberLookupAnswer(endOffset, endLine)
            .withLineOffsetLookupAnswer(startLine, 68)
            .withLineOffsetLookupAnswer(endLine, 290)
            .build();

    TextSelection expectedSelection = selection(startLine, 7, endLine, 61);

    TextSelection calculatedSelection =
        EditorAPI.calculateSelectionPosition(editor, offset, endOffset);

    assertEquals(expectedSelection, calculatedSelection);
  }

  @Test
  public void testCalculateSelectionOneLineSelection() {
    int offset = 85;
    int endOffset = 110;
    int startLine = 10;
    int endLine = 10;

    Editor editor =
        new EditorBuild()
            .withLineNumberLookupAnswer(offset, startLine)
            .withLineNumberLookupAnswer(endOffset, endLine)
            .withLineOffsetLookupAnswer(startLine, 79)
            .build();

    TextSelection expectedSelection = selection(startLine, 6, endLine, 31);

    TextSelection calculatedSelection =
        EditorAPI.calculateSelectionPosition(editor, offset, endOffset);

    assertEquals(expectedSelection, calculatedSelection);
  }

  @Test
  public void testCalculateSelectionEmptySelection() {
    int offset = 243;
    int endOffset = 243;
    int startLine = 98;
    int endLine = 98;

    Editor editor =
        new EditorBuild()
            .withLineNumberLookupAnswer(offset, startLine)
            .withLineOffsetLookupAnswer(startLine, 100)
            .build();

    TextSelection expectedSelection = selection(startLine, 143, endLine, 143);

    TextSelection calculatedSelection =
        EditorAPI.calculateSelectionPosition(editor, offset, endOffset);

    assertEquals(expectedSelection, calculatedSelection);
  }
}
