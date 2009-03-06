/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.manipulation.ConvertLineDelimitersOperation;
import org.eclipse.core.filebuffers.manipulation.FileBufferOperationRunner;
import org.eclipse.core.filebuffers.manipulation.TextFileBufferOperation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.ViewportActivity;
import de.fu_berlin.inf.dpp.activities.EditorActivity.Type;
import de.fu_berlin.inf.dpp.editor.annotations.ContributionAnnotation;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.editor.annotations.ViewportAnnotation;
import de.fu_berlin.inf.dpp.editor.internal.ContributionAnnotationManager;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.editor.internal.IEditorAPI;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.IActivityListener;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.BalloonNotification;
import de.fu_berlin.inf.dpp.util.FileUtil;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.ValueChangeListener;

/**
 * The EditorManager is responsible for handling all editors in a DPP-session.
 * This includes the functionality of listening for user inputs in an editor,
 * locking the editors of the observer.
 * 
 * The EditorManager contains the testable logic. All untestable logic should
 * only appear in an class of the {@link IEditorAPI} type.
 * 
 * @author rdjemili
 * 
 *         TODO CO Since it was forgotton to reset the DriverEditors after a
 *         session closed, it is highly likely that this whole class needs to be
 *         reviewed for restarting issues
 * 
 */
public class EditorManager implements IActivityProvider, ISharedProjectListener {

    private class DirtyStateListener implements IElementStateListener {

        public boolean enabled = true;

        public void elementDirtyStateChanged(Object element, boolean isDirty) {

            if (!enabled)
                return;

            if (isDirty)
                return;

            if (!isDriver)
                return;

            if (!(element instanceof FileEditorInput))
                return;

            FileEditorInput fileEditorInput = (FileEditorInput) element;
            IFile file = fileEditorInput.getFile();

            if (file.getProject() != EditorManager.this.sharedProject
                .getProject()) {
                return;
            }

            IPath path = file.getProjectRelativePath();
            sendEditorActivitySaved(path);
        }

        public void elementContentAboutToBeReplaced(Object element) {
            // ignore
        }

        public void elementContentReplaced(Object element) {
            // ignore
        }

        public void elementDeleted(Object element) {
            // ignore
        }

        public void elementMoved(Object originalElement, Object movedElement) {
            // ignore
        }
    }

    /**
     * @author rdjemili
     * 
     */
    private class EditorPool {
        private final Map<IPath, HashSet<IEditorPart>> editorParts = new HashMap<IPath, HashSet<IEditorPart>>();

        public void add(IEditorPart editorPart) {
            IResource resource = EditorManager.this.editorAPI
                .getEditorResource(editorPart);
            IPath path = resource.getProjectRelativePath();

            if (path == null) {
                return;
            }

            HashSet<IEditorPart> editors = this.editorParts.get(path);

            EditorManager.this.editorAPI.addSharedEditorListener(editorPart);
            EditorManager.this.editorAPI.setEditable(editorPart,
                EditorManager.this.isDriver);

            IDocumentProvider documentProvider = EditorManager.this.editorAPI
                .getDocumentProvider(editorPart.getEditorInput());

            documentProvider
                .addElementStateListener(EditorManager.this.dirtyStateListener);

            IDocument document = EditorManager.this.editorAPI
                .getDocument(editorPart);

            if (editors == null) {
                editors = new HashSet<IEditorPart>();
                this.editorParts.put(path, editors);
            }

            // if line delimiters are not in unix style convert them
            if (document instanceof IDocumentExtension4) {

                if (!((IDocumentExtension4) document).getDefaultLineDelimiter()
                    .equals("\n")) {
                    convertLineDelimiters(editorPart);
                }
                ((IDocumentExtension4) document).setInitialLineDelimiter("\n");
            } else {
                EditorManager.log
                    .error("Can't discover line delimiter of document");
            }
            document.addDocumentListener(EditorManager.this.documentListener);
            editors.add(editorPart);
            lastEditTimes.put(path, System.currentTimeMillis());
            lastRemoteEditTimes.put(path, System.currentTimeMillis());
        }

        private void convertLineDelimiters(IEditorPart editorPart) {

            EditorManager.log.debug("Converting line delimiters...");

            // get path of file
            IFile file = ((FileEditorInput) editorPart.getEditorInput())
                .getFile();
            IPath[] paths = new IPath[1];
            paths[0] = file.getFullPath();

            boolean makeReadable = false;

            ResourceAttributes resourceAttributes = file
                .getResourceAttributes();
            if (resourceAttributes.isReadOnly()) {
                resourceAttributes.setReadOnly(false);
                try {
                    file.setResourceAttributes(resourceAttributes);
                    makeReadable = true;
                } catch (CoreException e) {
                    log.error(
                        "Error making file readable for delimiter conversion:",
                        e);
                }
            }

            ITextFileBufferManager buffManager = FileBuffers
                .getTextFileBufferManager();

            // convert operation to change line delimiters
            TextFileBufferOperation convertOperation = new ConvertLineDelimitersOperation(
                "\n");

            // operation runner for the convert operation
            FileBufferOperationRunner runner = new FileBufferOperationRunner(
                buffManager, null);

            // execute convert operation in runner
            try {
                runner.execute(paths, convertOperation,
                    new NullProgressMonitor());
            } catch (OperationCanceledException e) {
                EditorManager.log.error("Can't convert line delimiters:", e);
            } catch (CoreException e) {
                EditorManager.log.error("Can't convert line delimiters:", e);
            }

            if (makeReadable) {
                resourceAttributes.setReadOnly(true);
                try {
                    file.setResourceAttributes(resourceAttributes);
                } catch (CoreException e) {
                    EditorManager.log
                        .error(
                            "Error restoring readable state to false after delimiter conversion:",
                            e);
                }
            }
        }

