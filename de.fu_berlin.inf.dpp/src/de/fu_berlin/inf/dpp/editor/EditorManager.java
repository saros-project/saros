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

import java.io.FileNotFoundException;
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
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
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
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import de.fu_berlin.inf.dpp.PreferenceConstants;
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
import de.fu_berlin.inf.dpp.editor.RemoteEditorManager.RemoteEditor;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.editor.annotations.ViewportAnnotation;
import de.fu_berlin.inf.dpp.editor.internal.ContributionAnnotationManager;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.editor.internal.IEditorAPI;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.IActivityListener;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.BalloonNotification;
import de.fu_berlin.inf.dpp.util.BlockingProgressMonitor;
import de.fu_berlin.inf.dpp.util.FileUtil;
import de.fu_berlin.inf.dpp.util.Predicate;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.ValueChangeListener;

/**
 * The EditorManager is responsible for handling all editors in a DPP-session.
 * This includes the functionality of listening for user inputs in an editor,
 * locking the editors of the observer.
 * 
 * The EditorManager contains the testable logic. All untestable logic should
 * only appear in an class of the {@link IEditorAPI} type. (CO: This is the
 * theory at least)
 * 
 * @author rdjemili
 * 
 *         TODO CO Since it was forgotten to reset the DriverEditors after a
 *         session closed, it is highly likely that this whole class needs to be
 *         reviewed for restarting issues
 * 
 *         TODO CO This class contains too many different concerns: TextEdits,
 *         Editor opening and closing, Parsing of activities, executing of
 *         activities, dirty state management,
 * 
 */
public class EditorManager implements IActivityProvider {

    protected class EditorPool {

        protected Map<IPath, HashSet<IEditorPart>> editorParts = new HashMap<IPath, HashSet<IEditorPart>>();

        public void add(IEditorPart editorPart) {

            IPath path = editorAPI.getEditorPath(editorPart);
            if (path == null) {
                log.warn("Could not find path for editor "
                    + editorPart.getTitle());
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
            editorAPI.setEditable(editorPart, isDriver);

            IDocumentProvider documentProvider = EditorUtils
                .getDocumentProvider(input);
            documentProvider.addElementStateListener(dirtyStateListener);

            IDocument document = EditorUtils.getDocument(editorPart);

            IFile file = ((IFileEditorInput) input).getFile();
            connect(file);

            document.addDocumentListener(documentListener);

            if (!getEditors(path).add(editorPart)) {
                log.warn("EditorPart was added twice to the EditorPool: "
                    + editorPart.getTitle());
            }
            lastEditTimes.put(path, System.currentTimeMillis());
            lastRemoteEditTimes.put(path, System.currentTimeMillis());
        }

        public void remove(IEditorPart editorPart) {
            IPath path = editorAPI.getEditorPath(editorPart);
            if (path == null) {
                log.warn("Could not find path for editor "
                    + editorPart.getTitle());
                return;
            }

            if (!getEditors(path).remove(editorPart)) {
                log.warn("EditorPart was never added to the EditorPool: "
                    + editorPart.getTitle());
                return;
            }

            // Unregister and unhook
            editorAPI.setEditable(editorPart, true);
            editorAPI.removeSharedEditorListener(editorPart);

            IEditorInput input = editorPart.getEditorInput();
            if (!(input instanceof IFileEditorInput)) {
                log.error("This editor does not use IFiles as input");
            }

            IDocumentProvider documentProvider = EditorUtils
                .getDocumentProvider(input);
            documentProvider.removeElementStateListener(dirtyStateListener);

            IFile file = ((IFileEditorInput) input).getFile();
            resetText(file);

            IDocument document = documentProvider.getDocument(input);
            document.removeDocumentListener(documentListener);

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

            for (IEditorPart part : new HashSet<IEditorPart>(getAllEditors())) {
                remove(part);
            }

            assert getAllEditors().size() == 0;
        }

        public void setDriverEnabled(boolean isDriver) {

            for (IEditorPart editorPart : getAllEditors()) {
                editorAPI.setEditable(editorPart, isDriver);
            }
        }
    }

