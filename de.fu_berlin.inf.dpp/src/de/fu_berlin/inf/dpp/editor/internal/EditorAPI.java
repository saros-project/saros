package de.fu_berlin.inf.dpp.editor.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.ITextViewerExtension6;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.ui.dialogs.WarningMessageDialog;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.util.BlockingProgressMonitor;
import de.fu_berlin.inf.dpp.util.Pair;
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

    private static final Logger log = Logger.getLogger(EditorAPI.class);

    public EditorAPI() {

    }

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
    protected List<IEditorPart> lockedEditors = new ArrayList<IEditorPart>();

    /**
     * Currently managed shared project part listeners for removal by
     * removePartListener
     */
    protected Map<EditorManager, IPartListener2> partListeners = new HashMap<EditorManager, IPartListener2>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEditorPartListener(EditorManager editorManager) {
        assert SWTUtils.isSWT();

        if (editorManager == null)
            throw new IllegalArgumentException();

        if (partListeners.containsKey(editorManager)) {
            log.error("EditorPartListener was added twice: ", new StackTrace());
            removeEditorPartListener(editorManager);
        }

        IPartListener2 partListener = new SafePartListener2(log,
            new EditorPartListener(editorManager));

        partListeners.put(editorManager, partListener);

        // TODO This can fail if a shared project is started when no
        // Eclipse Window is open!
        EditorAPI.getActiveWindow().getPartService()
            .addPartListener(partListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeEditorPartListener(EditorManager editorManager) {

        if (editorManager == null)
            throw new IllegalArgumentException();

        if (!partListeners.containsKey(editorManager)) {
            throw new IllegalStateException();
        }

        // TODO This can fail if a shared project is started when no
        // Eclipse Window is open!
        IWorkbenchWindow window = EditorAPI.getActiveWindow();
        if (window == null)
            return;

        window.getPartService().removePartListener(
            partListeners.remove(editorManager));
    }

    private boolean warnOnceExternalEditor = true;

    /**
     * {@inheritDoc}
     */
    @Override
    public IEditorPart openEditor(SPath path) {
        return openEditor(path, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IEditorPart openEditor(SPath path, boolean activate) {
        IFile file = path.getFile();

        if (!file.exists()) {
            log.error("EditorAPI cannot open file which does not exist: "
                + file, new StackTrace());
            return null;
        }

        IWorkbenchWindow window = EditorAPI.getActiveWindow();
        if (window != null) {
            try {
                IWorkbenchPage page = window.getActivePage();

                /*
                 * TODO Use
                 * 
                 * IWorkbenchPage.openEditor(IEditorInput input, String
                 * editorId, boolean activate)
                 * 
                 * to open an editor and set activate to false! So that we can
                 * separate opening from activating, which save us duplicate
                 * sending of activated events.
                 */

                IEditorDescriptor descriptor = IDE.getEditorDescriptor(file);
                if (descriptor.isOpenExternal()) {
                    /*
                     * WORK-AROUND for #2807684: Editors are opened externally
                     * erroneously
                     * 
                     * <a href=
                     * "https://sourceforge.net/tracker/?func=detail&aid=2807684&group_id=167540&atid=843359"
                     * ></a>
                     * 
                     * TODO Open as an internal editor
                     */
                    log.warn("Editor for file " + file.getName()
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
                log.error("Could not initialize part: ", e);
            }
        }

        return null;
    }

    @Override
    public boolean openEditor(IEditorPart part) {
        IWorkbenchWindow window = EditorAPI.getActiveWindow();
        if (window == null)
            return false;

        IWorkbenchPage page = window.getActivePage();
        try {
            page.openEditor(part.getEditorInput(), part.getEditorSite().getId());
            return true;
        } catch (PartInitException e) {
            log.error(
                "failed to open editor part with title: " + part.getTitle(), e);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeEditor(IEditorPart part) {
        IWorkbenchWindow window = EditorAPI.getActiveWindow();
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

        IWorkbenchWindow[] windows = EditorAPI.getWindows();
        for (IWorkbenchWindow window : windows) {
            IWorkbenchPage[] pages = window.getPages();

            for (IWorkbenchPage page : pages) {
                IEditorReference[] editorRefs = page.getEditorReferences();

                for (IEditorReference reference : editorRefs) {

                    IEditorPart editorPart = reference.getEditor(false);
                    if (!restore)
                        continue;
                    if (editorPart == null) {
                        log.debug("IWorkbenchPage."
                            + "getEditorReferences()"
                            + " returned IEditorPart which needs to be restored: "
                            + reference.getTitle());
                        // Making this call might cause partOpen events
                        editorPart = reference.getEditor(true);
                    }

                    if (editorPart != null) {
                        editorParts.add(editorPart);
                    } else {
                        log.warn("Internal Error: IEditorPart could "
                            + "not be restored: " + reference);
                    }
                }
            }
        }

        return editorParts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IEditorPart getActiveEditor() {
        IWorkbenchWindow window = EditorAPI.getActiveWindow();
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                return page.getActiveEditor();
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IResource getEditorResource(IEditorPart editorPart) {

        IEditorInput input = editorPart.getEditorInput();

        IResource resource = ResourceUtil.getResource(input);

        if (resource == null) {
            log.warn("Could not get resource from IEditorInput " + input);
        }

        return resource;
    }

    public static int getLine(ITextViewerExtension5 viewer, int offset) {
        return viewer.widgetLineOfWidgetOffset(viewer
            .modelOffset2WidgetOffset(offset));
    }

    /**
     * Given a new ITextSelection this method tries to reveal as much as
     * possible, but moving only in a conservative manner.
     */
    public static void reveal(IEditorPart editorPart, ITextSelection selection) {

        ITextViewer viewer = EditorAPI.getViewer(editorPart);

        IRegion visible = new Region(viewer.getTopIndexStartOffset(),
            viewer.getBottomIndexEndOffset() - viewer.getTopIndexStartOffset());
        IRegion newSelection = new Region(selection.getOffset(),
            selection.getLength());

        /*
         * log.debug("Visible: " + visible + " - Selected: " + newSelection);
         */

        if (!TextUtilities.overlaps(visible, newSelection)) {
            viewer.revealRange(selection.getOffset(), selection.getLength());
            return;
        }

        if (!(viewer instanceof ITextViewerExtension5))
            return;

        ITextViewerExtension5 viewer5 = (ITextViewerExtension5) viewer;

        // Make sure that we show as much of the selection
        // as possible with as little moving as possible
        if (newSelection.getOffset() > visible.getOffset()) {
            // newSelection is below viewport

            if (newSelection.getOffset() + newSelection.getLength() > visible
                .getOffset() + visible.getLength()) {
                // there is space to go down

                int visibleStartLine = getLine(viewer5, visible.getOffset());
                int selectionStartLine = getLine(viewer5,
                    newSelection.getOffset());
                int visibleEndLine = getLine(viewer5, visible.getOffset()
                    + visible.getLength());

                int targetLine = visibleEndLine
                    + (selectionStartLine - visibleStartLine);

                targetLine = Math.min(targetLine,
                    getLine(viewer5, viewer.getDocument().getLength()));

                int targetOffset = visible.getOffset() + visible.getLength();
                while (getLine(viewer5, targetOffset) < targetLine) {
                    targetOffset++;
                }

                viewer.revealRange(newSelection.getOffset(), targetOffset
                    - newSelection.getOffset());
            }

        } else {
            if (newSelection.getOffset() < visible.getOffset()) {
                // newSelection is above viewport

                if (newSelection.getOffset() + newSelection.getLength() < visible
                    .getOffset() + visible.getLength()) {
                    // there is space to go up

                    int visibleStartLine = getLine(viewer5, visible.getOffset());
                    int selectionEndLine = getLine(viewer5,
                        newSelection.getOffset() + newSelection.getLength());
                    int visibleEndLine = getLine(viewer5, visible.getOffset()
                        + visible.getLength());

                    int targetLine = visibleStartLine
                        + (selectionEndLine - visibleEndLine);

                    targetLine = Math.max(0, targetLine);

                    int targetOffset = visible.getOffset();
                    while (getLine(viewer5, targetOffset) > targetLine) {
                        targetOffset--;
                    }

                    viewer.revealRange(newSelection.getOffset(), targetOffset
                        - newSelection.getOffset());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ITextSelection getSelection(IEditorPart editorPart) {

        if (!(editorPart instanceof ITextEditor)) {
            return TextSelection.emptySelection();
        }

        ITextEditor textEditor = (ITextEditor) editorPart;
        ISelectionProvider selectionProvider = textEditor
            .getSelectionProvider();
        if (selectionProvider != null) {
            return (ITextSelection) selectionProvider.getSelection();
        }

        return TextSelection.emptySelection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEditable(final IEditorPart editorPart,
        final boolean newIsEditable) {

        SWTUtils.runSafeSWTSync(log, new Runnable() {
            @Override
            public void run() {
                ITextViewer textViewer = EditorAPI.getViewer(editorPart);

                if (textViewer == null)
                    return;

                boolean isEditable = !lockedEditors.contains(editorPart);

                // Already as we want it?
                if (newIsEditable == isEditable)
                    return;

                log.trace(editorPart.getEditorInput().getName()
                    + " set to editable: " + newIsEditable);

                updateStatusLine(editorPart, newIsEditable);

                if (newIsEditable) {
                    lockedEditors.remove(editorPart);

                    if (textViewer instanceof ITextViewerExtension)
                        ((ITextViewerExtension) textViewer)
                            .removeVerifyKeyListener(EditorAPI.this.keyVerifier);

                    // enable editing and undo-manager
                    textViewer.setEditable(true);

                    // TODO use undoLevel from Preferences (TextEditorPlugin)
                    if (textViewer instanceof ITextViewerExtension6)
                        ((ITextViewerExtension6) textViewer).getUndoManager()
                            .setMaximalUndoLevel(200);

                } else {
                    lockedEditors.add(editorPart);

                    if (textViewer instanceof ITextViewerExtension)
                        ((ITextViewerExtension) textViewer)
                            .prependVerifyKeyListener(EditorAPI.this.keyVerifier);

                    // disable editing and undo-manager
                    textViewer.setEditable(false);

                    if (textViewer instanceof ITextViewerExtension6)
                        ((ITextViewerExtension6) textViewer).getUndoManager()
                            .setMaximalUndoLevel(0);
                }
            }
        });
    }

    /**
     * Map of currently registered EditorListeners for removal via
     * removeSharedEditorListener
     */
    protected Map<Pair<EditorManager, IEditorPart>, EditorListener> editorListeners = new HashMap<Pair<EditorManager, IEditorPart>, EditorListener>();

    @Override
    public void addSharedEditorListener(EditorManager editorManager,
        IEditorPart editorPart) {

        assert SWTUtils.isSWT();

        if (editorManager == null || editorPart == null)
            throw new IllegalArgumentException();

        Pair<EditorManager, IEditorPart> key = new Pair<EditorManager, IEditorPart>(
            editorManager, editorPart);

        if (editorListeners.containsKey(key)) {
            log.error(
                "SharedEditorListener was added twice: "
                    + editorPart.getTitle(), new StackTrace());
            removeSharedEditorListener(editorManager, editorPart);
        }
        EditorListener listener = new EditorListener(editorManager);
        listener.bind(editorPart);
        editorListeners.put(key, listener);
    }

    @Override
    public void removeSharedEditorListener(EditorManager editorManager,
        IEditorPart editorPart) {

        assert SWTUtils.isSWT();

        if (editorManager == null || editorPart == null)
            throw new IllegalArgumentException();

        Pair<EditorManager, IEditorPart> key = new Pair<EditorManager, IEditorPart>(
            editorManager, editorPart);

        EditorListener listener = editorListeners.remove(key);
        if (listener == null)
            throw new IllegalArgumentException(
                "The given editorPart has no EditorListener");

        listener.unbind();
    }

    @Override
    public void reveal(IEditorPart editorPart, ILineRange lineRange) {

        int top = lineRange.getStartLine();
        int bottom = top + lineRange.getNumberOfLines();

        ITextViewer viewer = EditorAPI.getViewer(editorPart);
        IDocument document = viewer.getDocument();

        // Normalize line range...
        int lines = document.getNumberOfLines();
        top = Math.max(0, Math.min(lines - 1, top));
        bottom = Math.max(0, Math.min(lines - 1, bottom));

        try {
            /**
             * Show the middle of the user's (with
             * {@link User.Permission#WRITE_ACCESS}) viewport.
             */
            viewer.revealRange(
                document.getLineOffset(top + ((bottom - top) / 2)), 0);
        } catch (BadLocationException e) {
            log.error("Internal Error: BadLocationException - ", e);
        }

    }

    public static ILineRange getViewport(ITextViewer viewer) {

        int top = viewer.getTopIndex();
        // Have to add +1 because a LineRange should excludes the bottom line
        int bottom = viewer.getBottomIndex() + 1;

        if (bottom < top) {
            // FIXME This warning occurs when the document is shorter than the
            // viewport
            log.warn("Viewport Range Problem in " + viewer + ": Bottom == "
                + bottom + " < Top == " + top);
            bottom = top;
        }

        return new LineRange(top, bottom - top);
    }

    @Override
    public ILineRange getViewport(IEditorPart editorPart) {

        ITextViewer textViewer = EditorAPI.getViewer(editorPart);
        if (textViewer == null)
            return null;

        return getViewport(textViewer);
    }

    /**
     * Needs UI-thread.
     */
    protected void updateStatusLine(IEditorPart editorPart, boolean editable) {
        Object adapter = editorPart.getAdapter(IEditorStatusLine.class);
        if (adapter != null) {
            IEditorStatusLine statusLine = (IEditorStatusLine) adapter;
            statusLine.setMessage(false, editable ? "" : "Not editable", null);
        }
    }

    /**
     * @return true when the editor was successfully saved
     * 
     * @nonSWT This method may not be called from the SWT UI Thread!
     */
    public static boolean saveEditor(final IEditorPart editor) {

        if (editor == null)
            return true;

        final BlockingProgressMonitor monitor = new BlockingProgressMonitor();

        // save document
        SWTUtils.runSafeSWTSync(log, new Runnable() {
            @Override
            public void run() {
                editor.doSave(monitor);
            }
        });

        // Wait for saving or canceling to be done
        try {
            monitor.await();
        } catch (InterruptedException e) {
            log.warn("Code not designed to handle InterruptedException");
            Thread.currentThread().interrupt();
        }

        return !monitor.isCanceled();
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
    protected static IWorkbenchWindow getActiveWindow() {
        try {
            return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        } catch (Exception e) {
            return null;
        }
    }

    protected static IWorkbenchWindow[] getWindows() {
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

    public static boolean existUnsavedFiles(IProject proj) {
        // Note: We don't need to check editors that aren't loaded.
        for (IEditorPart editorPart : getOpenEditors(false)) {
            final IEditorInput editorInput = editorPart.getEditorInput();
            if (!(editorInput instanceof IFileEditorInput)) {
                continue;
            }
            // Object identity instead of using equals is sufficient.
            IFile file = ((IFileEditorInput) editorInput).getFile();
            if (file.getProject() == proj && editorPart.isDirty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Syntactic sugar for getting the path of the IEditorPart returned by
     * getActiveEditor()
     */
    @Override
    public SPath getActiveEditorPath() {
        IEditorPart newActiveEditor = getActiveEditor();
        if (newActiveEditor == null)
            return null;

        return getEditorPath(newActiveEditor);
    }

    /**
     * Syntactic sugar for getting the path of the given editor part.
     */
    @Override
    public SPath getEditorPath(IEditorPart editorPart) {
        IResource resource = getEditorResource(editorPart);
        if (resource == null) {
            return null;
        }
        IPath path = resource.getProjectRelativePath();

        if (path == null) {
            log.warn("Could not get path from resource " + resource);
        }

        return new SPath(resource);
    }
}
