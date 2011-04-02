package de.fu_berlin.inf.dpp.editor.internal;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
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
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;
import org.picocontainer.annotations.Nullable;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.editor.annotations.SelectionAnnotation;
import de.fu_berlin.inf.dpp.editor.annotations.ViewportAnnotation;
import de.fu_berlin.inf.dpp.ui.dialogs.WarningMessageDialog;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.util.BlockingProgressMonitor;
import de.fu_berlin.inf.dpp.util.Pair;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Utils;

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

    protected Saros saros;

    public EditorAPI(Saros saros) {
        this.saros = saros;
    }

    private static Logger log = Logger.getLogger(EditorAPI.class.getName());

    protected final VerifyKeyListener keyVerifier = new VerifyKeyListener() {
        public void verifyKey(VerifyEvent event) {
            if (event.character > 0) {
                event.doit = false;

                Object adapter = getActiveEditor().getAdapter(
                    IEditorStatusLine.class);
                if (adapter != null) {
                    SarosView
                        .showNotification("Read-Only Notification",
                            "You have only read access and therefore can't perform modifications.");
                    Toolkit.getDefaultToolkit().beep();
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
    public void addEditorPartListener(EditorManager editorManager) {

        assert Utils.isSWT();

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
    public IEditorPart openEditor(SPath path) {

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
                return IDE.openEditor(page, file);
            } catch (PartInitException e) {
                log.error("Could not initialize part: ", e);
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
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
    public IResource getEditorResource(IEditorPart editorPart) {

        IEditorInput input = editorPart.getEditorInput();

        IResource resource = ResourceUtil.getResource(input);

        if (resource == null) {
            log.warn("Could not get resource from IEditorInput " + input);
        }

        return resource;
    }

    /**
     * This implementation does not really set the selection but rather adds an
     * selection annotation.
     * 
     */
    public void setSelection(IEditorPart editorPart, ITextSelection selection,
        User source, boolean following) {

        if (!(editorPart instanceof ITextEditor)) {
            return;
        }

        ITextEditor textEditor = (ITextEditor) editorPart;

        IDocumentProvider docProvider = textEditor.getDocumentProvider();

        if (docProvider != null) {
            IEditorInput input = textEditor.getEditorInput();
            IAnnotationModel model = docProvider.getAnnotationModel(input);

            if (model == null) {
                return;
            }
            if (selection.isEmpty()) {
                setSelectionAnnotation(source, model, null, null);
                return;
            }

            if (following) {
                reveal(editorPart, selection);
            }
            /*
             * If the selection's length is 0 it will be displayed as a cursor.
             * It is moved one character to the left if the offset is at the end
             * of the line but not already at the start of the line. So the
             * cursor is not displayed in completely empty lines. :-(
             */
            int offset = selection.getOffset();
            int length = selection.getLength();
            boolean isCursor = length == 0;

            if (isCursor) {
                IDocument document = docProvider.getDocument(input);
                if (document != null && document.getLength() != 0) {
                    try {
                        IRegion lineInfo = document
                            .getLineInformationOfOffset(offset);
                        int lineLength = lineInfo.getLength();
                        if (lineLength != 0) {
                            // Cursor visible if line has content.
                            length = 1;
                            if (offset == (lineInfo.getOffset() + lineLength)) {
                                // Move one position left if at end of line.
                                offset--;
                            }
                        }
                    } catch (BadLocationException e) {
                        // Ignored intentionally.
                    }
                }
            }

            setSelectionAnnotation(source, model, new SelectionAnnotation(
                source, isCursor), new Position(offset, length));
        }
    }

    public static Iterable<Annotation> toIterable(final IAnnotationModel model) {
        return new Iterable<Annotation>() {
            @SuppressWarnings("unchecked")
            public Iterator<Annotation> iterator() {
                return model.getAnnotationIterator();
            }
        };
    }

    protected void setSelectionAnnotation(User source, IAnnotationModel model,
        @Nullable SarosAnnotation newAnnotation, @Nullable Position position) {

        if ((newAnnotation == null) != (position == null)) {
            throw new IllegalArgumentException(
                "Either both Annotation and Position must be null or non-null");
        }

        for (Annotation annotation : toIterable(model)) {

            if (!(annotation instanceof SelectionAnnotation)) {
                continue;
            }

            SarosAnnotation oldAnnotation = (SarosAnnotation) annotation;
            if (oldAnnotation.getSource().equals(source)) {
                // If model supports IAnnotationModelExtension we can
                // just update the existing annotation.
                if (newAnnotation != null
                    && model instanceof IAnnotationModelExtension) {
                    IAnnotationModelExtension extension = (IAnnotationModelExtension) model;
                    extension.replaceAnnotations(
                        new Annotation[] { oldAnnotation },
                        Collections.singletonMap(newAnnotation, position));
                    return;
                }
                model.removeAnnotation(annotation);
            }
        }

        if (newAnnotation != null)
            model.addAnnotation(newAnnotation, position);
    }

    public static int getLine(ITextViewerExtension5 viewer, int offset) {
        return viewer.widgetLineOfWidgetOffset(viewer
            .modelOffset2WidgetOffset(offset));
    }

    /**
     * Given a new ITextSelection this method tries to reveal as much as
     * possible, but moving only in a conservative manner.
     * 
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
    public void setEditable(final IEditorPart editorPart,
        final boolean newIsEditable) {

        Utils.runSafeSWTSync(log, new Runnable() {
            public void run() {
                ITextViewerExtension textViewer = (ITextViewerExtension) EditorAPI
                    .getViewer(editorPart);

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
                    textViewer
                        .removeVerifyKeyListener(EditorAPI.this.keyVerifier);

                    // enable editing and undo-manager
                    SourceViewer sourceViewer = (SourceViewer) textViewer;
                    sourceViewer.setEditable(true);

                    // TODO use undoLevel from Preferences (TextEditorPlugin)
                    sourceViewer.getUndoManager().setMaximalUndoLevel(200);

                } else {
                    lockedEditors.add(editorPart);
                    textViewer
                        .prependVerifyKeyListener(EditorAPI.this.keyVerifier);

                    // disable editing and undo-manager
                    SourceViewer sourceViewer = (SourceViewer) textViewer;
                    sourceViewer.setEditable(false);
                    sourceViewer.getUndoManager().setMaximalUndoLevel(0);
                }
            }
        });
    }

    /**
     * Map of currently registered EditorListeners for removal via
     * removeSharedEditorListener
     */
    protected Map<Pair<EditorManager, IEditorPart>, EditorListener> editorListeners = new HashMap<Pair<EditorManager, IEditorPart>, EditorListener>();

    public void addSharedEditorListener(EditorManager editorManager,
        IEditorPart editorPart) {

        assert Utils.isSWT();

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

    public void removeSharedEditorListener(EditorManager editorManager,
        IEditorPart editorPart) {

        assert Utils.isSWT();

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

    public void reveal(IEditorPart editorPart, ILineRange viewport) {

        int top = viewport.getStartLine();
        int bottom = top + viewport.getNumberOfLines();

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

    public void setViewportAnnotation(IEditorPart editorPart,
        ILineRange viewport, User user) {

        int top = viewport.getStartLine();
        int bottom = top + viewport.getNumberOfLines();

        ITextViewer viewer = EditorAPI.getViewer(editorPart);

        updateViewportAnnotation(viewer, top, bottom, user);
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

    public ILineRange getViewport(IEditorPart editorPart) {
        ITextViewer textViewer = EditorAPI.getViewer(editorPart);
        if (textViewer == null)
            return null;

        return getViewport(textViewer);
    }

    protected void updateViewportAnnotation(ITextViewer viewer, int top,
        int bottom, User source) {

        if (!(viewer instanceof ISourceViewer)) {
            return;
        }

        IAnnotationModel model = ((ISourceViewer) viewer).getAnnotationModel();

        for (@SuppressWarnings("unchecked")
        Iterator<Annotation> it = model.getAnnotationIterator(); it.hasNext();) {
            Annotation annotation = it.next();
            if (annotation instanceof ViewportAnnotation) {
                if (((ViewportAnnotation) annotation).getSource()
                    .equals(source))
                    model.removeAnnotation(annotation);
            }
        }

        IDocument document = viewer.getDocument();
        try {
            int lines = document.getNumberOfLines();
            top = Math.max(0, Math.min(lines - 1, top));
            bottom = Math.max(0, Math.min(lines - 1, bottom));

            int start = document.getLineOffset(top);
            if (start == -1)
                throw new BadLocationException("Start line -1");
            int end = document.getLineOffset(bottom);
            if (end == -1 || end < start)
                throw new BadLocationException("End line -1 or less than start");
            SarosAnnotation annotation = new ViewportAnnotation(source);
            Position position = new Position(start, end - start);
            model.addAnnotation(annotation, position);
        } catch (BadLocationException e) {
            log.warn("Internal Error:", e);
        }
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
        Utils.runSafeSWTSync(log, new Runnable() {
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
        } catch (SWTException e) {
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
            return Utils.runSWTSync(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    /**
                     * TODO saveAllEditors does not save the Documents that we
                     * are modifying in the background
                     */
                    return IDE.saveAllEditors(
                        new IResource[] { projectToSave }, confirm);
                }
            });
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

    public static IWorkbenchWindow getAWorkbenchWindow() {

        if (PlatformUI.getWorkbench().getDisplay().isDisposed()) {
            return null;
        }

        IWorkbenchWindow w = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow();
        if (w != null) {
            return w;
        }

        IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
            .getWorkbenchWindows();
        if (windows.length > 0) {
            return windows[0];
        } else {
            return null;
        }
    }

    /**
     * Tries to get a shell that is centered on the shells of the application.
     * Use instead of Display.getDefault().getActiveShell()!
     */
    public static Shell getShell() {

        IWorkbenchWindow window = getAWorkbenchWindow();

        if (window != null) {
            Shell shell = window.getShell();
            if (!shell.isDisposed()) {
                return shell;
            }
        }

        // Window was null or shell disposed
        Display display = PlatformUI.getWorkbench().getDisplay();
        if (!display.isDisposed())
            return display.getActiveShell();

        // Give up:
        return null;
    }

    /**
     * Syntactic sugar for getting the path of the IEditorPart returned by
     * getActiveEditor()
     */
    public SPath getActiveEditorPath() {
        IEditorPart newActiveEditor = getActiveEditor();
        if (newActiveEditor == null)
            return null;

        return getEditorPath(newActiveEditor);
    }

    /**
     * Syntactic sugar for getting the path of the given editor part.
     */
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
