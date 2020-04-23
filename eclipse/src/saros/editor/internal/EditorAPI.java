package saros.editor.internal;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITextViewerExtension6;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.texteditor.DocumentProviderRegistry;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import saros.editor.text.LineRange;
import saros.editor.text.TextPosition;
import saros.editor.text.TextSelection;
import saros.filesystem.EclipseFileImpl;
import saros.filesystem.ResourceAdapterFactory;
import saros.ui.dialogs.WarningMessageDialog;
import saros.ui.util.SWTUtils;
import saros.ui.views.SarosView;
import saros.util.StackTrace;

/**
 * Collection of helper methods to interact with the Eclipse IDE and its editors.
 *
 * @swt Pretty much all methods in this class need to be called from SWT
 */
public class EditorAPI {

  private static final Logger log = Logger.getLogger(EditorAPI.class);

  private static final String[] RESTRICTED_ACTION = {
    ITextEditorActionConstants.CUT_LINE,
    ITextEditorActionConstants.CUT_LINE_TO_BEGINNING,
    ITextEditorActionConstants.CUT_LINE_TO_END,
    IWorkbenchActionConstants.CUT_EXT,
    ITextEditorActionConstants.DELETE_LINE,
    ITextEditorActionConstants.DELETE_LINE_TO_BEGINNING,
    ITextEditorActionConstants.DELETE_LINE_TO_END,
    ITextEditorActionConstants.CONTENT_ASSIST,
    ITextEditorActionConstants.MOVE_LINE_DOWN,
    ITextEditorActionConstants.MOVE_LINE_UP,
    ITextEditorActionConstants.SHIFT_LEFT,
    ITextEditorActionConstants.SHIFT_RIGHT,
    ITextEditorActionConstants.SHIFT_RIGHT_TAB
  };

  private static final VerifyKeyListener keyVerifier =
      new VerifyKeyListener() {
        @Override
        public void verifyKey(VerifyEvent event) {
          if (event.character > 0) {
            event.doit = false;

            Object adapter = getActiveEditor().getAdapter(IEditorStatusLine.class);
            if (adapter != null) {
              SarosView.showNotification(
                  "Read-Only Notification",
                  "You have only read access and therefore can't perform modifications.");

              Display display = SWTUtils.getDisplay();

              if (!display.isDisposed()) display.beep();
            }
          }
        }
      };

  private static boolean warnOnceExternalEditor = true;

  /**
   * Opens the editor with given path. Needs to be called from an UI thread.
   *
   * @param activate <code>true</code>, if editor should get focus, otherwise <code>false</code>
   * @return the opened editor or <code>null</code> if the editor couldn't be opened.
   */
  public static IEditorPart openEditor(saros.filesystem.IFile wrappedFile, boolean activate) {
    IFile file = ((EclipseFileImpl) wrappedFile).getDelegate();

    if (!file.exists()) {
      log.error("EditorAPI cannot open file which does not exist: " + file, new StackTrace());
      return null;
    }

    IWorkbenchWindow window = getActiveWindow();

    if (window == null) return null;

    try {
      IWorkbenchPage page = window.getActivePage();

      /*
       * TODO Use
       *
       * IWorkbenchPage.openEditor(IEditorInput input, String editorId,
       * boolean activate)
       *
       * to open an editor and set activate to false! So that we can
       * separate opening from activating, which save us duplicate sending
       * of activated events.
       */

      IEditorDescriptor descriptor = IDE.getEditorDescriptor(file);
      if (descriptor.isOpenExternal()) {
        /*
         * WORK-AROUND for #224: Editors are opened externally
         * erroneously (http://sourceforge.net/p/dpp/bugs/224)
         *
         * TODO Open as an internal editor
         */
        log.warn(
            "Editor for file "
                + file.getName()
                + " is configured to be opened externally,"
                + " which is not supported by Saros");

        if (warnOnceExternalEditor) {
          warnOnceExternalEditor = false;
          WarningMessageDialog.showWarningMessage(
              "Unsupported Editor Settings",
              "Eclipse is configured to open this file externally, "
                  + "which is not supported by Saros.\n\nPlease change the configuration"
                  + " (Right Click on File -> Open With...) so that the file is opened in Eclipse."
                  + "\n\nAll further "
                  + "warnings of this type will be shown in the error "
                  + "log.");
        }
        return null;
      }

      return IDE.openEditor(page, file, activate);
    } catch (PartInitException e) {
      log.error("could not initialize part: ", e);
    }

    return null;
  }