    protected static Logger log = Logger.getLogger(EditorManager.class
        .getName());

    protected static EditorManager instance;

    protected IEditorAPI editorAPI;

    protected RemoteEditorManager remoteEditorManager;

    protected ISharedProject sharedProject;

    protected final List<IActivityListener> activityListeners = new LinkedList<IActivityListener>();

    protected User isFollowing = null;

    protected boolean isDriver;

    protected final EditorPool editorPool = new EditorPool();

    protected final DirtyStateListener dirtyStateListener = new DirtyStateListener(
        this);

    protected final StoppableDocumentListener documentListener = new StoppableDocumentListener(
        this);

    protected IPath locallyActiveEditor;

    protected Set<IPath> locallyOpenEditors = new HashSet<IPath>();

    protected ITextSelection localSelection;

    protected ILineRange localViewport;

    /** all files that have connected document providers */
    protected final Set<IFile> connectedFiles = new HashSet<IFile>();

    protected final List<ISharedEditorListener> editorListeners = new ArrayList<ISharedEditorListener>();

    protected HashMap<IPath, Long> lastEditTimes = new HashMap<IPath, Long>();

    protected HashMap<IPath, Long> lastRemoteEditTimes = new HashMap<IPath, Long>();

    protected ContributionAnnotationManager contributionAnnotationManager;

