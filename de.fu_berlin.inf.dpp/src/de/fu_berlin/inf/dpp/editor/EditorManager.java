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
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
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
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.DocumentProviderRegistry;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IActivityReceiver;
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
import de.fu_berlin.inf.dpp.optional.cdt.CDTFacade;
import de.fu_berlin.inf.dpp.optional.jdt.JDTFacade;
import de.fu_berlin.inf.dpp.project.IActivityListener;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.BalloonNotification;
import de.fu_berlin.inf.dpp.util.BlockingProgressMonitor;
import de.fu_berlin.inf.dpp.util.FileUtil;
import de.fu_berlin.inf.dpp.util.StackTrace;
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
 *         TODO CO Since it was forgotten to reset the DriverEditors after a
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

            log.debug("Dirty state reset for: " + file.toString());

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

        protected Map<IPath, HashSet<IEditorPart>> editorParts = new HashMap<IPath, HashSet<IEditorPart>>();

        public void add(IEditorPart editorPart) {

            IResource resource = EditorManager.this.editorAPI
                .getEditorResource(editorPart);
            if (resource == null) {
                log.warn("Resource not found: " + editorPart.getTitle());
                return;
            }

            IPath path = resource.getProjectRelativePath();
            if (path == null) {
                log.warn("Resource is a project or workspace: " + resource);
                return;
            }

            ITextViewer viewer = EditorAPI.getViewer(editorPart);
            if (viewer == null) {
                log.warn("This editor is not a ITextViewer: "
                    + editorPart.getTitle());
                return;
            }

            IEditorInput input = editorPart.getEditorInput();
            if (!(input instanceof IFileEditorInput)) {
                log.warn("This editor does not use IFiles as input");
                return;
            }

            editorAPI.addSharedEditorListener(editorPart);
            editorAPI.setEditable(editorPart, EditorManager.this.isDriver);

            IDocumentProvider documentProvider = getDocumentProvider(input);
            documentProvider.addElementStateListener(dirtyStateListener);

            IDocument document = getDocument(editorPart);

            IFile file = ((IFileEditorInput) input).getFile();
            connect(file);

            document.addDocumentListener(documentListener);
            getEditors(path).add(editorPart);
            lastEditTimes.put(path, System.currentTimeMillis());
            lastRemoteEditTimes.put(path, System.currentTimeMillis());
        }

        public void remove(IEditorPart editorPart) {
            IResource resource = EditorManager.this.editorAPI
                .getEditorResource(editorPart);

            if (resource == null) {
                log.warn("Resource not found: " + editorPart.getTitle());
                return;
            }

            IPath path = resource.getProjectRelativePath();

            if (path == null) {
                return;
            }

            getEditors(path).remove(editorPart);
        }

        public Set<IEditorPart> getEditors(IPath path) {

            if (!editorParts.containsKey(path)) {
                HashSet<IEditorPart> result = new HashSet<IEditorPart>();
                editorParts.put(path, result);
                return result;
            }
            return editorParts.get(path);
        }

        public Set<IEditorPart> getAllEditors() {
            Set<IEditorPart> result = new HashSet<IEditorPart>();

            for (Set<IEditorPart> parts : this.editorParts.values()) {
                result.addAll(parts);
            }
            return result;
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

    private final IActivityReceiver activityReceiver = new AbstractActivityReceiver() {
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
    };

    public static EditorManager getDefault() {
        if (EditorManager.instance == null) {
            EditorManager.instance = new EditorManager();
        }

        return EditorManager.instance;
    }

    public void connect(IFile file) {
        if (!connectedFiles.contains(file)) {
            FileEditorInput input = new FileEditorInput(file);
            IDocumentProvider documentProvider = getDocumentProvider(input);
            try {
                documentProvider.connect(input);
            } catch (CoreException e) {
                log.error("Error connecting to a document provider on file '"
                    + file.toString() + "':", e);
                e.printStackTrace();
            }
            connectedFiles.add(file);

            IDocument document = documentProvider.getDocument(input);

            // if line delimiters are not in unix style convert them
            if (document instanceof IDocumentExtension4) {

                if (!((IDocumentExtension4) document).getDefaultLineDelimiter()
                    .equals("\n")) {
                    // TODO fails if editorPart is not using a IFileEditorInput
                    convertLineDelimiters(file);
                }
                ((IDocumentExtension4) document).setInitialLineDelimiter("\n");
            } else {
                EditorManager.log
                    .error("Can't discover line delimiter of document");
            }
        }
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
    public void sessionStarted(ISharedProject project) {
        this.sharedProject = project;

        assert this.editorPool.editorParts.isEmpty() : "EditorPool was not correctly reset!";

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
            project);

        // TODO Review Why?
        activateOpenEditors();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISessionListener
     */
    public void sessionEnded(ISharedProject project) {
        assert this.sharedProject == project;
        setAllEditorsToEditable();
        removeAllAnnotations(new Predicate() {
            public boolean check(SarosAnnotation annotation) {
                return true;
            }
        });

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

        IDocument result = null;

        Set<IEditorPart> editors = getEditors(path);

        if (!editors.isEmpty()) {
            result = getDocument(editors.iterator().next());
        }

        // if result == null there is no editor with this resource open
        if (result == null) {

            IFile file = sharedProject.getProject().getFile(path);

            if (file == null) {
                log.error("No file in project for path " + path,
                    new StackTrace());
            } else {
                connect(file);
            }

            // get Document from FileBuffer
            result = getTextFileBuffer(file).getDocument();
        }
        return result;
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
            if (getDocument(editor) == document) {
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
                IDocumentProvider provider = getDocumentProvider(input);
                IAnnotationModel model = provider.getAnnotationModel(input);
                contributionAnnotationManager.splitAnnotation(model, offset);
            }
        } else {
            log.error("Can't get editor path");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener
     */
    public void roleChanged(User user, boolean replicated) {
        this.isDriver = this.sharedProject.isDriver();

        // TODO Review, because this is causing problems with FollowMode
        activateOpenEditors();

        // TODO Why remove all and not just the ones of user and only if user is
        // observer now?
        removeAllAnnotations(new Predicate() {
            public boolean check(SarosAnnotation annotation) {
                return annotation instanceof ContributionAnnotation;
            }
        });

        if (Saros.getDefault().getLocalUser().equals(user)) {

            // get the session view
            IViewPart view = Util
                .findView("de.fu_berlin.inf.dpp.ui.SessionView");

            if (isDriver) {
                removeAllAnnotations(new Predicate() {
                    public boolean check(SarosAnnotation annotation) {
                        return annotation instanceof ViewportAnnotation;
                    }
                });

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
                // User is not driver anymore, so remove the text selection.
                this.driverTextSelections.remove(user);

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
    public void userLeft(final JID user) {
        removeAllAnnotations(new Predicate() {
            public boolean check(SarosAnnotation annotation) {
                return annotation.getSource().equals(user.toString());
            }
        });
        driverTextSelections.remove(sharedProject.getParticipant(user));
    }

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

    public void setFollowing(boolean isFollowing) {
        // Driver can't be in follow mode.
        this.isFollowing = isFollowing && !this.isDriver;

        for (ISharedEditorListener editorListener : this.editorListeners) {
            editorListener.followModeChanged(this.isFollowing);
        }

        if (this.isFollowing)
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
     * @see IActivityProvider
     */
    public void exec(final IActivity activity) {
        activity.dispatch(activityReceiver);
    }

    private void execTextEdit(TextEditActivity textEdit) {

        String source = textEdit.getSource();
        User user = Saros.getDefault().getSessionManager().getSharedProject()
            .getParticipant(new JID(source));

        IPath path = textEdit.getEditor();
        IFile file = sharedProject.getProject().getFile(path);

        /*
         * Disable documentListener temporarily to avoid being notified of the
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
        // HACK 6 Why does this not work via partActivated? Causes duplicate
        // activate events
        sharedEditorActivated(editorPart);
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
                setFollowing(false);
                return;
            }

            // if the opened editor is not the followed one, leave following
            // mode
            IResource resource = this.editorAPI.getEditorResource(editorPart);

            if (resource == null) {
                log.warn("Resource not found: " + editorPart.getTitle());
                setFollowing(false);
                return;
            }

            IPath path = resource.getProjectRelativePath();

            if (!activeDriverEditor.equals(path)) {
                setFollowing(false);
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
            setFollowing(false);
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

    private IActivity parseTextEditActivity(XmlPullParser parser)
        throws XmlPullParserException, IOException {

        // extract current editor for text edit.
        String pathString = parser.getAttributeValue(null, "path");

        assert pathString != null;

        IPath path = Path.fromPortableString(Util.urlUnescape(pathString));
        int offset = Integer.parseInt(parser.getAttributeValue(null, "offset"));
        String source = Util.urlUnescape(parser.getAttributeValue(null,
            "source"));
        String sender = Util.urlUnescape(parser.getAttributeValue(null,
            "sender"));

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

        TextEditActivity result = new TextEditActivity(offset, text, replace,
            path, source);
        result.setSender(sender);
        return result;
    }

    private IActivity parseEditorActivity(XmlPullParser parser) {

        String pathString = parser.getAttributeValue(null, "path");

        IPath path;
        if (pathString == null) {
            path = null;
        } else {
            path = Path.fromPortableString(Util.urlUnescape(pathString));
        }

        Type type = EditorActivity.Type.valueOf(parser.getAttributeValue(null,
            "type"));
        return new EditorActivity(type, path);
    }

    private TextSelectionActivity parseTextSelection(XmlPullParser parser) {
        // TODO extract constants
        int offset = Integer.parseInt(parser.getAttributeValue(null, "offset"));
        int length = Integer.parseInt(parser.getAttributeValue(null, "length"));
        String path = Util
            .urlUnescape(parser.getAttributeValue(null, "editor"));
        return new TextSelectionActivity(offset, length, Path
            .fromPortableString(path));
    }

    private ViewportActivity parseViewport(XmlPullParser parser) {
        int top = Integer.parseInt(parser.getAttributeValue(null, "top"));
        int bottom = Integer.parseInt(parser.getAttributeValue(null, "bottom"));
        String path = Util
            .urlUnescape(parser.getAttributeValue(null, "editor"));
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
        IDocumentProvider provider = getDocumentProvider(input);

        connect(file);

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
                        offset, offset + replacedText.length(),
                        doc.getLength(), replacedText, text));
            return;
        }
        lastRemoteEditTimes.put(file.getProjectRelativePath(), System
            .currentTimeMillis());

        IAnnotationModel model = provider.getAnnotationModel(input);
        contributionAnnotationManager.insertAnnotation(model, offset, text
            .length(), source);

        // Don't disconnect from provider yet, because otherwise the text
        // changes would be lost. We only disconnect when the document is
        // reset or saved.
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
        IDocumentProvider provider = getDocumentProvider(input);

        if (this.connectedFiles.contains(file)) {
            provider.disconnect(input);
            this.connectedFiles.remove(file);
        }
    }

    /**
     * Programmatically saves the given editor.
     * 
     * Calling this method will trigger a call to all registered
     * SharedEditorListeners (independent of the success of this method) BEFORE
     * the file is actually saved.
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
            log.error(
                "Cannot save file that does not exist:" + path.toString(),
                new StackTrace());
            return;
        }

        for (ISharedEditorListener listener : this.editorListeners) {
            listener.driverEditorSaved(path, true);
        }

        FileEditorInput input = new FileEditorInput(file);

        IDocumentProvider provider = getDocumentProvider(input);

        if (!this.connectedFiles.contains(file)) {
            /*
             * Save not necessary, if we have no modified document If this
             * warning is printed we must suspect an inconsistency...
             * 
             * This can occur when the ConvertLineDelimitersOperation is run
             */
            log
                .warn("Saving not necessary (not connected): "
                    + file.toString());
            return;
        }
        boolean wasReadonly = FileUtil.setReadOnly(file, false);

        IDocument doc = provider.getDocument(input);
        IAnnotationModel model = provider.getAnnotationModel(input);
        model.connect(doc);

        dirtyStateListener.enabled = false;

        BlockingProgressMonitor monitor = new BlockingProgressMonitor();

        try {
            provider.saveDocument(monitor, input, doc, true);
            log.debug("Saved document: " + path);
        } catch (CoreException e) {
            log.error("Failed to save document: " + path, e);
        }

        // Wait for saving to be done
        try {
            if (!monitor.await(5)) {
                log.warn("Timeout expired on saving document: " + path);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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

    private interface Predicate {
        boolean check(SarosAnnotation annotation);
    }

    /**
     * Removes all annotations that fulfill given {@link Predicate}.
     * 
     * @param predicate
     */
    @SuppressWarnings("unchecked")
    private void removeAllAnnotations(Predicate predicate) {
        for (IEditorPart editor : this.editorPool.getAllEditors()) {
            IEditorInput input = editor.getEditorInput();
            IDocumentProvider provider = getDocumentProvider(input);
            IAnnotationModel model = provider.getAnnotationModel(input);

            if (model == null) {
                continue;
            }

            // Collect annotations.
            ArrayList<Annotation> annotations = new ArrayList<Annotation>(128);
            for (Iterator<Annotation> it = model.getAnnotationIterator(); it
                .hasNext();) {
                Annotation annotation = it.next();

                if (annotation instanceof SarosAnnotation) {
                    SarosAnnotation sarosAnnontation = (SarosAnnotation) annotation;
                    if (predicate.check(sarosAnnontation)) {
                        annotations.add(annotation);
                    }
                }
            }

            // Remove collected annotations.
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

    public static void convertLineDelimiters(IFile file) {

        EditorManager.log.debug("Converting line delimiters...");

        boolean makeReadable = false;

        ResourceAttributes resourceAttributes = file.getResourceAttributes();
        if (resourceAttributes.isReadOnly()) {
            resourceAttributes.setReadOnly(false);
            try {
                file.setResourceAttributes(resourceAttributes);
                makeReadable = true;
            } catch (CoreException e) {
                log.error(
                    "Error making file readable for delimiter conversion:", e);
            }
        }
        // Now run the conversion operation
        IPath[] paths = new IPath[] { file.getFullPath() };

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
            // FIXME #2671663: Converting Line Delimiters causes Save
            runner.execute(paths, convertOperation, new NullProgressMonitor());
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

    /**
     * Returns the TextFileBuffer associated with this project relative path OR
     * null if the path could not be traced to a Buffer.
     * 
     * @param docPath
     *            A project relative path
     * @return
     */
    protected ITextFileBuffer getTextFileBuffer(IResource resource) {

        IPath fullPath = resource.getFullPath();

        ITextFileBufferManager tfbm = FileBuffers.getTextFileBufferManager();

        ITextFileBuffer fileBuff = tfbm.getTextFileBuffer(fullPath,
            LocationKind.IFILE);
        if (fileBuff != null)
            return fileBuff;
        else {
            try {
                tfbm.connect(fullPath, LocationKind.IFILE,
                    new NullProgressMonitor());
            } catch (CoreException e) {
                log.error("Could not connect to file " + fullPath);
                return null;
            }
            return tfbm.getTextFileBuffer(fullPath, LocationKind.IFILE);
        }
    }

    public static IDocument getDocument(IEditorPart editorPart) {
        IEditorInput input = editorPart.getEditorInput();

        return getDocumentProvider(input).getDocument(input);
    }

    public static IDocumentProvider getDocumentProvider(IEditorInput input) {

        Object adapter = input.getAdapter(IFile.class);
        if (adapter != null) {
            IFile file = (IFile) adapter;

            String fileExtension = file.getFileExtension();

            if (fileExtension != null) {
                if (fileExtension.equals("java")) {
                    // TODO: Rather this dependency should be injected when the
                    // EditorAPI is created itself.
                    JDTFacade facade = Saros.getDefault().getContainer()
                        .getComponent(JDTFacade.class);

                    if (facade.isJDTAvailable()) {
                        return facade.getDocumentProvider();
                    }

                } else if (fileExtension.equals("c")
                    || fileExtension.equals("h") || fileExtension.equals("cpp")
                    || fileExtension.equals("cxx")
                    || fileExtension.equals("hxx")) {

                    // TODO: Rather this dependency should be injected when the
                    // EditorAPI is created itself.
                    CDTFacade facade = Saros.getDefault().getContainer()
                        .getComponent(CDTFacade.class);

                    if (facade.isCDTAvailable()) {
                        return facade.getDocumentProvider();
                    }
                }
            }
        }

        DocumentProviderRegistry registry = DocumentProviderRegistry
            .getDefault();
        return registry.getDocumentProvider(input);
    }
}
