package saros.editor.internal;

import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
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
import saros.activities.SPath;
import saros.editor.text.LineRange;
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

  private static final Logger LOG = Logger.getLogger(EditorAPI.class);

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
  public static IEditorPart openEditor(SPath path, boolean activate) {
    IFile file = ((EclipseFileImpl) path.getFile()).getDelegate();

    if (!file.exists()) {
      LOG.error("EditorAPI cannot open file which does not exist: " + file, new StackTrace());
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
        LOG.warn(
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
      LOG.error("could not initialize part: ", e);
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
      LOG.error("failed to open editor part with title: " + part.getTitle(), e);
    }
    return false;
  }

  /**
   * Closes the given editor part.
   *
   * <p>Needs to be called from an UI thread.
   */
  public static void closeEditor(IEditorPart part) {
    IWorkbenchWindow window = getActiveWindow();

    if (window == null) return;

    IWorkbenchPage page = window.getActivePage();
    // Close AND let user decide if saving is necessary
    page.closeEditor(part, true);
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
            LOG.debug("editor part needs to be restored: " + reference.getTitle());
            // Making this call might cause partOpen events
            editorPart = reference.getEditor(true);
          }

          if (editorPart != null) {
            editorParts.add(editorPart);
          } else {
            LOG.warn("editor part could not be restored: " + reference);
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
      LOG.warn("could not get resource reference from editor part: " + editorPart);
    }

    return resource;
  }

  /**
   * Returns the current text selection for given editor.
   *
   * @param editorPart the editorPart for which to get the text selection.
   * @return the current text selection. Returns {@link TextSelection#emptySelection()} if no text
   *     selection exists.
   */
  public static TextSelection getSelection(IEditorPart editorPart) {
    if (!(editorPart instanceof ITextEditor)) {
      return TextSelection.emptySelection();
    }

    ITextEditor textEditor = (ITextEditor) editorPart;
    ISelectionProvider selectionProvider = textEditor.getSelectionProvider();

    if (selectionProvider == null) {
      return TextSelection.emptySelection();
    }

    ITextSelection jfaceSelection = (ITextSelection) selectionProvider.getSelection();

    return new TextSelection(jfaceSelection.getOffset(), jfaceSelection.getLength());
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
      LOG.warn(
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
      LOG.error("could not get active window", e);
      return null;
    }
  }

  /**
   * @return the path of the file the given editor is displaying or null if the given editor is not
   *     showing a file or the file is not referenced via a path in the project.
   */
  public static SPath getEditorPath(IEditorPart editorPart) {
    IResource resource = getEditorResource(editorPart);

    return (resource == null) ? null : new SPath(ResourceAdapterFactory.create(resource));
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
      LOG.error("could not connect to document provider for file: " + input.getName(), e);
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

    final ITextEditor editor = (ITextEditor) editorPart.getAdapter(ITextEditor.class);

    if (editor == null) return;

    for (final String actionID : RESTRICTED_ACTION) {
      final IAction action = editor.getAction(actionID);

      if (action != null) action.setEnabled(enabled);
    }
  }
}