  /**
   * Opens the given editor part.
   *
   * <p>Needs to be called from an UI thread.
   *
   * @return <code>true</code> if the editor part was successfully opened, <code>false</code>
   *     otherwise
   */
  public static boolean openEditor(IEditorPart part) {
    IWorkbenchWindow window = getActiveWindow();

    if (window == null) return false;

    IWorkbenchPage page = window.getActivePage();
    try {
      page.openEditor(part.getEditorInput(), part.getEditorSite().getId());
      return true;
    } catch (PartInitException e) {
      log.error("failed to open editor part with title: " + part.getTitle(), e);
    }
    return false;
  }

  /**
   * Closes the given editor part. If <code>save=true</code> is specified, the content of the editor
   * will be written to disk before it is closed.
   *
   * <p>Needs to be called from an UI thread.
   *
   * @param part the editor part to close
   * @param save whether to write the editor content to disk before closing it
   * @see IWorkbenchPage#closeEditor(IEditorPart, boolean)
   */
  public static void closeEditor(IEditorPart part, boolean save) {
    IWorkbenchWindow window = getActiveWindow();

    if (window == null) return;

    IWorkbenchPage page = window.getActivePage();
    // Close AND let user decide if saving is necessary
    page.closeEditor(part, save);
  }

  /**
   * This method will return all editors open in all IWorkbenchWindows.
   *
   * <p>This method will ask Eclipse to restore editors which have not been loaded yet (if Eclipse
   * is started editors are loaded lazily), which is why it must run in the SWT thread. So calling
   * this method might cause partOpen events to be sent.
   *
   * @return all editors that are currently opened
   * @swt
   */
  public static Set<IEditorPart> getOpenEditors() {
    Set<IEditorPart> editorParts = new HashSet<IEditorPart>();

    for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
      for (IWorkbenchPage page : window.getPages()) {
        for (IEditorReference reference : page.getEditorReferences()) {
          IEditorPart editorPart = reference.getEditor(false);

          if (editorPart == null) {
            log.debug("editor part needs to be restored: " + reference.getTitle());
            // Making this call might cause partOpen events
            editorPart = reference.getEditor(true);
          }

          if (editorPart != null) {
            editorParts.add(editorPart);
          } else {
            log.warn("editor part could not be restored: " + reference);
          }
        }
      }
    }