    protected final IActivityReceiver activityReceiver = new AbstractActivityReceiver() {
        @Override
        public boolean receive(EditorActivity editorActivity) {
            if (editorActivity.getType().equals(Type.Activated)) {
                execActivated(editorActivity.getUser(), editorActivity
                    .getPath());

            } else if (editorActivity.getType().equals(Type.Closed)) {
                execClosed(editorActivity.getUser(), editorActivity.getPath());

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

    protected ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {

        @Override
        public void roleChanged(final User user, boolean replicated) {

            User localUser = Saros.getDefault().getLocalUser();

            // Make sure we have the up-to-date facts about ourself
            isDriver = sharedProject.isDriver();

            // Lock / unlock editors
            if (user.equals(localUser)) {
                editorPool.setDriverEnabled(isDriver);
            }

            if (isFollowing != null) {
                if (Saros.getDefault().getPreferenceStore().getBoolean(
                    PreferenceConstants.FOLLOW_EXCLUSIVE_DRIVER)) {
                    if (isFollowing.isObserver() && user.isDriver()
                        && !user.equals(localUser)) {
                        setFollowing(user);
                    }
                }
            }

            // TODO Does it make sense to remove other Annotations on a role
            // change?
            if (user.isObserver() && !user.equals(isFollowing)) {
                removeAllAnnotations(new Predicate<SarosAnnotation>() {
                    public boolean evaluate(SarosAnnotation annotation) {
                        return annotation instanceof ViewportAnnotation
                            && user.getJID().equals(
                                new JID(((ViewportAnnotation) annotation)
                                    .getSource()));

                    }
                });
            }

            /*
             * If local user is affected and no session view is open then show
             * the balloon notification in the control which has the keyboard
             * focus
             */
            if (localUser.equals(user)) {

                IViewPart view = Util
                    .findView("de.fu_berlin.inf.dpp.ui.SessionView");

                if (view == null) {
                    // TODO This should not be necessary
                    Util.runSafeSWTAsync(log, new Runnable() {
                        public void run() {
                            BalloonNotification.showNotification(Display
                                .getDefault().getFocusControl(),
                                "Role changed", "You are now a "
                                    + (user.isDriver() ? "driver" : "observer")
                                    + " of this session.", 5000);
                        }
                    });
                }
            }

        }

        @Override
        public void userJoined(JID user) {

            // TODO This should only be sent to this user

            // TODO The user should be able to ask us for this state

            // TODO The user does not know our history but just our current
            // position

            // Let the new user know where we are
            fireActivity(new EditorActivity(Saros.getDefault().getMyJID()
                .toString(), Type.Activated, locallyActiveEditor));

            if (locallyActiveEditor == null) {
                return;
            }

            if (localViewport != null) {
                fireActivity(new ViewportActivity(Saros.getDefault().getMyJID()
                    .toString(), localViewport, locallyActiveEditor));
            } else {
                log.warn("No viewport for locallyActivateEditor: "
                    + locallyActiveEditor);
            }

            if (localSelection != null) {
                int offset = localSelection.getOffset();
                int length = localSelection.getLength();

                fireActivity(new TextSelectionActivity(Saros.getDefault()
                    .getMyJID().toString(), offset, length, locallyActiveEditor));
            } else {
                log.warn("No select for locallyActivateEditor: "
                    + locallyActiveEditor);
            }

        }

        @Override
        public void userLeft(final JID user) {
            removeAllAnnotations(new Predicate<SarosAnnotation>() {
                public boolean evaluate(SarosAnnotation annotation) {
                    return annotation.getSource().equals(user.toString());
                }
            });
            remoteEditorManager.removeUser(sharedProject.getParticipant(user));
        }
    };

    protected ISessionListener sessionListener = new AbstractSessionListener() {

        @Override
        public void sessionStarted(ISharedProject project) {
            sharedProject = project;

            assert editorPool.editorParts.isEmpty() : "EditorPool was not correctly reset!";

            isDriver = sharedProject.isDriver();
            sharedProject.addListener(sharedProjectListener);

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

            sharedProject.getActivityManager().addProvider(EditorManager.this);
            contributionAnnotationManager = new ContributionAnnotationManager(
                project);
            remoteEditorManager = new RemoteEditorManager();

            Util.runSafeSWTSync(log, new Runnable() {
                public void run() {
                    for (IEditorPart editorPart : EditorManager.this.editorAPI
                        .getOpenEditors()) {
                        partOpened(editorPart);
                    }

                    IEditorPart activeEditor = EditorManager.this.editorAPI
                        .getActiveEditor();
                    if (activeEditor != null) {
                        partActivated(activeEditor);
                    }
                }
            });
        }

        /*
         * (non-Javadoc)
         * 
         * @see de.fu_berlin.inf.dpp.project.ISessionListener
         */
        @Override
        public void sessionEnded(ISharedProject project) {
            assert sharedProject == project;

            editorPool.removeAllEditors();

            removeAllAnnotations(new Predicate<SarosAnnotation>() {
                public boolean evaluate(SarosAnnotation annotation) {
                    return true;
                }
            });

            sharedProject.removeListener(sharedProjectListener);
            sharedProject.getActivityManager().removeProvider(
                EditorManager.this);
            sharedProject = null;
            lastEditTimes.clear();
            lastRemoteEditTimes.clear();
            contributionAnnotationManager.dispose();
            contributionAnnotationManager = null;
            remoteEditorManager = null;
            locallyActiveEditor = null;
            locallyOpenEditors.clear();
        }
    };

    protected EditorManager() {
        setEditorAPI(new EditorAPI());
        if ((Saros.getDefault() != null)
            && (Saros.getDefault().getSessionManager() != null)) {
            Saros.getDefault().getSessionManager().addSessionListener(
                this.sessionListener);
        } else {
            log.error("EditorManager could not be started!", new StackTrace());
        }
    }

    public static EditorManager getDefault() {
        if (EditorManager.instance == null) {
            EditorManager.instance = new EditorManager();
        }

        return EditorManager.instance;
    }

    public void connect(IFile file) {
        if (!connectedFiles.contains(file)) {
            FileEditorInput input = new FileEditorInput(file);
            IDocumentProvider documentProvider = EditorUtils
                .getDocumentProvider(input);
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
                    EditorUtils.convertLineDelimiters(file);
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

    public void addSharedEditorListener(ISharedEditorListener editorListener) {
        if (!this.editorListeners.contains(editorListener)) {
            this.editorListeners.add(editorListener);
        }
    }

    public void removeSharedEditorListener(ISharedEditorListener editorListener) {
        this.editorListeners.remove(editorListener);
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
        return this.locallyOpenEditors;
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
            result = EditorUtils.getDocument(editors.iterator().next());
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
            result = EditorUtils.getTextFileBuffer(file).getDocument();
        }
        return result;
    }

    /**
     * Sets the currently active driver editor.
     * 
     * @param path
     *            the project-relative path to the resource that the editor is
     *            currently editting.
     */
    public void generateEditorActivated(IPath path) {
        this.locallyActiveEditor = path;
        this.locallyOpenEditors.add(path);

        for (ISharedEditorListener listener : this.editorListeners) {
            listener.activeEditorChanged(Saros.getDefault().getLocalUser(),
                this.locallyActiveEditor);
        }

        fireActivity(new EditorActivity(Saros.getDefault().getMyJID()
            .toString(), Type.Activated, path));
    }

    public void generateViewport(IEditorPart part, ILineRange viewport) {

        if (this.sharedProject == null) {
            log.warn("SharedEditorListener not correctly unregistered!");
            return;
        }

        IPath path = editorAPI.getEditorPath(part);
        if (path == null) {
            log.warn("Could not find path for editor " + part.getTitle());
            return;
        }

        if (path.equals(locallyActiveEditor))
            this.localViewport = viewport;

        fireActivity(new ViewportActivity(Saros.getDefault().getMyJID()
            .toString(), viewport, path));
    }

    public void generateSelection(IEditorPart part, ITextSelection newSelection) {

        IPath path = editorAPI.getEditorPath(part);
        if (path == null) {
            log.warn("Could not find path for editor " + part.getTitle());
            return;
        }

        if (path.equals(locallyActiveEditor))
            localSelection = newSelection;

        int offset = newSelection.getOffset();
        int length = newSelection.getLength();

        fireActivity(new TextSelectionActivity(Saros.getDefault().getMyJID()
            .toString(), offset, length, path));
    }

    /**
     * Asks the ConcurrentDocumentManager if there are currently any
     * inconsistencies to resolve.
     */
    public static boolean isConsistencyToResolve() {
        ISharedProject project = Saros.getDefault().getSessionManager()
            .getSharedProject();

        if (project == null)
            return false;

        return project.getConcurrentDocumentManager().getConsistencyToResolve()
            .getValue();
    }

    public void textAboutToBeChanged(int offset, String text, int replace,
        IDocument document) {

        /*
         * TODO When Inconsistencies exists, all listeners should be stopped
         * rather than catching events -> Think Start/Stop on the SharedProject
         */
        if (!this.isDriver || isConsistencyToResolve()) {

            /**
             * TODO If we are not a driver, then receiving this event might
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
            if (EditorUtils.getDocument(editor) == document) {
                changedEditor = editor;
                break;
            }
        }
        assert changedEditor != null;

        IPath path = editorAPI.getEditorPath(changedEditor);
        if (path == null) {
            log.warn("Could not find path for editor "
                + changedEditor.getTitle());
            return;
        }

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

        TextEditActivity activity = new TextEditActivity(Saros.getDefault()
            .getMyJID().toString(), offset, text, replacedText, path);

        EditorManager.this.lastEditTimes.put(path, System.currentTimeMillis());

        fireActivity(activity);

        /*
         * TODO Investigate if this is really needed here
         */
        {
            IEditorInput input = changedEditor.getEditorInput();
            IDocumentProvider provider = EditorUtils.getDocumentProvider(input);
            IAnnotationModel model = provider.getAnnotationModel(input);
            contributionAnnotationManager.splitAnnotation(model, offset);
        }
    }

    public void addActivityListener(IActivityListener listener) {
        this.activityListeners.add(listener);
    }

    public void removeActivityListener(IActivityListener listener) {
        this.activityListeners.remove(listener);
    }

    /**
     * @see IActivityProvider
     * 
     * @swt This must be called from the SWT thread.
     */
    public void exec(final IActivity activity) {

        assert Util.isSWT();

        remoteEditorManager.exec(activity);

        activity.dispatch(activityReceiver);
    }

    protected void execTextEdit(TextEditActivity textEdit) {

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
                textEdit.offset + textEdit.text.length(), 0), source, user
                .equals(getFollowedUser()));
        }
    }

    protected void execTextSelection(TextSelectionActivity selection) {
        IPath path = selection.getEditor();
        TextSelection textSelection = new TextSelection(selection.getOffset(),
            selection.getLength());

        User user = sharedProject
            .getParticipant(new JID(selection.getSource()));

        if (path == null) {
            EditorManager.log
                .error("Received text selection but have no driver editor");
            return;
        }

        Set<IEditorPart> editors = EditorManager.this.editorPool
            .getEditors(path);
        for (IEditorPart editorPart : editors) {
            this.editorAPI.setSelection(editorPart, textSelection, selection
                .getSource(), user.equals(getFollowedUser()));
        }
    }

    protected void execViewport(ViewportActivity viewport) {

        String source = viewport.getSource();
        User user = sharedProject.getParticipant(new JID(source));
        boolean following = user.equals(getFollowedUser());

        {
            /*
             * Check if source is an observed driver and his cursor is outside
             * the viewport.
             */
            ITextSelection driverSelection = remoteEditorManager
                .getSelection(user);
            /*
             * driverSelection can be null if viewport activity came before the
             * first text selection activity.
             */
            if (driverSelection != null) {
                /*
                 * TODO MR Taking the last line of the driver's last selection
                 * might be a bit inaccurate.
                 */
                int driverCursor = driverSelection.getEndLine();
                int top = viewport.getTopIndex();
                int bottom = viewport.getBottomIndex();
                following = following
                    && (driverCursor < top || driverCursor > bottom);
            }
        }

        Set<IEditorPart> editors = this.editorPool.getEditors(viewport
            .getEditor());
        ILineRange lineRange = viewport.getLineRange();
        for (IEditorPart editorPart : editors) {
            if (following || user.isDriver())
                this.editorAPI.setViewportAnnotation(editorPart, lineRange,
                    source);
            if (following)
                this.editorAPI.reveal(editorPart, lineRange);
        }
    }

    protected void execActivated(final User user, final IPath path) {

        for (ISharedEditorListener listener : editorListeners) {
            listener.activeEditorChanged(user, path);
        }

        // Path null means this driver has no active editor any more
        if (user.equals(getFollowedUser()) && path != null) {
            editorAPI.openEditor(sharedProject.getProject().getFile(path));
        }
    }

    protected void execClosed(final User user, final IPath path) {

        for (ISharedEditorListener listener : editorListeners) {
            listener.editorRemoved(user, path);
        }

        // TODO Review for disconnection of document providers.
        /*
         * IFile file = EditorManager.this.sharedProject.getProject()
         * .getFile(path); resetText(file);
         */

        if (user.equals(getFollowedUser())) {
            for (IEditorPart part : editorPool.getEditors(path)) {
                editorAPI.closeEditor(part);
            }
        }
    }

    /**
     * Called when the local user opened an editor part.
     */
    public void partOpened(IEditorPart editorPart) {

        if (!isSharedEditor(editorPart)) {
            return;
        }

        this.editorPool.add(editorPart);

        // HACK 6 Why does this not work via partActivated? Causes duplicate
        // activate events
        partActivated(editorPart);
    }

    /**
     * Called when the local user activated an shared editor
     */
    public void partActivated(IEditorPart editorPart) {

        // First check for last editor being closed (which a null editorPart)
        if (editorPart == null) {
            generateEditorActivated(null);
            return;
        }

        // Is the new editor part supported by Saros (and inside the project?)
        if (!isSharedEditor(editorPart)) {
            if (getFollowedUser() != null) {
                setFollowing(null);
            }
            return;
        }

        /*
         * If the opened editor is not the active editor of the user being
         * followed, then leave follow mode
         */
        if (getFollowedUser() != null) {
            RemoteEditor activeEditor = remoteEditorManager.getEditorState(
                getFollowedUser()).getActiveEditor();

            if (activeEditor == null
                || !activeEditor.getPath().equals(
                    editorAPI.getEditorPath(editorPart))) {
                setFollowing(null);
            }
        }

        IPath editorPath = this.editorAPI.getEditorPath(editorPart);
        ILineRange viewport = this.editorAPI.getViewport(editorPart);
        ITextSelection selection = this.editorAPI.getSelection(editorPart);

        // Set (and thus send) in this order:
        generateEditorActivated(editorPath);
        generateSelection(editorPart, selection);
        if (viewport == null) {
            log.warn("Shared Editor does not have a Viewport: " + editorPart);
        } else {
            generateViewport(editorPart, viewport);
        }
    }

    /**
     * Called if the local user closed a part
     */
    public void partClosed(IEditorPart editorPart) {

        if (!isSharedEditor(editorPart)) {
            return;
        }

        IPath path = editorAPI.getEditorPath(editorPart);

        // if closing the followed editor, leave follow mode
        if (getFollowedUser() != null) {
            RemoteEditor activeEditor = remoteEditorManager.getEditorState(
                getFollowedUser()).getActiveEditor();

            if (activeEditor == null || activeEditor.getPath().equals(path)) {
                setFollowing(null);
            }
        }

        this.editorPool.remove(editorPart);

        // Check if the currently active editor is closed
        boolean newActiveEditor = path.equals(this.locallyActiveEditor);

        this.locallyOpenEditors.remove(path);

        for (ISharedEditorListener listener : this.editorListeners) {
            listener.editorRemoved(Saros.getDefault().getLocalUser(), path);
        }

        fireActivity(new EditorActivity(Saros.getDefault().getMyJID()
            .toString(), Type.Closed, path));

        if (newActiveEditor)
            generateEditorActivated(editorAPI.getActiveEditorPath());
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
    protected Set<IEditorPart> getEditors(IPath path) {
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

    protected IActivity parseTextEditActivity(XmlPullParser parser)
        throws XmlPullParserException, IOException {

        String source = Util.urlUnescape(parser.getAttributeValue(null,
            "source"));

        // extract current editor for text edit.
        String pathString = parser.getAttributeValue(null, "path");
        assert pathString != null;

        IPath path = Path.fromPortableString(Util.urlUnescape(pathString));
        int offset = Integer.parseInt(parser.getAttributeValue(null, "offset"));

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

        TextEditActivity result = new TextEditActivity(source, offset, text,
            replace, path);
        result.setSender(sender);
        return result;
    }

    protected IActivity parseEditorActivity(XmlPullParser parser) {

        String source = Util.urlUnescape(parser.getAttributeValue(null,
            "source"));
        String pathString = parser.getAttributeValue(null, "path");

        IPath path;
        if (pathString == null) {
            path = null;
        } else {
            path = Path.fromPortableString(Util.urlUnescape(pathString));
        }

        Type type = EditorActivity.Type.valueOf(parser.getAttributeValue(null,
            "type"));
        return new EditorActivity(source, type, path);
    }

    protected TextSelectionActivity parseTextSelection(XmlPullParser parser) {

        String source = Util.urlUnescape(parser.getAttributeValue(null,
            "source"));
        int offset = Integer.parseInt(parser.getAttributeValue(null, "offset"));
        int length = Integer.parseInt(parser.getAttributeValue(null, "length"));
        String path = Util
            .urlUnescape(parser.getAttributeValue(null, "editor"));
        return new TextSelectionActivity(source, offset, length, Path
            .fromPortableString(path));
    }

    protected ViewportActivity parseViewport(XmlPullParser parser) {

        String source = Util.urlUnescape(parser.getAttributeValue(null,
            "source"));
        int top = Integer.parseInt(parser.getAttributeValue(null, "top"));
        int bottom = Integer.parseInt(parser.getAttributeValue(null, "bottom"));
        String path = Util
            .urlUnescape(parser.getAttributeValue(null, "editor"));
        return new ViewportActivity(source, top, bottom, Path
            .fromPortableString(path));
    }

    protected boolean isSharedEditor(IEditorPart editorPart) {
        if (sharedProject == null)
            return false;

        IResource resource = this.editorAPI.getEditorResource(editorPart);

        if (resource == null)
            return false;

        return (resource.getProject() == this.sharedProject.getProject());
    }

    protected void replaceText(IFile file, int offset, String replacedText,
        String text, String source) {

        FileEditorInput input = new FileEditorInput(file);
        IDocumentProvider provider = EditorUtils.getDocumentProvider(input);

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
     * TODO document what this does and start thinking about what really needs
     * to be done if a user resets (closes without saving) a file.
     * 
     * @swt Needs to be called from a UI thread.
     */
    private void resetText(IFile file) {
        if (!file.exists()) {
            return;
        }

        FileEditorInput input = new FileEditorInput(file);
        IDocumentProvider provider = EditorUtils.getDocumentProvider(input);

        if (this.connectedFiles.contains(file)) {
            provider.disconnect(input);
            this.connectedFiles.remove(file);
        }
    }

    /**
     * Save file denoted by the given project relative path if necessary
     * according to isDirty(IPath) and call saveText(IPath) if necessary in the
     * SWT thead.
     * 
     * @blocking This method returns after the file has been saved in the SWT
     *           Thread.
     * 
     * @nonSWT This method is not intended to be called from SWT (but it should
     *         be okay)
     */
    public void saveLazy(final IPath path) throws FileNotFoundException {
        if (!isDirty(path)) {
            return;
        }

        try {
            Util.runSWTSync(log, new Callable<Object>() {
                public Object call() throws Exception {
                    saveText(path);
                    return null;
                }
            });
        } catch (Exception e) {
            log.error("Unexpected exception: " + e);
        }
    }

    /**
     * Returns whether according to the DocumentProvider of this file, it has
     * been modified.
     * 
     * @throws FileNotFoundException
     *             if the file denoted by the path does not exist on disk.
     * 
     * 
     */
    public boolean isDirty(IPath path) throws FileNotFoundException {

        IFile file = this.sharedProject.getProject().getFile(path);

        if (file == null || !file.exists()) {
            throw new FileNotFoundException("File not found: " + path);
        }

        FileEditorInput input = new FileEditorInput(file);

        return EditorUtils.getDocumentProvider(input).canSaveDocument(input);
    }

    /**
     * Programmatically saves the given editor IF and only if the file is
     * registered as a connected file.
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

        IDocumentProvider provider = EditorUtils.getDocumentProvider(input);

        if (this.connectedFiles.contains(file)) {
            if (provider.canSaveDocument(input)) {
                // Everything okay! Provider and EditorManager agree
            } else {
                log.warn("File is not modified, but "
                    + "EditorManager thinks it is: " + file.toString());
            }
        } else {
            if (provider.canSaveDocument(input)) {
                log.warn("File is modified, but "
                    + "EditorManager does not know about it: "
                    + file.toString());

                connect(file);
            } else {
                /*
                 * Saving is not necessary if we have no modified document. If
                 * this warning is printed we must suspect an inconsistency...
                 * 
                 * This can occur when the ConvertLineDelimitersOperation is run
                 */
                log.warn("Saving not necessary (not connected): "
                    + file.toString(), new StackTrace());
                return;
            }
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
            if (!monitor.await(10)) {
                log.warn("Timeout expired on saving document: " + path);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (monitor.isCanceled()) {
            log.warn("Saving was canceled by user: " + path);
        }

        dirtyStateListener.enabled = true;

        model.disconnect(doc);

        resetText(file);

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
    public void sendEditorActivitySaved(IPath path) {
        for (ISharedEditorListener listener : this.editorListeners) {
            listener.driverEditorSaved(path, false);
        }

        // TODO technically we can should mark the file as saved in the
        // editorPool, or?

        fireActivity(new EditorActivity(Saros.getDefault().getMyJID()
            .toString(), Type.Saved, path));
    }

    /**
     * Sends given activity to all registered activity listeners (most
     * importantly the ActivitySequencer).
     */
    protected void fireActivity(IActivity activity) {
        for (IActivityListener listener : this.activityListeners) {
            listener.activityCreated(activity);
        }
    }

    /**
     * This is only used in the following implementation of removeAllAnnotations
     * so that an error is only printed once.
     */
    private boolean errorPrinted = false;

    /**
     * Removes all annotations that fulfill given {@link Predicate}.
     * 
     * @param predicate
     */
    protected void removeAllAnnotations(Predicate<SarosAnnotation> predicate) {
        for (IEditorPart editor : this.editorPool.getAllEditors()) {
            IEditorInput input = editor.getEditorInput();
            IDocumentProvider provider = EditorUtils.getDocumentProvider(input);
            IAnnotationModel model = provider.getAnnotationModel(input);

            if (model == null) {
                continue;
            }

            // Collect annotations.
            ArrayList<Annotation> annotations = new ArrayList<Annotation>(128);
            for (@SuppressWarnings("unchecked")
            Iterator<Annotation> it = model.getAnnotationIterator(); it
                .hasNext();) {
                Annotation annotation = it.next();

                if (annotation instanceof SarosAnnotation) {
                    SarosAnnotation sarosAnnontation = (SarosAnnotation) annotation;
                    if (predicate.evaluate(sarosAnnontation)) {
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
                if (!errorPrinted) {
                    log.error("AnnotationModel does not "
                        + "support IAnnoationModelExtension: " + model);
                    errorPrinted = true;
                }
                for (Annotation annotation : annotations) {
                    model.removeAnnotation(annotation);
                }
            }
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
    public User getFollowedUser() {
        return isFollowing;
    }

    public void setFollowing(User userToFollow) {

        assert !Saros.getDefault().getLocalUser().equals(userToFollow) : "Local user cannot follow himself!";

        this.isFollowing = userToFollow;

        for (ISharedEditorListener editorListener : this.editorListeners) {
            editorListener.followModeChanged(this.isFollowing);
        }

        if (this.isFollowing != null)
            this.jumpToUser(this.isFollowing);
    }

    /**
     * Return the Viewport for one of the EditorParts (it is undefined which)
     * associated with the given path or null if no Viewport has been found.
     */
    public ILineRange getCurrentViewport(IPath path) {

        // TODO We need to find a way to identify individual editors on the
        // client and not only paths.

        for (IEditorPart editorPart : getEditors(path)) {
            ILineRange viewport = editorAPI.getViewport(editorPart);
            if (viewport != null)
                return viewport;
        }
        return null;
    }

    public void jumpToUser(User jumpTo) {

        RemoteEditor activeEditor = remoteEditorManager.getEditorState(jumpTo)
            .getActiveEditor();

        if (activeEditor == null) {
            log.info("User[" + jumpTo + "] has no editor open");
            return;
        }

        IEditorPart newEditor = this.editorAPI.openEditor(this.sharedProject
            .getProject().getFile(activeEditor.getPath()));

        if (newEditor == null) {
            return;
        }

        ILineRange viewport = activeEditor.getViewport();

        if (viewport == null) {
            log.warn("User[" + jumpTo + "] has no viewport in editor: "
                + activeEditor.getPath());
            return;
        }

        this.editorAPI.reveal(newEditor, viewport);
    }

    public List<User> getRemoteOpenEditorUsers(IPath path) {
        return remoteEditorManager.getRemoteOpenEditorUsers(path);
    }

    public List<User> getRemoteActiveEditorUsers(IPath path) {
        return remoteEditorManager.getRemoteActiveEditorUsers(path);
    }
}
