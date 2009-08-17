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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.DocumentProviderRegistry;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;
import org.picocontainer.annotations.Nullable;

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
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.RemoteEditorManager.RemoteEditor;
import de.fu_berlin.inf.dpp.editor.RemoteEditorManager.RemoteEditorState;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.editor.annotations.SelectionAnnotation;
import de.fu_berlin.inf.dpp.editor.annotations.ViewportAnnotation;
import de.fu_berlin.inf.dpp.editor.internal.ContributionAnnotationManager;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.editor.internal.IEditorAPI;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.IActivityListener;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.synchronize.Blockable;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.util.BlockingProgressMonitor;
import de.fu_berlin.inf.dpp.util.Predicate;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Util;

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
 *         activities, dirty state management,...
 */
@Component(module = "core")
public class EditorManager implements IActivityProvider, Disposable {

    protected static Logger log = Logger.getLogger(EditorManager.class
        .getName());

    protected static Logger wpLog = Logger.getLogger(EditorManager.class
        .getName());

    protected static EditorManager instance;

    protected SharedEditorListenerDispatch editorListener = new SharedEditorListenerDispatch();

    protected IEditorAPI editorAPI;

    protected RemoteEditorManager remoteEditorManager;

    protected ISharedProject sharedProject;

    protected final List<IActivityListener> activityListeners = new LinkedList<IActivityListener>();

    /**
     * The user that is followed or <code>null</code> if no user is followed.
     */
    protected User userToFollow = null;

    protected boolean isDriver;

    protected final EditorPool editorPool = new EditorPool(this);

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

    protected HashMap<IPath, Long> lastEditTimes = new HashMap<IPath, Long>();

    protected HashMap<IPath, Long> lastRemoteEditTimes = new HashMap<IPath, Long>();

    protected ContributionAnnotationManager contributionAnnotationManager;

    protected final IActivityReceiver activityReceiver = new AbstractActivityReceiver() {
        @Override
        public boolean receive(EditorActivity editorActivity) {

            User sender = sharedProject.getUser(new JID(editorActivity
                .getSource()));

            if (editorActivity.getType().equals(Type.Activated)) {
                execActivated(sender, editorActivity.getPath());

            } else if (editorActivity.getType().equals(Type.Closed)) {
                execClosed(sender, editorActivity.getPath());

            } else if (editorActivity.getType().equals(Type.Saved)) {
                saveText(editorActivity.getPath());
            }
            return false;
        }

        @Override
        public boolean receive(TextEditActivity textEditActivity) {
            execTextEdit(textEditActivity);
            return false;
        }

        @Override
        public boolean receive(TextSelectionActivity textSelectionActivity) {
            execTextSelection(textSelectionActivity);
            return false;
        }

        @Override
        public boolean receive(ViewportActivity viewportActivity) {
            execViewport(viewportActivity);
            return false;
        }
    };

    protected Blockable stopManagerListener = new Blockable() {
        public void unblock() {
            lockAllEditors(false);
        }

        public void block() {
            lockAllEditors(true);
        }
    };