        public void remove(IEditorPart editorPart) {
            IResource resource = EditorManager.this.editorAPI
                .getEditorResource(editorPart);
            IPath path = resource.getProjectRelativePath();

            if (path == null) {
                return;
            }

            HashSet<IEditorPart> editors = this.editorParts.get(path);
            editors.remove(editorPart);
        }

        public Set<IEditorPart> getEditors(IPath path) {
            HashSet<IEditorPart> set = this.editorParts.get(path);
            return set == null ? new HashSet<IEditorPart>() : set; // HACK
        }

        public Set<IEditorPart> getAllEditors() {
            Set<IEditorPart> all = new HashSet<IEditorPart>();

            for (Set<IEditorPart> parts : this.editorParts.values()) {
                for (IEditorPart part : parts) {
                    all.add(part);
                }
            }

            return all;
        }

        public void removeAllEditors() {
            editorParts.clear();
        }
    }

    private static Logger log = Logger.getLogger(EditorManager.class.getName());

    private static EditorManager instance;

    private IEditorAPI editorAPI;

    private ISharedProject sharedProject;

    private final List<IActivityListener> activityListeners = new LinkedList<IActivityListener>();

    private boolean isFollowing = false;

    private boolean isDriver;

    private final EditorPool editorPool = new EditorPool();

    private final DirtyStateListener dirtyStateListener = new DirtyStateListener();

    public class StoppableDocumentListener implements IDocumentListener {

        boolean enabled = true;

        public void documentAboutToBeChanged(final DocumentEvent event) {

            if (enabled) {
                String text = event.getText() == null ? "" : event.getText();
                textAboutToBeChanged(event.getOffset(), text,
                    event.getLength(), event.getDocument());
            }
        }

        public void documentChanged(final DocumentEvent event) {
            // do nothing. We handeled everything in documentAboutToBeChanged
        }
    }

    private final StoppableDocumentListener documentListener = new StoppableDocumentListener();

    // TODO save only the editor of the followed driver
    private IPath activeDriverEditor;

    private final Set<IPath> driverEditors = new HashSet<IPath>();

    private HashMap<User, ITextSelection> driverTextSelections = new HashMap<User, ITextSelection>();

    /** all files that have connected document providers */
    private final Set<IFile> connectedFiles = new HashSet<IFile>();

    private final List<ISharedEditorListener> editorListeners = new ArrayList<ISharedEditorListener>();

    public HashMap<IPath, Long> lastEditTimes = new HashMap<IPath, Long>();

    public HashMap<IPath, Long> lastRemoteEditTimes = new HashMap<IPath, Long>();

    private ContributionAnnotationManager contributionAnnotationManager;

    public static EditorManager getDefault() {
        if (EditorManager.instance == null) {
            EditorManager.instance = new EditorManager();
        }

        return EditorManager.instance;
    }

