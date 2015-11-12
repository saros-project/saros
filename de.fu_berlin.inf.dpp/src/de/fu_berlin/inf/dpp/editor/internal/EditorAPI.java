package de.fu_berlin.inf.dpp.editor.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
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

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.text.LineRange;
import de.fu_berlin.inf.dpp.editor.text.TextSelection;
import de.fu_berlin.inf.dpp.filesystem.EclipseFileImpl;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.ui.dialogs.WarningMessageDialog;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.util.StackTrace;

/**
 * The central implementation of the IEditorAPI which basically encapsulates the
 * interaction with the TextEditor.
 * 
 * @swt Pretty much all methods in this class need to be called from SWT
 * 
 * @author rdjemili
 * 
 */
@Component(module = "core")
public class EditorAPI implements IEditorAPI {

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
        ITextEditorActionConstants.SHIFT_RIGHT_TAB };

    protected final VerifyKeyListener keyVerifier = new VerifyKeyListener() {
        @Override
        public void verifyKey(VerifyEvent event) {
            if (event.character > 0) {
                event.doit = false;

                Object adapter = getActiveEditor().getAdapter(
                    IEditorStatusLine.class);
                if (adapter != null) {
                    SarosView
                        .showNotification("Read-Only Notification",
                            "You have only read access and therefore can't perform modifications.");

                    Display display = SWTUtils.getDisplay();

                    if (!display.isDisposed())
                        display.beep();
                }
            }
        }
    };

    /**
     * Editors where the user isn't allowed to write
     */
    private final List<IEditorPart> lockedEditors = new ArrayList<IEditorPart>();

    private boolean warnOnceExternalEditor = true;

    @Override
    public IEditorPart openEditor(SPath path) {
        return openEditor(path, true);
    }

    @Override
    public IEditorPart openEditor(SPath path, boolean activate) {
        IFile file = ((EclipseFileImpl) path.getFile()).getDelegate();

        if (!file.exists()) {
            LOG.error("EditorAPI cannot open file which does not exist: "
                + file, new StackTrace());
            return null;
        }

        IWorkbenchWindow window = getActiveWindow();

        if (window == null)
            return null;

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
                LOG.warn("Editor for file " + file.getName()
                    + " is configured to be opened externally,"
                    + " which is not supported by Saros");

                if (warnOnceExternalEditor) {
                    warnOnceExternalEditor = false;
                    WarningMessageDialog
                        .showWarningMessage(
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

    @Override
    public boolean openEditor(IEditorPart part) {
        IWorkbenchWindow window = getActiveWindow();

        if (window == null)
            return false;

        IWorkbenchPage page = window.getActivePage();
        try {
            page.openEditor(part.getEditorInput(), part.getEditorSite().getId());
            return true;
        } catch (PartInitException e) {
            LOG.error(
                "failed to open editor part with title: " + part.getTitle(), e);
        }
        return false;
    }

    @Override
    public void closeEditor(IEditorPart part) {
        IWorkbenchWindow window = getActiveWindow();
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            page.closeEditor(part, true); // Close AND let user decide if saving
            // is necessary
        }
    }

    /**
     * This method will return all editors open in all IWorkbenchWindows.
     * 
     * This method will ask Eclipse to restore editors which have not been
     * loaded yet (if Eclipse is started editors are loaded lazily), which is
     * why it must run in the SWT thread. So calling this method might cause
     * partOpen events to be sent.
     * 
     * @return all editors that are currently opened
     * @swt
     */
    public static Set<IEditorPart> getOpenEditors() {
        return getOpenEditors(true);
    }

    /**
     * If <code>restore</code> is <code>true</code>, this method will ask
     * Eclipse to restore editors which have not been loaded yet, and must be
     * run in the SWT thread. If false, only editors which are already loaded
     * are returned.
     * 
     * @param restore
     * @return
     * @see EditorAPI#getOpenEditors()
     */
    private static Set<IEditorPart> getOpenEditors(boolean restore) {
        Set<IEditorPart> editorParts = new HashSet<IEditorPart>();

        IWorkbenchWindow[] windows = getWindows();

        for (IWorkbenchWindow window : windows) {
            IWorkbenchPage[] pages = window.getPages();

            for (IWorkbenchPage page : pages) {
                IEditorReference[] editorRefs = page.getEditorReferences();

                for (IEditorReference reference : editorRefs) {

                    IEditorPart editorPart = reference.getEditor(false);

                    /*
                     * FIXME calling this with restore = false will always
                     * return an empty set
                     */
                    if (!restore)
                        continue;

                    if (editorPart == null) {
                        LOG.debug("editor part needs to be restored: "
                            + reference.getTitle());
                        // Making this call might cause partOpen events
                        editorPart = reference.getEditor(true);
                    }

                    if (editorPart != null) {
                        editorParts.add(editorPart);
                    } else {
                        LOG.warn("editor part could not be restored: "
                            + reference);
                    }
                }
            }
        }

        return editorParts;
    }

    @Override
    public IEditorPart getActiveEditor() {
        final IWorkbenchWindow window = getActiveWindow();

        if (window == null)
            return null;

        final IWorkbenchPage page = window.getActivePage();

        return page != null ? page.getActiveEditor() : null;
    }

    @Override
    public IResource getEditorResource(IEditorPart editorPart) {

        IEditorInput input = editorPart.getEditorInput();

        IResource resource = ResourceUtil.getResource(input);

        if (resource == null) {
            LOG.warn("could not get resource reference from editor part: "
                + editorPart);
        }

        return resource;
    }

    @Override
    public TextSelection getSelection(IEditorPart editorPart) {

        if (!(editorPart instanceof ITextEditor)) {
            return TextSelection.emptySelection();
        }

        ITextEditor textEditor = (ITextEditor) editorPart;
        ISelectionProvider selectionProvider = textEditor
            .getSelectionProvider();

        if (selectionProvider == null) {
            return TextSelection.emptySelection();
        }

        ITextSelection jfaceSelection = (ITextSelection) selectionProvider
            .getSelection();
        return new TextSelection(jfaceSelection.getOffset(),
            jfaceSelection.getLength());
    }

    @Override
    public void setEditable(final IEditorPart editorPart,
        final boolean newIsEditable) {

        ITextViewer textViewer = EditorAPI.getViewer(editorPart);

        if (textViewer == null)
            return;

        boolean isEditable = !lockedEditors.contains(editorPart);

        // Already as we want it?
        if (newIsEditable == isEditable)
            return;

        LOG.trace(editorPart.getEditorInput().getName() + " set to editable: "
            + newIsEditable);

        updateStatusLine(editorPart, newIsEditable);

        if (newIsEditable) {
            lockedEditors.remove(editorPart);

            if (textViewer instanceof ITextViewerExtension)
                ((ITextViewerExtension) textViewer)
                    .removeVerifyKeyListener(EditorAPI.this.keyVerifier);

            textViewer.setEditable(true);
            setEditorActionState(editorPart, true);

            // TODO use undoLevel from Preferences (TextEditorPlugin)
            if (textViewer instanceof ITextViewerExtension6)
                ((ITextViewerExtension6) textViewer).getUndoManager()
                    .setMaximalUndoLevel(200);

        } else {
            lockedEditors.add(editorPart);

            if (textViewer instanceof ITextViewerExtension)
                ((ITextViewerExtension) textViewer)
                    .prependVerifyKeyListener(EditorAPI.this.keyVerifier);

            textViewer.setEditable(false);
            setEditorActionState(editorPart, false);

            if (textViewer instanceof ITextViewerExtension6)
                ((ITextViewerExtension6) textViewer).getUndoManager()
                    .setMaximalUndoLevel(0);

        }
    }

    public static LineRange getViewport(ITextViewer viewer) {

        int top = viewer.getTopIndex();
        // Have to add +1 because a LineRange should excludes the bottom line
        int bottom = viewer.getBottomIndex() + 1;

        if (bottom < top) {
            // FIXME This warning occurs when the document is shorter than the
            // viewport
            LOG.warn("Viewport Range Problem in " + viewer + ": Bottom == "
                + bottom + " < Top == " + top);
            bottom = top;
        }

        return new LineRange(top, bottom - top);
    }

    @Override
    public LineRange getViewport(IEditorPart editorPart) {

        ITextViewer textViewer = EditorAPI.getViewer(editorPart);
        if (textViewer == null)
            return null;

        return getViewport(textViewer);
    }

    private void updateStatusLine(IEditorPart editorPart, boolean editable) {
        Object adapter = editorPart.getAdapter(IEditorStatusLine.class);
        if (adapter != null) {
            IEditorStatusLine statusLine = (IEditorStatusLine) adapter;
            statusLine.setMessage(false, editable ? "" : "Not editable", null);
        }
    }

    /**
     * Gets a {@link ITextViewer} instance for the given editor part.
     * 
     * @param editorPart
     *            for which we want a {@link TextViewer}.
     * @return {@link ITextViewer} or <code>null</code> if there is no
     *         {@link TextViewer} for the editorPart.
     */
    public static ITextViewer getViewer(IEditorPart editorPart) {

        Object viewer = editorPart.getAdapter(ITextOperationTarget.class);
        if (viewer instanceof ITextViewer) {
            return (ITextViewer) viewer;
        } else {
            return null;
        }
    }

    /**
     * Returns the active workbench window. Needs to be called from UI thread.
     * 
     * @return the active workbench window or <code>null</code> if there is no
     *         window or method is called from non-UI thread or the
     *         activeWorkbenchWindow is disposed.
     * @see IWorkbench#getActiveWorkbenchWindow()
     */
    private static IWorkbenchWindow getActiveWindow() {
        try {
            return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        } catch (Exception e) {
            return null;
        }
    }

    private static IWorkbenchWindow[] getWindows() {
        return PlatformUI.getWorkbench().getWorkbenchWindows();
    }

    /**
     * Saves the given project and returns true if the operation was successful
     * or false if the user canceled.
     * 
     * TODO Tell the user why we do want to save!
     * 
     * @param confirm
     *            true to ask the user before saving unsaved changes, and false
     *            to save unsaved changes without asking
     */
    public static boolean saveProject(final IProject projectToSave,
        final boolean confirm) {
        try {
            Boolean result = true;
            result = SWTUtils.runSWTSync(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    /**
                     * TODO saveAllEditors does not save the Documents that we
                     * are modifying in the background
                     */
                    return IDE.saveAllEditors(
                        new IResource[] { projectToSave }, confirm);
                }
            });
            return result;
        } catch (Exception e) {
            // The operation does not throw an exception, thus this is an error.
            throw new RuntimeException(e);
        }
    }

    @Override
    public SPath getActiveEditorPath() {
        IEditorPart newActiveEditor = getActiveEditor();
        if (newActiveEditor == null)
            return null;

        return getEditorPath(newActiveEditor);
    }

    @Override
    public SPath getEditorPath(IEditorPart editorPart) {
        IResource resource = getEditorResource(editorPart);
        if (resource == null) {
            return null;
        }

        IPath path = resource.getProjectRelativePath();

        if (path == null) {
            LOG.warn("could not get path from resource " + resource);
        }

        return new SPath(ResourceAdapterFactory.create(resource));
    }

    @Override
    public IDocumentProvider getDocumentProvider(final IEditorInput input) {
        return DocumentProviderRegistry.getDefault().getDocumentProvider(input);
    }

    @Override
    public IDocument getDocument(final IEditorPart editorPart) {
        final IEditorInput input = editorPart.getEditorInput();

        return getDocumentProvider(input).getDocument(input);
    }

    private void setEditorActionState(final IEditorPart editorPart,
        final boolean enabled) {

        final ITextEditor editor = (ITextEditor) editorPart
            .getAdapter(ITextEditor.class);

        if (editor == null)
            return;

        for (final String actionID : RESTRICTED_ACTION) {
            final IAction action = editor.getAction(actionID);

            if (action != null)
                action.setEnabled(enabled);
        }
    }
}