    protected ISharedProjectListener sharedProjectListener = new ISharedProjectListener() {

        public void roleChanged(final User user) {

            // Make sure we have the up-to-date facts about ourself
            isDriver = sharedProject.isDriver();

            // Lock / unlock editors
            if (user.isLocal()) {
                editorPool.setDriverEnabled(isDriver);
            }

            if (saros.getPreferenceStore().getBoolean(
                PreferenceConstants.FOLLOW_EXCLUSIVE_DRIVER)) {

                if (isDriver) {
                    // If I became a driver, then I stop following
                    if (user.isLocal() && userToFollow != null)
                        setFollowing(null);

                } else {
                    // If I am not yet following anybody, or following an
                    // observer
                    if (userToFollow == null || userToFollow.isObserver()) {
                        // If most recent change made somebody a driver
                        if (user.isDriver() && user.isRemote()) {
                            // start following...
                            setFollowing(user);
                        } else {
                            // Find another driver to follow...
                            boolean foundUser = false;
                            for (User aUser : sharedProject.getParticipants()) {
                                if (aUser.isDriver() && aUser.isRemote()) {
                                    setFollowing(aUser);
                                    foundUser = true;
                                    break;
                                }
                            }
                            // Did not find anybody, stopping to follow
                            if (!foundUser) {
                                if (userToFollow != null)
                                    setFollowing(null);
                            }
                        }
                    }
                }
            }

            // TODO [PERF] 1 Make this lazy triggered on activating a part?
            refreshAnnotations();
        }

        public void userJoined(User user) {

            // TODO The user should be able to ask us for this state

            // Let the new user know where we are
            List<User> recipient = Collections.singletonList(user);

            for (IPath path : getLocallyOpenEditors()) {
                sharedProject.sendActivity(recipient, new EditorActivity(saros
                    .getMyJID().toString(), Type.Activated, path));
            }

            if (locallyActiveEditor == null)
                return;

            sharedProject.sendActivity(recipient, new EditorActivity(saros
                .getMyJID().toString(), Type.Activated, locallyActiveEditor));

            if (localViewport != null) {
                sharedProject.sendActivity(recipient, new ViewportActivity(
                    saros.getMyJID().toString(), localViewport,
                    locallyActiveEditor));
            } else {
                log.warn("No viewport for locallyActivateEditor: "
                    + locallyActiveEditor);
            }

            if (localSelection != null) {
                int offset = localSelection.getOffset();
                int length = localSelection.getLength();

                sharedProject.sendActivity(recipient,
                    new TextSelectionActivity(saros.getMyJID().toString(),
                        offset, length, locallyActiveEditor));
            } else {
                log.warn("No select for locallyActivateEditor: "
                    + locallyActiveEditor);
            }
        }

        public void userLeft(final User user) {

            // If the user left which I am following, then stop following...
            if (user.equals(userToFollow))
                setFollowing(null);

            removeAllAnnotations(new Predicate<SarosAnnotation>() {
                public boolean evaluate(SarosAnnotation annotation) {
                    return annotation.getSource().equals(user);
                }
            });
            remoteEditorManager.removeUser(user);
        }
    };

    protected ISessionListener sessionListener = new AbstractSessionListener() {

        @Override
        public void sessionStarted(ISharedProject project) {
            sharedProject = project;

            assert editorPool.getAllEditors().size() == 0 : "EditorPool was not correctly reset!";

            isDriver = sharedProject.isDriver();
            sharedProject.addListener(sharedProjectListener);

            sharedProject.addActivityProvider(EditorManager.this);
            contributionAnnotationManager = new ContributionAnnotationManager(
                project);
            remoteEditorManager = new RemoteEditorManager(sharedProject);

            Util.runSafeSWTSync(log, new Runnable() {
                public void run() {

                    editorAPI.addEditorPartListener(EditorManager.this);

                    // Calling this method might cause openPart events
                    Set<IEditorPart> allOpenEditorParts = editorAPI
                        .getOpenEditors();

                    Set<IEditorPart> editorsOpenedByRestoring = editorPool
                        .getAllEditors();

                    for (IEditorPart editorPart : allOpenEditorParts) {
                        // Make sure that we open those editors twice
                        // (print a warning)
                        if (!editorsOpenedByRestoring.contains(editorPart))
                            partOpened(editorPart);
                    }

                    IEditorPart activeEditor = editorAPI.getActiveEditor();
                    if (activeEditor != null) {
                        partActivated(activeEditor);
                    }
                }
            });
        }

        @Override
        public void sessionEnded(ISharedProject project) {

            assert sharedProject == project;

            Util.runSafeSWTSync(log, new Runnable() {
                public void run() {

                    editorAPI.removeEditorPartListener(EditorManager.this);

                    /*
                     * First need to remove the annotations and then clear the
                     * editorPool
                     */
                    removeAllAnnotations(new Predicate<SarosAnnotation>() {
                        public boolean evaluate(SarosAnnotation annotation) {
                            return true;
                        }
                    });

                    editorPool.removeAllEditors();

                    sharedProject.removeListener(sharedProjectListener);
                    sharedProject.removeActivityProvider(EditorManager.this);
                    sharedProject = null;
                    lastEditTimes.clear();
                    lastRemoteEditTimes.clear();
                    contributionAnnotationManager.dispose();
                    contributionAnnotationManager = null;
                    remoteEditorManager = null;
                    locallyActiveEditor = null;
                    locallyOpenEditors.clear();
                }
            });
        }
    };