    return editorParts;
  }

  /** @return the editor that is currently activated. */
  public static IEditorPart getActiveEditor() {
    final IWorkbenchWindow window = getActiveWindow();

    if (window == null) return null;

    final IWorkbenchPage page = window.getActivePage();

    return page != null ? page.getActiveEditor() : null;
  }

  /**
   * Returns the resource currently displayed in the given editorPart.
   *
   * @return Can be <code>null</code>, e.g. if the given editorPart is not operating on a resource,
   *     or has several resources.
   */
  public static IResource getEditorResource(IEditorPart editorPart) {
    IEditorInput input = editorPart.getEditorInput();
    IResource resource = ResourceUtil.getResource(input);

    if (resource == null) {
      log.warn("could not get resource reference from editor part: " + editorPart);
    }

    return resource;
  }

  // TODO move text position/offset calculation logic to a separate class; see PR #862
  /**
   * Returns the line base selection values for the given editor part.
   *
   * <p>The given editor part must not be <code>null</code>.
   *
   * @param editorPart the editorPart for which to get the text selection
   * @return the line base selection values for the given editor part or {@link
   *     TextSelection#EMPTY_SELECTION} if the given editor part is is <code>null</code> or not a
   *     text editor, the editor does not have a valid selection provider or document provider, a
   *     valid IDocument could not be obtained form the document provider, or the correct line
   *     numbers or in-lin offsets could not be calculated
   */
  public static TextSelection getSelection(IEditorPart editorPart) {
    if (!(editorPart instanceof ITextEditor)) {
      return TextSelection.EMPTY_SELECTION;
    }

    ITextEditor textEditor = (ITextEditor) editorPart;
    ISelectionProvider selectionProvider = textEditor.getSelectionProvider();

    if (selectionProvider == null) {
      return TextSelection.EMPTY_SELECTION;
    }

    IDocumentProvider documentProvider = textEditor.getDocumentProvider();

    if (documentProvider == null) {
      return TextSelection.EMPTY_SELECTION;
    }

    IDocument document = documentProvider.getDocument(editorPart.getEditorInput());

    if (document == null) {
      return TextSelection.EMPTY_SELECTION;
    }

    ITextSelection textSelection = (ITextSelection) selectionProvider.getSelection();
    int offset = textSelection.getOffset();
    int length = textSelection.getLength();

    return calculateSelection(document, offset, length);
  }

  /**
   * Calculates the text position for the given text based offset in the given document.
   *
   * <p>The given document must not be <code>null</code> and the given offset must not be negative.
   *
   * @param document the document in which to calculate the text position
   * @param offset the offset whose text position to calculate
   * @return the text position for the given text based offset in the given document or {@link
   *     TextPosition#INVALID_TEXT_POSITION} if the correct line number or in-line offset could not
   *     be calculated for the given offset
   * @throws NullPointerException if the given document is <code>null</code>
   * @throws IllegalArgumentException if the given offset is negative
   */
  public static TextPosition calculatePosition(IDocument document, int offset) {
    Objects.requireNonNull(document, "The given document must not be null");

    if (offset < 0) {
      throw new IllegalArgumentException("The given offset must not be negative");
    }

    return calculatePositionInternal(document, offset, null).getLeft();
  }

  /**
   * Calculates the text position for the given text based offset in the given document.
   *
   * <p>This method is used for performance optimization. It accepts a pair containing a line number
   * and the start offset of that line and also returns the calculated line number and start offset
   * of that line. This information can be used to skip calculations of line start offsets when
   * multiple text positions are located in the same line.
   *
   * <p>This method does not validate the input. The input is expected to be validated beforehand by
   * the caller.
   *
   * @param document the document in which to calculate the text position
   * @param offset the offset whose text position to calculate
   * @param knownLineStartOffset a pair containing a line number and the offset of the start of that
   *     line or <code>null</code> if no such pair is known
   * @return a pair containing the text position for the given text based offset in the given
   *     document and the line start offset or {@link TextPosition#INVALID_TEXT_POSITION} and <code>
   *     null</code> if the correct line number or in-line offset could not be calculated for the
   *     given offset
   * @throws NullPointerException if the given document is <code>null</code>
   */
  private static Pair<TextPosition, Pair<Integer, Integer>> calculatePositionInternal(
      IDocument document, int offset, Pair<Integer, Integer> knownLineStartOffset) {

    int lineNumber;
    int lineStartOffset;

    try {
      /*
       * Explicitly does not use textSelection.getStartLine() to obtain the start line number as
       * this call also uses IDocument.getLineOfOffset(...) but simply drops the thrown exception.
       */
      lineNumber = document.getLineOfOffset(offset);

      if (knownLineStartOffset != null && knownLineStartOffset.getLeft() == lineNumber) {
        lineStartOffset = knownLineStartOffset.getRight();

      } else {
        lineStartOffset = document.getLineOffset(lineNumber);
      }

    } catch (BadLocationException e) {
      log.warn(
          "Could not calculate the start line or start line offset for the given start offset "
              + offset
              + " in the document "
              + document,
          e);

      return new ImmutablePair<>(TextPosition.INVALID_TEXT_POSITION, null);
    }

    int inLineOffset = offset - lineStartOffset;

    TextPosition textPosition = new TextPosition(lineNumber, inLineOffset);

    return new ImmutablePair<>(textPosition, new ImmutablePair<>(lineNumber, lineStartOffset));
  }

  /**
   * Calculates the line based selection values for the given text based selection offset and length
   * in the given document.
   *
   * <p>An empty selection is represented by both the offset and length having the value -1. In any
   * other case, the offset and length must both be greater or equal to zero.
   *
   * <p>The given document must not be <code>null</code>.
   *
   * @param document the document the selection belongs to
   * @param offset the text based offset of the selection
   * @param length the text based length of the selection
   * @return the line based selection values for the given text offset based selection in the given
   *     document or {@link TextSelection#EMPTY_SELECTION} if the correct line numbers or in-line
   *     offsets could not be calculated or the given offset and length have the value -1
   * @throws NullPointerException if the given document is <code>null</code>
   * @throws IllegalArgumentException if one but not both of selection and length is set to -1 or
   *     any negative value besides -1 is passed
   * @see TextSelection
   * @see ITextSelection
   */
  public static TextSelection calculateSelection(IDocument document, int offset, int length) {
    Objects.requireNonNull(document, "The given document must not be null");

    if (offset == -1 && length == -1) {
      return TextSelection.EMPTY_SELECTION;

    } else if (offset < 0 || length < 0) {
      throw new IllegalArgumentException(
          "Invalid set of selection parameters given; must either both be -1 or both be >=0. o: "
              + offset
              + ", l: "
              + length);
    }

    int endOffset = offset + length;

    Pair<TextPosition, Pair<Integer, Integer>> startPositionResult =
        calculatePositionInternal(document, offset, null);

    TextPosition startPosition = startPositionResult.getLeft();

    if (!startPosition.isValid()) {
      return TextSelection.EMPTY_SELECTION;
    }

    // Skip second part of calculation if no selection is present
    if (endOffset == offset) {
      return new TextSelection(startPosition, startPosition);
    }

    Pair<Integer, Integer> knownLineStartOffset = startPositionResult.getRight();

    Pair<TextPosition, Pair<Integer, Integer>> endPositionResult =
        calculatePositionInternal(document, endOffset, knownLineStartOffset);
    TextPosition endPosition = endPositionResult.getLeft();

    return new TextSelection(startPosition, endPosition);
  }

  /**
   * Calculates the text based offset in the given editor part for the given text position.
   *
   * <p>The given editor part must not be <code>null</code> and the given text selection must not be
   * <code>null</code> and must not be empty.
   *
   * @param document the document in which to calculate the offset
   * @param textPosition the text position whose offset to calculate
   * @return the absolute offset in the given document for the given position or <code>-1</code> if
   *     the line start offset could not be calculated
   * @throws NullPointerException if the given document or text position is <code>null</code>
   * @throws IllegalArgumentException if the given text position is not valid
   */
  public static int calculateOffset(IDocument document, TextPosition textPosition) {
    Objects.requireNonNull(document, "The given document must not be null");
    Objects.requireNonNull(textPosition, "The given text position must not be null");

    if (!textPosition.isValid()) {
      throw new IllegalArgumentException("The given text position must not be invalid");
    }

    return calculateOffsetInternal(document, textPosition);
  }

  /**
   * Calculates the text based offset in the given editor part for the given text position.
   *
   * <p>This method does not validate the input. The input is expected to be validated beforehand by
   * the caller.
   *
   * @param document the document in which to calculate the offset
   * @param textPosition the text position whose offset to calculate
   * @return the absolute offset in the given document for the given position or <code>-1</code> if
   *     the line start offset could not be calculated
   * @throws NullPointerException if the given document or text position is <code>null</code>
   */
  private static int calculateOffsetInternal(IDocument document, TextPosition textPosition) {
    int lineOffset;

    try {
      lineOffset = document.getLineOffset(textPosition.getLineNumber());

    } catch (BadLocationException e) {
      log.warn(
          "Could not calculate the line offset for the given text position "
              + textPosition
              + " in the document "
              + document,
          e);

      return -1;
    }

    return lineOffset + textPosition.getInLineOffset();
  }

  /**
   * Calculates the text based offsets in the given editor part for the given text selection.
   *
   * <p>The given editor part must not be <code>null</code> and the given text selection must not be
   * <code>null</code> and must not be empty.
   *
   * @param editorPart the local editor part
   * @param selection the text selection
   * @return the absolute offsets in the given editor part for the given text selection or {@link
   *     org.eclipse.jface.text.TextSelection#emptySelection()} if the given editor is not a text
   *     editor, the editor does not have a valid document provider, a valid IDocument could not be
   *     obtained form the document provider, or the line offsets could not be calculated
   * @throws NullPointerException if the given editor part or selection is <code>null</code>
   * @throws IllegalArgumentException if the given text selection is empty
   */
  public static ITextSelection calculateOffsets(IEditorPart editorPart, TextSelection selection) {
    Objects.requireNonNull(editorPart, "The given editor part must not be null");
    Objects.requireNonNull(selection, "The given selection must not be null");

    if (selection.isEmpty()) {
      throw new IllegalArgumentException("The given text selection must not be empty");
    }

    if (!(editorPart instanceof ITextEditor)) {
      return org.eclipse.jface.text.TextSelection.emptySelection();
    }

    ITextEditor textEditor = (ITextEditor) editorPart;

    IDocumentProvider documentProvider = textEditor.getDocumentProvider();

    if (documentProvider == null) {
      return org.eclipse.jface.text.TextSelection.emptySelection();
    }

    IDocument document = documentProvider.getDocument(textEditor.getEditorInput());

    if (document == null) {
      return org.eclipse.jface.text.TextSelection.emptySelection();
    }

    TextPosition startPosition = selection.getStartPosition();

    int startOffset = calculateOffsetInternal(document, startPosition);

    if (startOffset == -1) {
      return org.eclipse.jface.text.TextSelection.emptySelection();
    }

    TextPosition endPosition = selection.getEndPosition();

    if (startPosition.equals(endPosition)) {
      return new org.eclipse.jface.text.TextSelection(startOffset, 0);
    }

    int endOffset;

    if (endPosition.getLineNumber() == startPosition.getLineNumber()) {
      int endLineOffset = startOffset - startPosition.getInLineOffset();

      endOffset = endLineOffset + endPosition.getInLineOffset();

    } else {
      endOffset = calculateOffsetInternal(document, endPosition);

      if (endOffset == -1) {
        return org.eclipse.jface.text.TextSelection.emptySelection();
      }
    }

    int length = endOffset - startOffset;

    return new org.eclipse.jface.text.TextSelection(startOffset, length);
  }

  /** Enables/disables the ability to edit the document in given editor. */
  public static void setEditable(final IEditorPart editorPart, final boolean isEditable) {

    ITextViewer textViewer = getViewer(editorPart);

    if (textViewer == null) return;

    if (isEditable) {
      updateStatusLine(editorPart, "");

      if (textViewer instanceof ITextViewerExtension)
        ((ITextViewerExtension) textViewer).removeVerifyKeyListener(keyVerifier);

      textViewer.setEditable(true);
      setEditorActionState(editorPart, true);

      // TODO use undoLevel from Preferences (TextEditorPlugin)
      if (textViewer instanceof ITextViewerExtension6)
        ((ITextViewerExtension6) textViewer).getUndoManager().setMaximalUndoLevel(200);

    } else {
      updateStatusLine(editorPart, "Not editable");

      if (textViewer instanceof ITextViewerExtension)
        ((ITextViewerExtension) textViewer).prependVerifyKeyListener(keyVerifier);

      textViewer.setEditable(false);
      setEditorActionState(editorPart, false);

      if (textViewer instanceof ITextViewerExtension6)
        ((ITextViewerExtension6) textViewer).getUndoManager().setMaximalUndoLevel(0);
    }
  }

  /** @return Return the viewport for viewer, <code>null</code> if no viewer is given */
  public static LineRange getViewport(ITextViewer viewer) {
    if (viewer == null) return null;

    int top = viewer.getTopIndex();
    // Have to add +1 because a LineRange should excludes the bottom line
    int bottom = viewer.getBottomIndex() + 1;

    if (bottom < top) {
      // FIXME This warning occurs when the document is shorter than the
      // viewport
      log.warn(
          "Viewport Range Problem in " + viewer + ": Bottom == " + bottom + " < Top == " + top);
      bottom = top;
    }

    return new LineRange(top, bottom - top);
  }

  public static void updateStatusLine(IEditorPart editorPart, String status) {
    Object adapter = editorPart.getAdapter(IEditorStatusLine.class);

    if (adapter == null) return;

    IEditorStatusLine statusLine = (IEditorStatusLine) adapter;
    statusLine.setMessage(false, status, null);
  }

  /**
   * Gets a {@link ITextViewer} instance for the given editor part.
   *
   * @param editorPart for which we want a {@link TextViewer}.
   * @return {@link ITextViewer} or <code>null</code> if there is no {@link TextViewer} for the
   *     editorPart.
   */
  public static ITextViewer getViewer(IEditorPart editorPart) {
    Object viewer = editorPart.getAdapter(ITextOperationTarget.class);

    return (viewer instanceof ITextViewer) ? (ITextViewer) viewer : null;
  }

  /**
   * Returns the active workbench window. Needs to be called from UI thread.
   *
   * @return the active workbench window or <code>null</code> if there is no window or method is
   *     called from non-UI thread or the activeWorkbenchWindow is disposed.
   * @see IWorkbench#getActiveWorkbenchWindow()
   */
  private static IWorkbenchWindow getActiveWindow() {
    try {
      return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    } catch (Exception e) {
      log.error("could not get active window", e);
      return null;
    }
  }

  /**
   * @return the path of the file the given editor is displaying or null if the given editor is not
   *     showing a file or the file is not referenced via a path in the project.
   */
  public static saros.filesystem.IFile getEditorPath(IEditorPart editorPart) {
    IFile resource = getEditorResource(editorPart).getAdapter(IFile.class);

    return (resource == null) ? null : ResourceAdapterFactory.create(resource);
  }

  /**
   * Returns the {@link IDocumentProvider} of the given {@link IEditorInput}. This method analyzes
   * the file extension of the {@link IFile} associated with the given {@link IEditorInput}.
   * Depending on the file extension it returns file-types responsible {@link IDocumentProvider}.
   *
   * @param input the {@link IEditorInput} for which {@link IDocumentProvider} is needed
   * @return IDocumentProvider of the given input
   */
  public static IDocumentProvider getDocumentProvider(final IEditorInput input) {
    return DocumentProviderRegistry.getDefault().getDocumentProvider(input);
  }

  /**
   * Can be called instead of {@link #getDocumentProvider(IEditorInput)} and {@link
   * IDocumentProvider#connect(Object) provider.connect()} in a try-catch block. This method logs an
   * error in case of an Exception and returns <code>null</code>. Otherwise, it returns the
   * connected provider.
   *
   * <p>Example usage:
   *
   * <pre>
   * <code>
   * IDocumentProvider provider = EditorAPI.connect(editorInput)
   *
   * if (provider == null) {
   *  doErrorHandling
   *  return;
   * }
   *
   * try {
   *  doLogic
   * }
   * finally {
   *  provider.disconnect(editorInput);
   * }
   * </code>
   * </pre>
   */
  public static IDocumentProvider connect(IEditorInput input) {
    IDocumentProvider provider = getDocumentProvider(input);
    try {
      provider.connect(input);
    } catch (CoreException e) {
      log.error("could not connect to document provider for file: " + input.getName(), e);
      return null;
    }

    return provider;
  }

  /**
   * Can be called instead of {@link #getDocumentProvider(IEditorInput)} and {@link
   * IDocumentProvider#disconnect(Object) provider.disconnect()}, in case there is no {@link
   * IDocumentProvider} instance at hand (e.g. if <code>connect()</code> and <code>disconnect()
   * </code> are called in different contexts).
   */
  public static void disconnect(IEditorInput input) {
    IDocumentProvider documentProvider = EditorAPI.getDocumentProvider(input);

    documentProvider.disconnect(input);
  }

  private static void setEditorActionState(final IEditorPart editorPart, final boolean enabled) {

    final ITextEditor editor = editorPart.getAdapter(ITextEditor.class);

    if (editor == null) return;

    for (final String actionID : RESTRICTED_ACTION) {
      final IAction action = editor.getAction(actionID);

      if (action != null) action.setEnabled(enabled);
    }
  }
}
