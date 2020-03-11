package saros.editor.internal;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.Test;
import saros.editor.text.TextPosition;
import saros.editor.text.TextSelection;

/** Tests the position calculation logic of the editor api. */
public class EditorAPITextPositionTest {

  /**
   * Build an IDocument mock that can be used to call {@link EditorAPI#calculateSelection(IDocument,
   * int, int)}.
   *
   * <p>The needed line offset lookup answers must be set using {@link
   * #withLineOffsetLookupAnswer(int, int)} before calling {@link #build()}.
   *
   * <p>The needed line number lookup answers must be set using {@link
   * #withLineNumberLookupAnswer(int, int)} before calling {@link #build()}.
   */
  private static class IDocumentBuilder {
    private IDocument document;

    private IDocumentBuilder() {
      document = EasyMock.createNiceMock(IDocument.class);
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
    private IDocumentBuilder withLineOffsetLookupAnswer(int lineNumberInput, int lineOffsetAnswer) {
      try {
        EasyMock.expect(document.getLineOffset(lineNumberInput)).andReturn(lineOffsetAnswer);

      } catch (BadLocationException ignored) {
        // ignored
      }

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
    private IDocumentBuilder withLineNumberLookupAnswer(int offsetInput, int lineNumberAnswer) {
      try {
        EasyMock.expect(document.getLineOfOffset(offsetInput)).andReturn(lineNumberAnswer);

      } catch (BadLocationException ignored) {
        // ignored
      }

      return this;
    }

    /**
     * Builds the IDocument mock.
     *
     * @return the IDocument mock
     * @see #withLineOffsetLookupAnswer(int, int)
     * @see #withLineNumberLookupAnswer(int, int)
     */
    private IDocument build() {
      EasyMock.replay(document);
      return document;
    }
  }

  /**
   * Build an ITextEditor mock that can be used to call {@link
   * EditorAPI#calculateOffsets(IEditorPart, TextSelection)}.
   *
   * <p>The needed line offset lookup answers must be set using {@link
   * #withLineOffsetLookupAnswer(int, int)} before calling {@link #build()}.
   */
  private static class ITextEditorBuilder {
    ITextEditor editor;

    IDocumentBuilder documentBuilder = new IDocumentBuilder();

    private ITextEditorBuilder() {
      editor = EasyMock.createNiceMock(ITextEditor.class);
    }

    /**
     * Sets the passed values for the line offset lookup.
     *
     * <p>This must be called for all needed lookup values before calling {@link #build()}.
     *
     * @param lineNumberInput the line number whose offset is looked up
     * @param lineOffsetAnswer the offset to return as the answer for the lookup
     * @return this builder
     * @see IDocumentBuilder#withLineOffsetLookupAnswer(int, int)
     */
    private ITextEditorBuilder withLineOffsetLookupAnswer(
        int lineNumberInput, int lineOffsetAnswer) {
      documentBuilder.withLineOffsetLookupAnswer(lineNumberInput, lineOffsetAnswer);

      return this;
    }

    /**
     * Builds the ITextEditor mock.
     *
     * @return the ITextEditor mock
     * @see #withLineOffsetLookupAnswer(int, int)
     */
    private ITextEditor build() {
      IDocument document = documentBuilder.build();

      IEditorInput editorInput = EasyMock.createNiceMock(IEditorInput.class);
      EasyMock.replay(editorInput);

      IDocumentProvider documentProvider = EasyMock.createNiceMock(IDocumentProvider.class);
      EasyMock.expect(documentProvider.getDocument(editorInput)).andReturn(document).anyTimes();
      EasyMock.replay(documentProvider);

      editor = EasyMock.createNiceMock(ITextEditor.class);
      EasyMock.expect(editor.getDocumentProvider()).andReturn(documentProvider).anyTimes();
      EasyMock.expect(editor.getEditorInput()).andReturn(editorInput).anyTimes();
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
   * Ease of use method to instantiate an <code>ITextSelection</code> object holding the given start
   * offset and the length calculated using the given endOffset.
   *
   * @param startOffset the start offset
   * @param endOffset the end offset
   * @return an <code>ITextSelection</code> object holding the given start offset and the length
   *     calculated using the given endOffset
   */
  private ITextSelection offsets(int startOffset, int endOffset) {
    int length = endOffset - startOffset;

    return new org.eclipse.jface.text.TextSelection(startOffset, length);
  }

  @Test
  public void testCalculateOffsetsStartSelection() {
    int startLine = 0;
    int endLine = 1;
    TextSelection textSelection = selection(startLine, 0, endLine, 1);

    ITextEditor editor =
        new ITextEditorBuilder()
            .withLineOffsetLookupAnswer(startLine, 0)
            .withLineOffsetLookupAnswer(endLine, 10)
            .build();

    ITextSelection expectedOffsets = offsets(0, 11);

    ITextSelection calculatedOffsets = EditorAPI.calculateOffsets(editor, textSelection);

    assertEquals(expectedOffsets, calculatedOffsets);
  }

  @Test
  public void testCalculateOffsetsMultiLineSelection() {
    int startLine = 5;
    int endLine = 9;
    TextSelection textSelection = selection(startLine, 7, endLine, 3);

    ITextEditor editor =
        new ITextEditorBuilder()
            .withLineOffsetLookupAnswer(startLine, 250)
            .withLineOffsetLookupAnswer(endLine, 315)
            .build();

    ITextSelection expectedOffsets = offsets(257, 318);

    ITextSelection calculatedOffsets = EditorAPI.calculateOffsets(editor, textSelection);

    assertEquals(expectedOffsets, calculatedOffsets);
  }

  @Test
  public void testCalculateOffsetsOneLineSelection() {
    int startLine = 3;
    int endLine = 3;
    TextSelection textSelection = selection(startLine, 7, endLine, 9);

    ITextEditor editor = new ITextEditorBuilder().withLineOffsetLookupAnswer(startLine, 11).build();

    ITextSelection expectedOffsets = offsets(18, 20);

    ITextSelection calculatedOffsets = EditorAPI.calculateOffsets(editor, textSelection);

    assertEquals(expectedOffsets, calculatedOffsets);
  }

  @Test
  public void testCalculateOffsetsEmptySelection() {
    int startLine = 7;
    int endLine = 7;
    TextSelection textSelection = selection(startLine, 5, endLine, 5);

    ITextEditor editor = new ITextEditorBuilder().withLineOffsetLookupAnswer(startLine, 23).build();

    ITextSelection expectedOffsets = offsets(28, 28);

    ITextSelection calculatedOffsets = EditorAPI.calculateOffsets(editor, textSelection);

    assertEquals(expectedOffsets, calculatedOffsets);
  }

  @Test
  public void testCalculateSelectionStartSelection() {
    int offset = 0;
    int endOffset = 15;
    int length = endOffset - offset;
    int startLine = 0;
    int endLine = 1;

    IDocument document =
        new IDocumentBuilder()
            .withLineNumberLookupAnswer(offset, startLine)
            .withLineNumberLookupAnswer(endOffset, endLine)
            .withLineOffsetLookupAnswer(startLine, 0)
            .withLineOffsetLookupAnswer(endLine, 8)
            .build();

    TextSelection expectedSelection = selection(startLine, 0, endLine, 7);

    TextSelection calculatedSelection = EditorAPI.calculateSelection(document, offset, length);

    assertEquals(expectedSelection, calculatedSelection);
  }

  @Test
  public void testCalculateSelectionMultipleLineSelection() {
    int offset = 75;
    int endOffset = 351;
    int length = endOffset - offset;
    int startLine = 9;
    int endLine = 28;

    IDocument document =
        new IDocumentBuilder()
            .withLineNumberLookupAnswer(offset, startLine)
            .withLineNumberLookupAnswer(endOffset, endLine)
            .withLineOffsetLookupAnswer(startLine, 68)
            .withLineOffsetLookupAnswer(endLine, 290)
            .build();

    TextSelection expectedSelection = selection(startLine, 7, endLine, 61);

    TextSelection calculatedSelection = EditorAPI.calculateSelection(document, offset, length);

    assertEquals(expectedSelection, calculatedSelection);
  }

  @Test
  public void testCalculateSelectionOneLineSelection() {
    int offset = 85;
    int endOffset = 110;
    int length = endOffset - offset;
    int startLine = 10;
    int endLine = 10;

    IDocument document =
        new IDocumentBuilder()
            .withLineNumberLookupAnswer(offset, startLine)
            .withLineNumberLookupAnswer(endOffset, endLine)
            .withLineOffsetLookupAnswer(startLine, 79)
            .build();

    TextSelection expectedSelection = selection(startLine, 6, endLine, 31);

    TextSelection calculatedSelection = EditorAPI.calculateSelection(document, offset, length);

    assertEquals(expectedSelection, calculatedSelection);
  }

  @Test
  public void testCalculateSelectionEmptySelection() {
    int offset = 243;
    int endOffset = 243;
    int length = endOffset - offset;
    int startLine = 98;
    int endLine = 98;

    IDocument document =
        new IDocumentBuilder()
            .withLineNumberLookupAnswer(offset, startLine)
            .withLineOffsetLookupAnswer(startLine, 100)
            .build();

    TextSelection expectedSelection = selection(startLine, 143, endLine, 143);

    TextSelection calculatedSelection = EditorAPI.calculateSelection(document, offset, length);

    assertEquals(expectedSelection, calculatedSelection);
  }
}