    @Inject
    protected FileReplacementInProgressObservable fileReplacementInProgressObservable;

    /**
     * @Inject
     */
    protected Saros saros;

    /**
     * @Inject
     */
    protected StopManager stopManager;

    public EditorManager(Saros saros, SessionManager sessionManager,
        StopManager stopManager) {

        wpLog.trace("EditorManager initialised");

        this.saros = saros;

        setEditorAPI(new EditorAPI(saros));
        sessionManager.addSessionListener(this.sessionListener);

        stopManager.addBlockable(stopManagerListener);
        this.stopManager = stopManager;
    }

    public boolean isConnected(IFile file) {
        return connectedFiles.contains(file);
    }

    /**
     * Tries to add the given {@link IFile} to a set of locally opened files.
     * The file gets connected to its {@link IDocumentProvider} (e.g.
     * CompilationUnitDocumentProvider for Java-Files) This Method also converts
     * the line delimiters of the document. Already connected files will not be
     * connected twice.
     * 
     */
    public void connect(IFile file) {

        // TODO Check that file exists...
        wpLog.trace("EditorManager.connect(" + file.getName() + ") invoked");

        if (!isConnected(file)) {
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

            wpLog.trace("EditorManager.connect: IDocument loaded");

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
    }

    public void addSharedEditorListener(ISharedEditorListener editorListener) {
        this.editorListener.add(editorListener);
    }

    public void removeSharedEditorListener(ISharedEditorListener editorListener) {
        this.editorListener.remove(editorListener);
    }

    /**
     * Returns the resource paths of editors that the local user is currently
     * using.
     * 
     * @return all paths (in project-relative format) of files that the local
     *         user is currently editing by using an editor. Never returns
     *         <code>null</code>. A empty set is returned if there are no
     *         currently opened editors.
     */
    public Set<IPath> getLocallyOpenEditors() {
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

        if (path == null)
            throw new IllegalArgumentException();

        IDocument result = null;

        Set<IEditorPart> editors = getEditors(path);

        if (!editors.isEmpty()) {
            result = getDocument(editors.iterator().next());
        }

        // if result == null there is no editor with this resource open
        if (result == null) {

            IFile file = sharedProject.getProject().getFile(path);
            if (file == null || !file.exists()) {
                log.error("No file in project for path " + path,
                    new StackTrace());
                return null;
            }

            connect(file);

            // get Document from FileBuffer
            result = EditorUtils.getTextFileBuffer(file).getDocument();
        }
        return result;
    }

    /**
     * Sets the local editor 'opened' and fires an {@link EditorActivity} of
     * type <code>Activated</code>.
     * 
     * @param path
     *            the project-relative path to the resource that the editor is
     *            currently editing or null if the local user has no editor
     *            open.
     */
    public void generateEditorActivated(@Nullable IPath path) {

        this.locallyActiveEditor = path;

        if (path != null)
            this.locallyOpenEditors.add(path);

        editorListener.activeEditorChanged(sharedProject.getLocalUser(), path);

        fireActivity(new EditorActivity(sharedProject.getLocalUser().getJID()
            .toString(), Type.Activated, path));
    }

    /**
     * Fires an update of the given viewport for the given {@link IEditorPart}
     * so that all remote parties know that the user is now positioned at the
     * given viewport in the given part.
     * 
     * A viewport activity not necessarily indicates that the given IEditorPart
     * is currently active. If it is (the given IEditorPart matches the
     * locallyActiveEditor) then the {@link #localViewport} is updated to
     * reflect this.
     * 
     * 
     * @param part
     *            The IEditorPart for which to generate a ViewportActivity.
     * 
     * @param viewport
     *            The ILineRange in the given part which represents the
     *            currently visible portion of the editor. (again visible does
     *            not mean that this editor is actually the active one)
     */
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

        fireActivity(new ViewportActivity(sharedProject.getLocalUser().getJID()
            .toString(), viewport, path));
    }

    /**
     * Fires an update of the given {@link ITextSelection} for the given
     * {@link IEditorPart} so that all remote parties know that the user
     * selected some text in the given part.
     * 
     * @param part
     *            The IEditorPart for which to generate a TextSelectionActivity
     * 
     * @param newSelection
     *            The ITextSelection in the given part which represents the
     *            currently selected text in editor.
     */
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