    public void setEditorAPI(IEditorAPI editorAPI) {
        this.editorAPI = editorAPI;
        editorAPI.setEditorManager(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISessionListener
     */
    public void sessionStarted(ISharedProject session) {
        this.sharedProject = session;
        assert (this.editorPool.editorParts.isEmpty());
        this.isDriver = this.sharedProject.isDriver();
        this.sharedProject.addListener(this);

        // Add ConsistencyListener
        Saros.getDefault().getSessionManager().getSharedProject()
            .getConcurrentDocumentManager().getConsistencyToResolve().add(
                new ValueChangeListener<Boolean>() {
                    public void setValue(Boolean inconsistency) {
                        if (inconsistency) {
                            Util.runSafeSWTSync(log, new Runnable() {
                                public void run() {
                                    try {
                                        // Open Session view
                                        PlatformUI
                                            .getWorkbench()
                                            .getActiveWorkbenchWindow()
                                            .getActivePage()
                                            .showView(
                                                "de.fu_berlin.inf.dpp.ui.SessionView",
                                                null,
                                                IWorkbenchPage.VIEW_ACTIVATE);
                                    } catch (PartInitException e) {
                                        log
                                            .error("Could not open session view!");
                                    }
                                }
                            });
                        }
                    }
                });

        this.sharedProject.getActivityManager().addProvider(this);
        this.contributionAnnotationManager = new ContributionAnnotationManager(
            session);

        // TODO Review Why?
        activateOpenEditors();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISessionListener
     */
    public void sessionEnded(ISharedProject session) {
        setAllEditorsToEditable();
        removeAllAnnotations(null, null);

        // FIXME remove this.dirtyStateListener from IDocumentProvider

        this.sharedProject.removeListener(this);
        this.sharedProject.getActivityManager().removeProvider(this);
        this.sharedProject = null;
        this.editorPool.removeAllEditors();
        this.lastEditTimes.clear();
        this.lastRemoteEditTimes.clear();
        this.contributionAnnotationManager.dispose();
        this.contributionAnnotationManager = null;
        this.activeDriverEditor = null;
        this.driverEditors.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISessionListener
     */
    public void invitationReceived(IIncomingInvitationProcess invitation) {
        // ignore
    }

    public void addSharedEditorListener(ISharedEditorListener editorListener) {
        if (!this.editorListeners.contains(editorListener)) {
            this.editorListeners.add(editorListener);
        }
    }

    public void removeSharedEditorListener(ISharedEditorListener editorListener) {
        this.editorListeners.remove(editorListener);
    }

    /**
     * @return the path to the resource that the driver is currently editting.
     *         Can be <code>null</code>.
     */
    public IPath getActiveDriverEditor() {
        return this.activeDriverEditor;
    }

    /**
     * Returns the resource paths of editors that the driver is currently using.
     * 
     * @return all paths (in project-relative format) of files that the driver
     *         is currently editing by using an editor. Never returns
     *         <code>null</code>. A empty set is returned if there are no
     *         currently opened editors.
     */
    public Set<IPath> getDriverEditors() {
        return this.driverEditors;
    }

    /**
     * Return the document of the given path.
     * 
     * @param path
     *            the path of the wanted document
     * @return the document or null if no document exists with given path or no
     *         editor with this file is open
     */
    public IDocument getDocument(IPath path) {
        Set<IEditorPart> editors = getEditors(path);
        if (editors.isEmpty())
            return null;
        AbstractTextEditor editor = (AbstractTextEditor) editors.toArray()[0];
        IEditorInput input = editor.getEditorInput();
        return editor.getDocumentProvider().getDocument(input);
    }

    /**
     * @param user
     *            User for who's text selection will be returned.
     * @return the text selection of given user or <code>null</code> if that
     *         user is not a driver.
     */
    public ITextSelection getDriverTextSelection(User user) {
        return this.driverTextSelections.get(user);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.editor.ISharedEditorListener
     */
    public void viewportChanged(IEditorPart part, ILineRange viewport) {
        if (!this.sharedProject.isDriver()) {
            return;
        }

        IPath path = editorAPI.getEditorResource(part).getProjectRelativePath();

        fireActivity(new ViewportActivity(viewport, path));
    }

    public void selectionChanged(IEditorPart part, ITextSelection newSelection) {

        IPath path = editorAPI.getEditorResource(part).getProjectRelativePath();

        int offset = newSelection.getOffset();
        int length = newSelection.getLength();

        if (path == null) {
            log.error("Couldn't get editor!");
        } else
            fireActivity(new TextSelectionActivity(offset, length, path));
    }

    /**
     * Asks the ConcurrentDocumentManager if there are currently any
     * inconsistencies to resolve.
     */
    public boolean isConsistencyToResolve() {
        ISharedProject project = Saros.getDefault().getSessionManager()
            .getSharedProject();

        if (project == null)
            return false;

        return project.getConcurrentDocumentManager().getConsistencyToResolve()
            .getValue();

    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.editor.ISharedEditorListener
     */
    public void textAboutToBeChanged(int offset, String text, int replace,
        IDocument document) {

        /*
         * TODO When Inconsistencies exists, all listeners should be stopped
         * rather than catching events -> Think Start/Stop on the SharedProject
         */
        if (!this.isDriver || isConsistencyToResolve()) {

            /**
             * TODO: If we are not a driver, then receiving this event might
             * indicate that the user somehow achieved to change his document.
             * We should run a consistency check.
             * 
             * But watch out for changes because of a consistency check!
             */
            return;
        }

        IEditorPart changedEditor = null;

        // search editor which changed
        for (IEditorPart editor : editorPool.getAllEditors()) {
            if (editorAPI.getDocument(editor) == document) {
                changedEditor = editor;
                break;
            }
        }
        assert changedEditor != null;

        IPath path = editorAPI.getEditorResource(changedEditor)
            .getProjectRelativePath();

        if (path != null) {

            String replacedText;
            try {
                replacedText = document.get(offset, replace);
            } catch (BadLocationException e) {
                log.error("Offset and/or replace invalid", e);

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < replace; i++)
                    sb.append("?");
                replacedText = sb.toString();
            }

            TextEditActivity activity = new TextEditActivity(offset, text,
                replacedText, path, Saros.getDefault().getMyJID().toString());

            EditorManager.this.lastEditTimes.put(path, System
                .currentTimeMillis());

            fireActivity(activity);

            /*
             * TODO Investigate if this is really needed here
             */
            {
                IEditorInput input = changedEditor.getEditorInput();
                IDocumentProvider provider = this.editorAPI
                    .getDocumentProvider(input);
                IAnnotationModel model = provider.getAnnotationModel(input);
                contributionAnnotationManager.splitAnnotation(model, offset);
            }
        } else {
            log.error("Can't get editor path");
        }
    }

    public static IViewPart findView(String id) {
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null)
            return null;

        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        if (window == null)
            return null;

        IWorkbenchPage page = window.getActivePage();
        if (page == null)
            return null;

        return page.findView(id);
    }

    /* ---------- ISharedProjectListener --------- */

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener
     */
    public void roleChanged(User user, boolean replicated) {
        this.isDriver = this.sharedProject.isDriver();

        // TODO Review, because this is causing problems with FollowMode
        activateOpenEditors();

        // TODO Why remove all?
        removeAllAnnotations(ContributionAnnotation.TYPE);

        if (Saros.getDefault().getLocalUser().equals(user)) {

            // get the session view
            IViewPart view = findView("de.fu_berlin.inf.dpp.ui.SessionView");

            if (isDriver) {

                removeAllAnnotations(ViewportAnnotation.TYPE);

                // if session view is not open show the balloon notification in
                // the control which has the keyboard focus
                if (view == null) {
                    Util.runSafeSWTAsync(log, new Runnable() {
                        public void run() {
                            BalloonNotification.showNotification(Display
                                .getDefault().getFocusControl(),
                                "Role changed",
                                "You are now a driver of this session.", 5000);
                        }
                    });
                }
            } else {
                // if session view is not open show the balloon notification in
                // the control which has the keyboard focus
                if (view == null) {
                    Util.runSafeSWTAsync(log, new Runnable() {
                        public void run() {
                            BalloonNotification.showNotification(Display
                                .getDefault().getFocusControl(),
                                "Role changed",
                                "You are now an observer of this session.",
                                5000);
                        }
                    });
                }
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener
     */
    public void userJoined(JID user) {
        // ignore
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener
     */
    public void userLeft(JID user) {
        removeAllAnnotations(user.toString(), null);
        driverTextSelections.remove(sharedProject.getParticipant(user));
    }

    /* ---------- etc --------- */

    /**
     * Opens the editor that is currently used by the driver. This method needs
     * to be called from an UI thread. Is ignored if caller is already driver.
     */
    public void openDriverEditor() {
        if (this.isDriver) {
            return;
        }

        IPath path = getActiveDriverEditor();
        if (path == null) {
            return;
        }

        this.editorAPI
            .openEditor(this.sharedProject.getProject().getFile(path));
    }

    public void setEnableFollowing(boolean enable) {
        this.isFollowing = enable;

        for (ISharedEditorListener editorListener : this.editorListeners) {
            editorListener.followModeChanged(enable);
        }

        if (enable)
            openDriverEditor();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void addActivityListener(IActivityListener listener) {
        this.activityListeners.add(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void removeActivityListener(IActivityListener listener) {
        this.activityListeners.remove(listener);
    }

    /**
     * 
     * @swt This must be called from the SWT thread.
     * 
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void exec(final IActivity activity) {

        activity.dispatch(new AbstractActivityReceiver() {

            @Override
            public boolean receive(EditorActivity editorActivity) {
                if (editorActivity.getType().equals(Type.Activated)) {
                    setActiveDriverEditor(editorActivity.getPath(), true);

                } else if (editorActivity.getType().equals(Type.Closed)) {
                    removeDriverEditor(editorActivity.getPath(), true);

                } else if (editorActivity.getType().equals(Type.Saved)) {
                    saveText(editorActivity.getPath());
                }
                return true;
            }

            @Override
            public boolean receive(TextEditActivity textEditActivity) {
                execTextEdit(textEditActivity);
                return true;
            }

            @Override
            public boolean receive(TextSelectionActivity textSelectionActivity) {
                execTextSelection(textSelectionActivity);
                return true;
            }

            @Override
            public boolean receive(ViewportActivity viewportActivity) {
                execViewport(viewportActivity);
                return true;
            }
        });
    }

    private void execTextEdit(TextEditActivity textEdit) {

        String source = textEdit.getSource();
        User user = Saros.getDefault().getSessionManager().getSharedProject()
            .getParticipant(new JID(source));

        IPath path = textEdit.getEditor();
        IFile file = sharedProject.getProject().getFile(path);

        /*
         * Disable documentListner temporarily to avoid being notified of the
         * change
         */
        documentListener.enabled = false;

        replaceText(file, textEdit.offset, textEdit.replacedText,
            textEdit.text, source);

        documentListener.enabled = true;

        for (IEditorPart editorPart : editorPool.getEditors(path)) {
            editorAPI.setSelection(editorPart, new TextSelection(
                textEdit.offset + textEdit.text.length(), 0), source,
                shouldIFollow(user));
        }
    }

    private void execTextSelection(TextSelectionActivity selection) {
        IPath path = selection.getEditor();
        TextSelection textSelection = new TextSelection(selection.getOffset(),
            selection.getLength());

        User user = sharedProject
            .getParticipant(new JID(selection.getSource()));

        if (user.isDriver()) {
            setDriverTextSelection(user, textSelection);
        }

        if (path == null) {
            EditorManager.log
                .error("Received text selection but have no driver editor");
            return;
        }

        Set<IEditorPart> editors = EditorManager.this.editorPool
            .getEditors(path);
        for (IEditorPart editorPart : editors) {
            this.editorAPI.setSelection(editorPart, textSelection, selection
                .getSource(), shouldIFollow(user));
        }
    }

    // TODO selectable driver to follow
    protected boolean shouldIFollow(User user) {
        return isFollowing && user.isDriver();
    }

    private void execViewport(ViewportActivity viewport) {
        if (isDriver)
            return;

        int top = viewport.getTopIndex();
        int bottom = viewport.getBottomIndex();
        IPath path = viewport.getEditor();
        String source = viewport.getSource();
        /*
         * Check if source is an observed driver and his cursor is outside the
         * viewport. Taking the last line of the driver's last selection might
         * be a bit inaccurate.
         */
        User user = sharedProject.getParticipant(new JID(source));
        ITextSelection driverSelection = getDriverTextSelection(user);
        // Check needed when viewport activity came before the first
        // text selection activity.
        boolean following = shouldIFollow(user);
        if (driverSelection != null) {
            int driverCursor = driverSelection.getEndLine();
            following = following
                && (driverCursor < top || driverCursor > bottom);
        }

        Set<IEditorPart> editors = this.editorPool.getEditors(path);
        for (IEditorPart editorPart : editors) {
            this.editorAPI.setViewport(editorPart, top, bottom, source,
                following);
        }
    }

    // TODO unify partActivated and partOpened
    public void partOpened(IEditorPart editorPart) {

        // if in follow mode and the opened editor is not the followed one,
        // exit Follow Mode
        checkFollowMode(editorPart);

        if (!isSharedEditor(editorPart)) {
            return;
        }

        this.editorPool.add(editorPart);
        sharedEditorActivated(editorPart); // HACK
    }

    public void partActivated(IEditorPart editorPart) {

        // if in follow mode and the activated editor is not the followed one,
        // leave Follow Mode
        checkFollowMode(editorPart);

        if (!isSharedEditor(editorPart)) {
            return;
        }

        sharedEditorActivated(editorPart);
    }

    protected void checkFollowMode(IEditorPart editorPart) {

        if (isFollowing && activeDriverEditor != null) {

            if (!isSharedEditor(editorPart)) {
                setEnableFollowing(false);
                return;
            }

            // if the opened editor is not the followed one, leave following
            // mode
            IResource resource = this.editorAPI.getEditorResource(editorPart);

            if (resource == null) {
                log.warn("Resource not found");
                setEnableFollowing(false);
                return;
            }

            IPath path = resource.getProjectRelativePath();

            if (!activeDriverEditor.equals(path)) {
                setEnableFollowing(false);
            }
        }
    }

    public void partClosed(IEditorPart editorPart) {
        if (!isSharedEditor(editorPart)) {
            return;
        }

        IResource resource = this.editorAPI.getEditorResource(editorPart);
        IPath path = resource.getProjectRelativePath();

        // if closing the following editor, leave follow mode
        if (isFollowing && activeDriverEditor != null
            && activeDriverEditor.equals(path)) {
            setEnableFollowing(false);
        }

        this.editorPool.remove(editorPart);

        if (this.isDriver) {
            removeDriverEditor(path, false);
        }
    }

    /**
     * Checks whether given resource is currently opened.
     * 
     * @param path
     *            the project-relative path to the resource.
     * @return <code>true</code> if the given resource is opened accoring to the
     *         editor pool.
     */
    public boolean isOpened(IPath path) {
        return this.editorPool.getEditors(path).size() > 0;
    }

    /**
     * Gives the editors of given path.
     * 
     * @param path
     *            the project-relative path to the resource.
     * @return the set of editors
     */
    public Set<IEditorPart> getEditors(IPath path) {
        return this.editorPool.getEditors(path);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.IActivityProvider
     */
    public IActivity fromXML(XmlPullParser parser) {

        try {
            if (parser.getName().equals("editor")) {
                return parseEditorActivity(parser);

            } else if (parser.getName().equals("edit")) {
                return parseTextEditActivity(parser);

            } else if (parser.getName().equals("textSelection")) {
                return parseTextSelection(parser);

            } else if (parser.getName().equals("viewport")) {
                return parseViewport(parser);
            }

        } catch (XmlPullParserException e) {
            EditorManager.log.error("Couldn't parse message");
        } catch (IOException e) {
            EditorManager.log.error("Couldn't parse message");
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.IActivityProvider
     */
    public String toXML(IActivity activity) {
        if (activity instanceof EditorActivity) {
            EditorActivity editorActivity = (EditorActivity) activity;

            // TODO What is that checksum?
            return "<editor " + "path=\"" + editorActivity.getPath() + "\" "
                + "type=\"" + editorActivity.getType() + "\" " + "checksum=\""
                + editorActivity.getChecksum() + "\"  />";

        } else if (activity instanceof TextEditActivity) {
            TextEditActivity textEditActivity = (TextEditActivity) activity;

            String result = String
                .format(
                    "<edit path=\"%s\" offset=\"%d\" source=\"%s\"><text>%s</text><replace>%s</replace></edit>",
                    textEditActivity.getEditor(), textEditActivity.offset,
                    textEditActivity.getSource(), Util
                        .escapeCDATA(textEditActivity.text), Util
                        .escapeCDATA(textEditActivity.replacedText));
            return result;

        } else if (activity instanceof TextSelectionActivity) {
            TextSelectionActivity textSelection = (TextSelectionActivity) activity;
            assert textSelection.getEditor() != null;
            return "<textSelection " + "offset=\"" + textSelection.getOffset()
                + "\" " + "length=\"" + textSelection.getLength() + "\" "
                + "editor=\"" + textSelection.getEditor().toPortableString()
                + "\" />";

        } else if (activity instanceof ViewportActivity) {
            ViewportActivity viewportActvity = (ViewportActivity) activity;

            // TODO This assertion has failed => Could not get editor?
            assert viewportActvity.getEditor() != null;

            return "<viewport " + "top=\"" + viewportActvity.getTopIndex()
                + "\" " + "bottom=\"" + viewportActvity.getBottomIndex()
                + "\" " + "editor=\""
                + viewportActvity.getEditor().toPortableString() + "\" />";
        }

        return null;
    }

    /**
     * This was an attempt to do some CDATA escaping
     * 
     * <code>
    String text = "";
    if (parser.next() == XmlPullParser.START_TAG) {
        if (parser.next() == XmlPullParser.TEXT) {
            text = parser.getText();
            parser.next(); // close tag
        }
    }

    String replace = "";
    if (parser.next() == XmlPullParser.START_TAG) {
        if (parser.next() == XmlPullParser.TEXT) {
            replace = parser.getText();
            parser.next(); // close tag
        }
    }
    </code>
     */
    private IActivity parseTextEditActivity(XmlPullParser parser)
        throws XmlPullParserException, IOException {

        // extract current editor for text edit.
        String pathString = parser.getAttributeValue(null, "path");

        assert pathString != null;

        Path path = new Path(pathString);
        int offset = Integer.parseInt(parser.getAttributeValue(null, "offset"));
        String source = parser.getAttributeValue(null, "source");
        String text = "";
        if (parser.next() == XmlPullParser.START_TAG) {
            if (parser.next() == XmlPullParser.TEXT) {
                text = parser.getText();
                parser.next(); // close tag
            }
        }

        String replace = "";
        if (parser.next() == XmlPullParser.START_TAG) {
            if (parser.next() == XmlPullParser.TEXT) {
                replace = parser.getText();
                parser.next(); // close tag
            }
        }
        return new TextEditActivity(offset, text, replace, path, source);
    }

    private IActivity parseEditorActivity(XmlPullParser parser) {
        String pathString = parser.getAttributeValue(null, "path");
        String checksumString = parser.getAttributeValue(null, "checksum");

        // TODO handle cases where the file is really named "null"
        Path path = pathString.equals("null") ? null : new Path(pathString);

        Type type = EditorActivity.Type.valueOf(parser.getAttributeValue(null,
            "type"));
        EditorActivity edit = new EditorActivity(type, path);
        try {
            long checksum = Long.parseLong(checksumString);
            edit.setChecksum(checksum);
        } catch (Exception e) {
            /* exception during parse process */
        }

        return edit;
    }

    private TextSelectionActivity parseTextSelection(XmlPullParser parser) {
        // TODO extract constants
        int offset = Integer.parseInt(parser.getAttributeValue(null, "offset"));
        int length = Integer.parseInt(parser.getAttributeValue(null, "length"));
        String path = parser.getAttributeValue(null, "editor");
        return new TextSelectionActivity(offset, length, Path
            .fromPortableString(path));
    }

    private ViewportActivity parseViewport(XmlPullParser parser) {
        int top = Integer.parseInt(parser.getAttributeValue(null, "top"));
        int bottom = Integer.parseInt(parser.getAttributeValue(null, "bottom"));
        String path = parser.getAttributeValue(null, "editor");
        return new ViewportActivity(top, bottom, Path.fromPortableString(path));
    }

    private boolean isSharedEditor(IEditorPart editorPart) {
        if (sharedProject == null)
            return false;

        IResource resource = this.editorAPI.getEditorResource(editorPart);

        if (resource == null)
            return false;

        return (resource.getProject() == this.sharedProject.getProject());
    }

    private void replaceText(IFile file, int offset, String replacedText,
        String text, String source) {
        FileEditorInput input = new FileEditorInput(file);
        IDocumentProvider provider = this.editorAPI.getDocumentProvider(input);

        try {
            if (!this.connectedFiles.contains(file)) {
                provider.connect(input);
                this.connectedFiles.add(file);
            }

            IDocument doc = provider.getDocument(input);

            // Check if the replaced text is really there.
            if (log.isDebugEnabled()) {

                String is;
                try {
                    is = doc.get(offset, replacedText.length());
                    if (!is.equals(replacedText)) {
                        log.error("replaceText should be '" + replacedText
                            + "' is '" + is + "'");
                    }
                } catch (BadLocationException e) {
                    // Ignore, because this is going to fail again just below
                }
            }
            // Try to replace
            try {
                doc.replace(offset, replacedText.length(), text);
            } catch (BadLocationException e) {
                log
                    .error(String
                        .format(
                            "Could not apply TextEdit at %d-%d of document with length %d.\nWas supposed to replace '%s' with '%s'.",
                            offset, offset + replacedText.length(), doc
                                .getLength(), replacedText, text));
                throw e;
            }
            EditorManager.this.lastRemoteEditTimes.put(file
                .getProjectRelativePath(), System.currentTimeMillis());

            IAnnotationModel model = provider.getAnnotationModel(input);
            contributionAnnotationManager.insertAnnotation(model, offset, text
                .length(), source);

            // Don't disconnect from provider yet, because otherwise the text
            // changes would be lost. We only disconnect when the document is
            // reset or saved.

        } catch (BadLocationException e) {
            // TODO If this happens a resend of the original text should be
            // initiated.
            log
                .error("Couldn't insert driver text because of bad location.",
                    e);
        } catch (CoreException e) {
            log.error("Couldn't insert driver text.", e);
        }
    }

    /**
     * TODO document what this does
     * 
     * @swt Needs to be called from a UI thread.
     */
    private void resetText(IFile file) {
        if (!file.exists()) {
            return;
        }

        FileEditorInput input = new FileEditorInput(file);
        IDocumentProvider provider = this.editorAPI.getDocumentProvider(input);

        if (this.connectedFiles.contains(file)) {
            provider.disconnect(input);
            this.connectedFiles.remove(file);
        }
    }

    /**
     * Programmatically saves the given editor.
     * 
     * Calling this method will trigger a call to all registered
     * SharedEditorListeners (independent of the success of this method).
     * 
     * Calling this method will NOT trigger a {@link EditorActivity} of type
     * Save to be sent to the other clients.
     * 
     * @param path
     *            the project relative path to the file that is supposed to be
     *            saved to disk.
     * 
     * @swt This method must be called from the SWT thread
     * 
     * @nonReentrant This method cannot be called twice at the same time.
     */
    public void saveText(IPath path) {

        IFile file = this.sharedProject.getProject().getFile(path);

        if (!file.exists()) {
            log.warn("Cannot save file that does not exist:" + path.toString());
            return;
        }

        for (ISharedEditorListener listener : this.editorListeners) {
            listener.driverEditorSaved(path, true);
        }

        FileEditorInput input = new FileEditorInput(file);

        IDocumentProvider provider = this.editorAPI.getDocumentProvider(input);

        if (!this.connectedFiles.contains(file)) {
            // Save not necessary, if we have no modified document
            // If this warning is printed we must suspect an
            // inconsistency...
            log.warn("Saving not necessary (not connected)!");
            return;
        }
        boolean wasReadonly = FileUtil.setReadOnly(file, false);

        IDocument doc = provider.getDocument(input);
        IAnnotationModel model = provider.getAnnotationModel(input);
        model.connect(doc);

        dirtyStateListener.enabled = false;
        try {
            provider.saveDocument(new NullProgressMonitor(), input, doc, true);
            log.debug("Saved document: " + path);
        } catch (CoreException e) {
            log.error("Failed to save document: " + path, e);
        }
        dirtyStateListener.enabled = true;

        model.disconnect(doc);
        provider.disconnect(input);
        this.connectedFiles.remove(file);

        // Reset readonly state
        if (wasReadonly)
            FileUtil.setReadOnly(file, true);
    }

    /**
     * Sends an activity for clients to save the editor of given path.
     * 
     * @param path
     *            the project relative path to the resource that the driver was
     *            editing.
     */
    protected void sendEditorActivitySaved(IPath path) {

        for (ISharedEditorListener listener : this.editorListeners) {
            listener.driverEditorSaved(path, false);
        }

        fireActivity(new EditorActivity(Type.Saved, path));
    }

    /**
     * Sends given activity to all registered activity listeners (most
     * importantly the ActivitySequencer).
     * 
     */
    private void fireActivity(IActivity activity) {
        for (IActivityListener listener : this.activityListeners) {
            listener.activityCreated(activity);
        }
    }

    // TODO CJ: review needed
    private void activateOpenEditors() {
        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {
                for (IEditorPart editorPart : EditorManager.this.editorAPI
                    .getOpenEditors()) {
                    partOpened(editorPart);
                }

                IEditorPart activeEditor = EditorManager.this.editorAPI
                    .getActiveEditor();
                if (activeEditor != null) {
                    sharedEditorActivated(activeEditor);
                }
            }
        });
    }

    private void sharedEditorActivated(IEditorPart editorPart) {
        if (!this.sharedProject.isDriver()) {
            return;
        }

        IResource resource = this.editorAPI.getEditorResource(editorPart);
        IPath editorPath = resource.getProjectRelativePath();
        setActiveDriverEditor(editorPath, false);

        ITextSelection selection = this.editorAPI.getSelection(editorPart);
        setDriverTextSelection(Saros.getDefault().getLocalUser(), selection);

        ILineRange viewport = this.editorAPI.getViewport(editorPart);
        if (viewport == null) {
            log.warn("Shared Editor does not have a Viewport: " + editorPart);
        } else {
            viewportChanged(editorPart, viewport);
        }
    }

    private void setAllEditorsToEditable() {
        for (IEditorPart editor : this.editorPool.getAllEditors()) {
            this.editorAPI.setEditable(editor, true);
        }
    }

    /**
     * Removes all annotations of a given type and for all users.
     * 
     * @param annotationType
     *            the annotation type that will be removed.
     */
    @SuppressWarnings("unchecked")
    private void removeAllAnnotations(String annotationType) {
        for (IEditorPart editor : this.editorPool.getAllEditors()) {
            IEditorInput input = editor.getEditorInput();
            IDocumentProvider provider = this.editorAPI
                .getDocumentProvider(input);
            IAnnotationModel model = provider.getAnnotationModel(input);

            if (model == null) {
                return;
            }

            ArrayList<Annotation> annotations = new ArrayList<Annotation>(128);
            for (Iterator<Annotation> it = model.getAnnotationIterator(); it
                .hasNext();) {
                Annotation annotation = it.next();
                // TODO Use the type (in the sense of class) here instead of
                // the string.
                if (annotation.getType().startsWith(annotationType)) {
                    annotations.add(annotation);
                }
            }

            removeAnnotations(model, annotations);
        }
    }

    private void removeAnnotations(IAnnotationModel model,
        ArrayList<Annotation> annotations) {

        if (model instanceof IAnnotationModelExtension) {
            IAnnotationModelExtension extension = (IAnnotationModelExtension) model;
            extension.replaceAnnotations(annotations
                .toArray(new Annotation[annotations.size()]), Collections
                .emptyMap());
        } else {
            // TODO maybe warn once!
            for (Annotation annotation : annotations) {
                model.removeAnnotation(annotation);
            }
        }
    }

    /**
     * Removes all annotations of given user and type.
     * 
     * @param forUserID
     *            the id of the user whos annotations will be removed, if null
     *            annotations of given type for all users are removed
     * @param typeAnnotation
     *            the type of the annotations to remove if null all annotations
     *            are removed
     */
    private void removeAllAnnotations(String forUserID, String typeAnnotation) {

        for (IEditorPart editor : editorPool.getAllEditors()) {
            IEditorInput input = editor.getEditorInput();
            IDocumentProvider provider = this.editorAPI
                .getDocumentProvider(input);
            IAnnotationModel model = provider.getAnnotationModel(input);

            if (model == null) {
                continue;
            }

            ArrayList<Annotation> annotations = new ArrayList<Annotation>(128);
            for (@SuppressWarnings("unchecked")
            Iterator<Annotation> it = model.getAnnotationIterator(); it
                .hasNext();) {
                Annotation annotation = it.next();
                String type = annotation.getType();

                if (typeAnnotation != null && !typeAnnotation.equals(type)) {
                    continue;
                }

                if (!(annotation instanceof SarosAnnotation))
                    continue;

                SarosAnnotation sarosAnnotation = (SarosAnnotation) annotation;
                if (forUserID == null
                    || sarosAnnotation.getSource().equals(forUserID)) {
                    annotations.add(annotation);
                }
            }
            removeAnnotations(model, annotations);
        }
    }

    private EditorManager() {
        setEditorAPI(new EditorAPI());
        if ((Saros.getDefault() != null)
            && (Saros.getDefault().getSessionManager() != null)) {
            Saros.getDefault().getSessionManager().addSessionListener(this);
        }
    }

    /**
     * Sets the currently active driver editor.
     * 
     * @param path
     *            the project-relative path to the resource that the editor is
     *            currently editting.
     * @param replicated
     *            <code>false</code> if this action originates on this client.
     *            <code>false</code> if it is an replication of an action from
     *            another participant of the shared project.
     */
    private void setActiveDriverEditor(IPath path, boolean replicated) {
        this.activeDriverEditor = path;
        this.driverEditors.add(path);

        for (ISharedEditorListener listener : this.editorListeners) {
            listener.activeDriverEditorChanged(this.activeDriverEditor,
                replicated);
        }

        if (replicated) {
            if (this.isFollowing) {
                Util.runSafeSWTSync(log, new Runnable() {
                    public void run() {
                        openDriverEditor();
                    }
                });
            }
        } else {
            fireActivity(new EditorActivity(Type.Activated, path));
        }
    }

    /**
     * Removes the given editor from the list of editors that the driver is
     * currently using.
     * 
     * @param path
     *            the path to the resource that the driver was editting.
     * @param replicated
     *            <code>false</code> if this action originates on this client.
     *            <code>true</code> if it is an replication of an action from
     *            another participant of the shared project.
     */
    private void removeDriverEditor(final IPath path, boolean replicated) {
        if (path.equals(this.activeDriverEditor)) {
            setActiveDriverEditor(null, replicated);
        }

        this.driverEditors.remove(path);

        for (ISharedEditorListener listener : this.editorListeners) {
            listener.driverEditorRemoved(path, replicated);
        }

        if (replicated) {
            Util.runSafeSWTSync(log, new Runnable() {
                public void run() {
                    IFile file = EditorManager.this.sharedProject.getProject()
                        .getFile(path);
                    resetText(file);

                    if (EditorManager.this.isFollowing) {
                        for (IEditorPart part : editorPool.getEditors(path)) {
                            editorAPI.closeEditor(part);
                        }
                    }
                }
            });
        } else {
            fireActivity(new EditorActivity(Type.Closed, path));
        }
    }

    /**
     * @param selection
     *            sets the current text selection that is used by the driver.
     */
    private void setDriverTextSelection(User user, ITextSelection selection) {
        if (user.isDriver()) {
            this.driverTextSelections.put(user, selection);
        }
    }

    /**
     * To get the java system time of the last local edit operation.
     * 
     * @param path
     *            the project relative path of the resource
     * @return System.currentTimeMillis() of last local edit or 0 if there was
     *         no edit.
     */
    public long getLastEditTime(IPath path) {
        if (!this.lastEditTimes.containsKey(path)) {
            log.warn("File has never been edited: " + path);
            return 0;
        }
        return this.lastEditTimes.get(path);
    }

    /**
     * To get the java system time of the last remote edit operation.
     * 
     * @param path
     *            the project relative path of the resource
     * @return java system time of last remote edit
     */
    public long getLastRemoteEditTime(IPath path) {
        if (!this.lastRemoteEditTimes.containsKey(path)) {
            log.warn("File has never been edited: " + path);
            return 0;
        }
        return this.lastRemoteEditTimes.get(path);
    }

    /**
     * to get the information whether the user is in following mode or not
     * 
     * @return <code>true</code> when in following mode, otherwise
     *         <code>false</code>
     */
    public boolean isFollowing() {
        return isFollowing;
    }

    /**
     * Return the Viewport for one of the EditorParts (it is undefined which)
     * associated with the given path or null if no Viewport has been found.
     */
    public ILineRange getCurrentViewport(IPath path) {

        // TODO We need to find a way to identify individual editor on the
        // client and not only paths.

        for (IEditorPart editorPart : getEditors(path)) {
            ILineRange viewport = editorAPI.getViewport(editorPart);
            if (viewport != null)
                return viewport;
        }
        return null;
    }
}