        fireActivity(new TextSelectionActivity(sharedProject.getLocalUser()
            .getJID().toString(), offset, length, path));
    }

    /**
     * This method is called from Eclipse (via the StoppableDocumentListener)
     * whenever the local user has changed some text in an editor.
     * 
     * @param offset
     *            The index into the given document where the text change
     *            started.
     * @param text
     *            The text that has been inserted (is "" if no text was inserted
     *            but just characters were removed)
     * @param replaceLength
     *            The number of characters which have been replaced by this edit
     *            (is 0 if no character has been removed)
     * @param document
     *            The document which was changed.
     */
    public void textAboutToBeChanged(int offset, String text,
        int replaceLength, IDocument document) {

        if (fileReplacementInProgressObservable.isReplacementInProgress())
            return;

        if (sharedProject == null) {
            log.error("Shared Project has ended, but text edits"
                + " are received from local user.");
            return;
        }

        if (!this.isDriver) {
            /**
             * TODO If we are not a driver, then receiving this event might
             * indicate that the user somehow achieved to change his document.
             * We should run a consistency check.
             * 
             * But watch out for changes because of a consistency check!
             */
            log.warn("Local user caused text changes as an observer");
            return;
        }

        IEditorPart changedEditor = null;

        // FIXME: This is potentially slow and definitely ugly
        // search editor which changed
        for (IEditorPart editor : editorPool.getAllEditors()) {
            if (ObjectUtils.equals(getDocument(editor), document)) {
                changedEditor = editor;
                break;
            }
        }

        if (changedEditor == null) {
            log.error("Could not find editor for changed document " + document);
            return;
        }

        IPath path = editorAPI.getEditorPath(changedEditor);
        if (path == null) {
            log.warn("Could not find path for editor "
                + changedEditor.getTitle());
            return;
        }

        String replacedText;
        try {
            replacedText = document.get(offset, replaceLength);
        } catch (BadLocationException e) {
            log.error("Offset and/or replace invalid", e);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < replaceLength; i++)
                sb.append("?");
            replacedText = sb.toString();
        }

        EditorManager.this.lastEditTimes.put(path, System.currentTimeMillis());

        fireActivity(new TextEditActivity(sharedProject.getLocalUser().getJID()
            .toString(), offset, text, replacedText, path));

        // inform all registered ISharedEditorListeners about this text edit
        editorListener.textEditRecieved(sharedProject.getLocalUser(), path,
            text, replacedText, offset);

        /*
         * TODO Investigate if this is really needed here
         */
        {
            IEditorInput input = changedEditor.getEditorInput();
            IDocumentProvider provider = getDocumentProvider(input);
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

        // First let the remoteEditorManager update itself based on the activity
        remoteEditorManager.exec(activity);

        activity.dispatch(activityReceiver);
    }

    protected void execTextEdit(TextEditActivity textEdit) {

        wpLog.trace("EditorManager.execTextEdit invoked");

        User user = sharedProject.getUser(new JID(textEdit.getSource()));

        IPath path = textEdit.getEditor();
        IFile file = sharedProject.getProject().getFile(path);

        if (!file.exists()) {
            log.error("TextEditActivity refers to file which"
                + " is not available locally: " + textEdit);
            // TODO A consistency check can be started here
            return;
        }

        /*
         * Disable documentListener temporarily to avoid being notified of the
         * change
         */
        documentListener.enabled = false;

        replaceText(file, textEdit.offset, textEdit.replacedText,
            textEdit.text, user);

        documentListener.enabled = true;

        /*
         * TODO Performance optimization in case of batch operation might make
         * sense
         */
        for (IEditorPart editorPart : editorPool.getEditors(path)) {
            editorAPI.setSelection(editorPart, new TextSelection(
                textEdit.offset + textEdit.text.length(), 0), user, user
                .equals(getFollowedUser()));
        }

        // inform all registered ISharedEditorListeners about this text edit
        editorListener.textEditRecieved(user, path, textEdit.text,
            textEdit.replacedText, textEdit.offset);
    }

    protected void execTextSelection(TextSelectionActivity selection) {

        wpLog.trace("EditorManager.execTextSelection invoked");

        IPath path = selection.getEditor();

        if (path == null) {
            EditorManager.log
                .error("Received text selection but have no driver editor");
            return;
        }

        TextSelection textSelection = new TextSelection(selection.getOffset(),
            selection.getLength());

        User user = sharedProject.getUser(new JID(selection.getSource()));

        Set<IEditorPart> editors = EditorManager.this.editorPool
            .getEditors(path);
        for (IEditorPart editorPart : editors) {
            this.editorAPI.setSelection(editorPart, textSelection, user, user
                .equals(getFollowedUser()));
        }
    }

    protected void execViewport(ViewportActivity viewport) {

        wpLog.trace("EditorManager.execViewport invoked");

        User user = sharedProject.getUser(new JID(viewport.getSource()));
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
                    user);
            if (following)
                this.editorAPI.reveal(editorPart, lineRange);
        }
    }

    protected void execActivated(User user, IPath path) {

        wpLog.trace("EditorManager.execActivated invoked");

        editorListener.activeEditorChanged(user, path);

        // Path null means this driver has no active editor any more
        if (user.equals(getFollowedUser()) && path != null) {
            editorAPI.openEditor(sharedProject.getProject().getFile(path));
        }
    }

    protected void execClosed(User user, IPath path) {

        wpLog.trace("EditorManager.execClosed invoked");

        editorListener.editorRemoved(user, path);

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

        wpLog.trace("EditorManager.partOpened invoked");

        if (!isSharedEditor(editorPart)) {
            return;
        }

        /*
         * If the resource is not accessible it might have been deleted without
         * the editor having been closed (for instance outside of Eclipse).
         * Others might be confused about if they receive this editor from us.
         */
        if (!editorAPI.getEditorResource(editorPart).isAccessible())
            return;

        this.editorPool.add(editorPart);

        refreshAnnotations(editorPart);

        // HACK 6 Why does this not work via partActivated? Causes duplicate
        // activate events
        partActivated(editorPart);
    }

    /**
     * Called when the local user activated an shared editor.
     * 
     * This can be called twice for a single IEditorPart, because it is called
     * from partActivated and from partBroughtToTop.
     * 
     * We do not filter duplicate events, because it would be bad to miss events
     * and is not too bad have duplicate one's. In particular we use IPath as an
     * identifier to the IEditorPart which might not work for multiple editors
     * based on the same file.
     */
    public void partActivated(IEditorPart editorPart) {

        wpLog.trace("EditorManager.partActiveted invoked");

        // First check for last editor being closed (which is a null editorPart)
        if (editorPart == null) {
            generateEditorActivated(null);
            return;
        }

        // Is the new editor part supported by Saros (and inside the project)
        // and the Resource accessible (we don't want to report stale files)?
        if (!isSharedEditor(editorPart)
            || !editorAPI.getEditorResource(editorPart).isAccessible()) {
            if (getFollowedUser() != null) {
                setFollowing(null);
            }
            generateEditorActivated(null);
            return;
        }

        /*
         * If the opened editor is not the active editor of the user being
         * followed, then leave follow mode
         */
        if (getFollowedUser() != null) {
            RemoteEditor activeEditor = remoteEditorManager.getEditorState(
                getFollowedUser()).getActiveEditor();

            if (activeEditor != null
                && !activeEditor.getPath().equals(
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
     * Called if the IEditorInput of the IEditorPart is now something different
     * than before! Probably when renaming.
     * 
     */
    public void partInputChanged(IEditorPart editor) {

        if (editorPool.isManaged(editor)) {

            IPath path = editorPool.remove(editor);

            if (path == null) {
                log.warn("Editor was managed but path could not be found: "
                    + editor);
            } else {
                fireActivity(new EditorActivity(sharedProject.getLocalUser()
                    .getJID().toString(), Type.Closed, path));
            }

            partActivated(editor);
        }
    }

    /**
     * Called if the local user closed a part
     */
    public void partClosed(IEditorPart editorPart) {

        wpLog.trace("EditorManager.partClosed invoked");

        if (!isSharedEditor(editorPart)) {
            return;
        }

        IPath path = editorAPI.getEditorPath(editorPart);

        // if closing the followed editor, leave follow mode
        if (getFollowedUser() != null) {
            RemoteEditor activeEditor = remoteEditorManager.getEditorState(
                getFollowedUser()).getActiveEditor();

            if (activeEditor != null && activeEditor.getPath().equals(path)) {
                setFollowing(null);
            }
        }

        this.editorPool.remove(editorPart);

        // Check if the currently active editor is closed
        boolean newActiveEditor = path.equals(this.locallyActiveEditor);

        this.locallyOpenEditors.remove(path);

        editorListener.editorRemoved(sharedProject.getLocalUser(), path);

        fireActivity(new EditorActivity(sharedProject.getLocalUser().getJID()
            .toString(), Type.Closed, path));

        /**
         * TODO We need a reliable way to communicate editors which are outside
         * the shared project scope and a way to deal with closing the active
         * editor
         */
        if (newActiveEditor) {
            partActivated(editorAPI.getActiveEditor());
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
    protected Set<IEditorPart> getEditors(IPath path) {
        return this.editorPool.getEditors(path);
    }

    /**
     * This method verifies if the given EditorPart is supported by Saros, which
     * is based basically on two facts:
     * 
     * 1.) Has a IResource belonging to the project
     * 
     * 2.) Can be mapped to a ITextViewer
     * 
     * Since a null editor does not support either, this method returns false.
     */
    protected boolean isSharedEditor(IEditorPart editorPart) {
        if (sharedProject == null)
            return false;

        if (EditorAPI.getViewer(editorPart) == null)
            return false;

        IResource resource = this.editorAPI.getEditorResource(editorPart);

        if (resource == null)
            return false;

        return ObjectUtils.equals(resource.getProject(), this.sharedProject
            .getProject());
    }

    /**
     * This method is called when a remote text edit has been received over the
     * network to apply the change to the local files.
     * 
     * @param file
     *            The file in which the change should be made.
     * 
     *            TODO We would like to be able to allow changing editors which
     *            are not driven by files someday, but it is not possible yet.
     * 
     * @param offset
     *            The position into the document of the given file, where the
     *            change started.
     * 
     * @param replacedText
     *            The text which is to be replaced by this operation at the
     *            given offset (is "" if this operation is only inserting text)
     * 
     * @param text
     *            The text which is to be inserted at the given offset instead
     *            of the replaced text (is "" if this operation is only deleting
     *            text)
     * @param source
     *            The User who caused this change.
     */
    protected void replaceText(IFile file, int offset, String replacedText,
        String text, User source) {

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
                    log.error("replaceText should be '"
                        + StringEscapeUtils.escapeJava(replacedText) + "' is '"
                        + StringEscapeUtils.escapeJava(is) + "'");
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
    protected void resetText(IFile file) {

        wpLog.trace("EditorManager.resetText(" + file.getName() + ") invoked.");
        if (!file.exists()) {
            log.warn("EditorManager.resetText :" + file.getName()
                + "does not exist. Exit.");
            return;
        }

        if (isConnected(file)) {
            FileEditorInput input = new FileEditorInput(file);
            IDocumentProvider provider = getDocumentProvider(input);
            provider.disconnect(input);
            this.connectedFiles.remove(file);
            wpLog.trace("EditorManager.resetText file " + file.getName()
                + " disconnencted.");
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
            Util.runSWTSync(new Callable<Object>() {
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

        return getDocumentProvider(input).canSaveDocument(input);
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

        wpLog.trace("EditorManager.saveText (" + file.getName() + ") invoked");

        if (!file.exists()) {
            log.error(
                "Cannot save file that does not exist:" + path.toString(),
                new StackTrace());
            return;
        }

        editorListener.driverEditorSaved(path, true);

        FileEditorInput input = new FileEditorInput(file);
        wpLog.trace("EditorManager.saveText EditorInput created ");

        IDocumentProvider provider = getDocumentProvider(input);
        wpLog.trace("EditorManager.saveText IDocumentProvider craeted ");

        if (isConnected(file)) {
            wpLog.trace("EditorManager.saveText File " + file.getName()
                + " is connected");
            if (provider.canSaveDocument(input)) {
                wpLog.trace("EditorManager.saveText File " + file.getName()
                    + " can be saved");
                // Everything okay! Provider and EditorManager agree
            } else {
                /*
                 * This happens when a file which is already saved is saved
                 * again by a remote user
                 */
                log.warn("File is not modified, but "
                    + "EditorManager thinks it is: " + file.toString());
            }
        } else {
            wpLog.trace("EditorManager.saveText File " + file.getName()
                + "is NOT connected");
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
                 * 
                 * FIXME after a role-change, observer (and host) executes two
                 * save-activities and comes in this warning
                 */
                log.warn("Saving not necessary (not connected): "
                    + file.toString(), new StackTrace());
                return;
            }
        }

        IDocument doc = provider.getDocument(input);

        IAnnotationModel model = provider.getAnnotationModel(input);
        model.connect(doc);

        wpLog.trace("EditorManager.saveText Annotations on the IDocument "
            + "are set");

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
            log.error("Code not designed to be interruptable", e);
            Thread.currentThread().interrupt();
        }

        if (monitor.isCanceled()) {
            log.warn("Saving was canceled by user: " + path);
        }

        dirtyStateListener.enabled = true;

        model.disconnect(doc);

        wpLog.trace("EditorManager.saveText File " + file.getName()
            + " will be reseted");
        /**
         * resetText leads to disconnecting the document from its provider.
         * disconnecting document of the driver (after resolving inconsitency)
         * makes saving impossible
         * 
         */
        if (!isDriver)
            resetText(file);

    }

    /**
     * Sends an activity for clients to save the editor of given path.
     * 
     * @param path
     *            the project relative path to the resource that the driver was
     *            editing.
     */
    public void sendEditorActivitySaved(IPath path) {

        wpLog.trace("EditorManager.sendEditorActivitySaved started for "
            + path.toString());

        // TODO technically we should mark the file as saved in the
        // editorPool, or?

        editorListener.driverEditorSaved(path, false);
        fireActivity(new EditorActivity(sharedProject.getLocalUser().getJID()
            .toString(), Type.Saved, path));
    }

    /**
     * Sends given activity to all registered activity listeners (most
     * importantly the ActivitySequencer).
     */
    protected void fireActivity(IActivity activity) {
        wpLog.trace("EditorManager.fireActivity invoked");

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
            removeAllAnnotations(editor, predicate);
        }
    }

    protected void removeAllAnnotations(IEditorPart editor,
        Predicate<SarosAnnotation> predicate) {
        IEditorInput input = editor.getEditorInput();
        IDocumentProvider provider = getDocumentProvider(input);
        IAnnotationModel model = provider.getAnnotationModel(input);

        if (model == null) {
            return;
        }

        // Collect annotations.
        ArrayList<Annotation> annotations = new ArrayList<Annotation>(128);
        for (@SuppressWarnings("unchecked")
        Iterator<Annotation> it = model.getAnnotationIterator(); it.hasNext();) {
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
     * Returns <code>true</code> if there is currently a {@link User} followed,
     * otherwise <code>false</code>.
     */
    public boolean isFollowing() {
        return getFollowedUser() != null;
    }

    /**
     * Returns the followed {@link User} or <code>null</code> if currently no
     * user is followed.
     */
    public User getFollowedUser() {
        return userToFollow;
    }

    /**
     * Sets the {@link User} to follow or <code>null</code> if no user should be
     * followed.
     */
    public void setFollowing(User userToFollow) {

        assert userToFollow == null
            || !userToFollow.equals(sharedProject.getLocalUser()) : "Local user cannot follow himself!";

        this.userToFollow = userToFollow;

        editorListener.followModeChanged(this.userToFollow);

        if (this.userToFollow != null)
            this.jumpToUser(this.userToFollow);
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

    public void refreshAnnotations() {
        for (IEditorPart part : editorPool.getAllEditors()) {
            refreshAnnotations(part);
        }
    }

    /**
     * Removes and then re-adds all viewport and selection annotations.
     * 
     * TODO This method does not deal with ContributionAnnotation.
     */
    public void refreshAnnotations(IEditorPart editorPart) {

        IPath path = editorAPI.getEditorPath(editorPart);
        if (path == null) {
            log.warn("Could not find path for editor " + editorPart.getTitle());
            return;
        }

        // Clear all annotations
        removeAllAnnotations(editorPart, new Predicate<SarosAnnotation>() {
            public boolean evaluate(SarosAnnotation annotation) {
                return annotation instanceof ViewportAnnotation
                    || annotation instanceof SelectionAnnotation;
            }
        });

        for (User user : sharedProject.getParticipants()) {

            if (user.isLocal()) {
                continue;
            }

            RemoteEditorState remoteEditorState = remoteEditorManager
                .getEditorState(user);
            if (!remoteEditorState.isRemoteOpenEditor(path)) {
                continue;
            }

            RemoteEditor remoteEditor = remoteEditorState.getRemoteEditor(path);

            if (user.isDriver() || user.equals(userToFollow)) {
                ILineRange viewport = remoteEditor.getViewport();
                if (viewport != null) {
                    editorAPI.setViewportAnnotation(editorPart, viewport, user);
                }
            }

            ITextSelection selection = remoteEditor.getSelection();
            if (selection != null) {
                editorAPI.setSelection(editorPart, selection, user, false);
            }
        }
    }

    public void jumpToUser(User jumpTo) {

        RemoteEditor activeEditor = remoteEditorManager.getEditorState(jumpTo)
            .getActiveEditor();

        if (activeEditor == null) {
            log.info("User [" + jumpTo + "] has no editor open");
            return;
        }

        IEditorPart newEditor = this.editorAPI.openEditor(this.sharedProject
            .getProject().getFile(activeEditor.getPath()));

        if (newEditor == null) {
            return;
        }

        ILineRange viewport = activeEditor.getViewport();

        if (viewport == null) {
            log.warn("User [" + jumpTo + "] has no viewport in editor: "
                + activeEditor.getPath());
            return;
        }

        this.editorAPI.reveal(newEditor, viewport);
    }

    /**
     * Returns a snap shot copy of the paths representing the editors that the
     * given user has currently opened (one of them being the active editor).
     * 
     * Returns an empty set if the user has no editors open.
     */
    public Set<IPath> getRemoteOpenEditors(User user) {
        return remoteEditorManager.getRemoteOpenEditors(user);
    }

    public List<User> getRemoteOpenEditorUsers(IPath path) {
        return remoteEditorManager.getRemoteOpenEditorUsers(path);
    }

    public List<User> getRemoteActiveEditorUsers(IPath path) {
        return remoteEditorManager.getRemoteActiveEditorUsers(path);
    }

    /**
     * Returns the set of paths representing the editors which are currently
     * opened by the remote users and the local user.
     * 
     * Returns an empty set if no editors are opened.
     */
    public Set<IPath> getOpenEditorsOfAllParticipants() {
        Set<IPath> result = remoteEditorManager.getRemoteOpenEditors();
        result.addAll(locallyOpenEditors);
        return result;
    }

    /**
     * TODO Review and document the purpose of this
     * 
     * Returns the {@link IDocumentProvider} of the given {@link IEditorInput}.
     * This method analyzes the file extension of the {@link IFile} associated
     * with the given {@link IEditorInput}. Depending on the file extension it
     * returns file-types responsible {@link IDocumentProvider}.
     * 
     * 
     * @param input
     *            the {@link IEditorInput} for which {@link IDocumentProvider}
     *            is needed
     * 
     * @return IDocumentProvider of the given input
     */
    public static IDocumentProvider getDocumentProvider(IEditorInput input) {
        return DocumentProviderRegistry.getDefault().getDocumentProvider(input);
    }

    public static IDocument getDocument(IEditorPart editorPart) {
        IEditorInput input = editorPart.getEditorInput();

        return getDocumentProvider(input).getDocument(input);
    }

    /**
     * Locks/unlocks all Editors for writing operations. Locked means local
     * keyboard inputs are not applied.
     * 
     * @param lock
     *            if true then editors are locked, otherwise they are unlocked
     */
    protected void lockAllEditors(boolean lock) {
        if (lock)
            log.debug("Lock all editors");
        else
            log.debug("Unlock all editors");
        editorPool.setDriverEnabled(!lock && sharedProject.isDriver());
    }

    public void dispose() {
        stopManager.removeBlockable(stopManagerListener);
    }
}
