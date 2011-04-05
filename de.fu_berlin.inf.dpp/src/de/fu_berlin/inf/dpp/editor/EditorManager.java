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
import java.util.Queue;
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
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
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
import org.eclipse.ui.texteditor.ITextEditor;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;
import org.picocontainer.annotations.Nullable;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.EditorActivity;
import de.fu_berlin.inf.dpp.activities.business.EditorActivity.Type;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.business.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.business.ViewportActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.RemoteEditorManager.RemoteEditor;
import de.fu_berlin.inf.dpp.editor.RemoteEditorManager.RemoteEditorState;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.editor.annotations.SelectionAnnotation;
import de.fu_berlin.inf.dpp.editor.annotations.ViewportAnnotation;
import de.fu_berlin.inf.dpp.editor.internal.ContributionAnnotationManager;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.editor.internal.IEditorAPI;
import de.fu_berlin.inf.dpp.editor.internal.RevertBufferListener;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.IActivityListener;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.synchronize.Blockable;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.util.BlockingProgressMonitor;
import de.fu_berlin.inf.dpp.util.Predicate;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * The EditorManager is responsible for handling all editors in a DPP-session.
 * This includes the functionality of listening for user inputs in an editor,
 * locking the editors of the users with {@link User.Permission#READONLY_ACCESS}
 * .
 * 
 * The EditorManager contains the testable logic. All untestable logic should
 * only appear in an class of the {@link IEditorAPI} type. (CO: This is the
 * theory at least)
 * 
 * @author rdjemili
 * 
 *         TODO CO Since it was forgotten to reset the Editors of users with
 *         {@link User.Permission#WRITE_ACCESS} after a session closed, it is
 *         highly likely that this whole class needs to be reviewed for
 *         restarting issues
 * 
 *         TODO CO This class contains too many different concerns: TextEdits,
 *         Editor opening and closing, Parsing of activityDataObjects, executing
 *         of activityDataObjects, dirty state management,...
 */
@Component(module = "core")
public class EditorManager implements IActivityProvider, Disposable {

    protected static Logger log = Logger.getLogger(EditorManager.class
        .getName());

    protected SharedEditorListenerDispatch editorListenerDispatch = new SharedEditorListenerDispatch();

    protected IEditorAPI editorAPI;

    protected RemoteEditorManager remoteEditorManager;

    protected RemoteWriteAccessManager remoteWriteAccessManager;

    protected ISarosSession sarosSession;

    protected final List<IActivityListener> activityListeners = new LinkedList<IActivityListener>();

    /**
     * The user that is followed or <code>null</code> if no user is followed.
     */
    protected User followedUser = null;

    protected boolean hasWriteAccess;

    protected final EditorPool editorPool = new EditorPool(this);

    protected final DirtyStateListener dirtyStateListener = new DirtyStateListener(
        this);

    protected final StoppableDocumentListener documentListener = new StoppableDocumentListener(
        this);

    protected SPath locallyActiveEditor;

    protected Set<SPath> locallyOpenEditors = new HashSet<SPath>();

    protected ITextSelection localSelection;

    protected ILineRange localViewport;

    /** all files that have connected document providers */
    protected final Set<IFile> connectedFiles = new HashSet<IFile>();

    protected HashMap<SPath, Long> lastEditTimes = new HashMap<SPath, Long>();

    protected HashMap<SPath, Long> lastRemoteEditTimes = new HashMap<SPath, Long>();

    protected ContributionAnnotationManager contributionAnnotationManager;

    protected Queue<IActivity> queuedActivities = new LinkedList<IActivity>();

    protected IActivityReceiver activityReceiver = new AbstractActivityReceiver() {
        @Override
        public void receive(EditorActivity editorActivity) {
            execEditorActivity(editorActivity);
        }

        @Override
        public void receive(TextEditActivity textEditActivity) {
            execTextEdit(textEditActivity);
        }

        @Override
        public void receive(TextSelectionActivity textSelectionActivity) {
            execTextSelection(textSelectionActivity);
        }

        @Override
        public void receive(ViewportActivity viewportActivity) {
            execViewport(viewportActivity);
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

    protected ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {

        @Override
        public void permissionChanged(final User user) {

            // Make sure we have the up-to-date facts about ourself
            hasWriteAccess = sarosSession.hasWriteAccess();

            // Lock / unlock editors
            if (user.isLocal()) {
                editorPool.setWriteAccessEnabled(hasWriteAccess);
            }

            // TODO [PERF] 1 Make this lazy triggered on activating a part?
            refreshAnnotations();
        }

        @Override
        public void userJoined(User user) {

            // TODO The user should be able to ask us for this state

            // Let the new user know where we are
            List<User> recipient = Collections.singletonList(user);

            User localUser = sarosSession.getLocalUser();
            for (SPath path : getLocallyOpenEditors()) {
                sarosSession.sendActivity(recipient, new EditorActivity(
                    localUser, Type.Activated, path));
            }

            if (locallyActiveEditor == null)
                return;

            sarosSession.sendActivity(recipient, new EditorActivity(localUser,
                Type.Activated, locallyActiveEditor));

            if (localViewport != null) {
                sarosSession.sendActivity(recipient, new ViewportActivity(
                    localUser, localViewport, locallyActiveEditor));
            } else {
                log.warn("No viewport for locallyActivateEditor: "
                    + locallyActiveEditor);
            }

            if (localSelection != null) {
                int offset = localSelection.getOffset();
                int length = localSelection.getLength();

                sarosSession.sendActivity(recipient, new TextSelectionActivity(
                    localUser, offset, length, locallyActiveEditor));
            } else {
                log.warn("No selection for locallyActivateEditor: "
                    + locallyActiveEditor);
            }
        }

        @Override
        public void userLeft(final User user) {

            // If the user left which I am following, then stop following...
            if (user.equals(followedUser))
                setFollowing(null);

            removeAllAnnotations(new Predicate<SarosAnnotation>() {
                public boolean evaluate(SarosAnnotation annotation) {
                    return annotation.getSource().equals(user);
                }
            });
            remoteEditorManager.removeUser(user);
        }
    };

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        protected RevertBufferListener buffListener;

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            sarosSession = newSarosSession;

            assert editorPool.getAllEditors().size() == 0 : "EditorPool was not correctly reset!";

            hasWriteAccess = sarosSession.hasWriteAccess();
            sarosSession.addListener(sharedProjectListener);

            sarosSession.addActivityProvider(EditorManager.this);
            contributionAnnotationManager = new ContributionAnnotationManager(
                newSarosSession);
            remoteEditorManager = new RemoteEditorManager(sarosSession);
            remoteWriteAccessManager = new RemoteWriteAccessManager(
                sarosSession);

            Utils.runSafeSWTSync(log, new Runnable() {
                public void run() {

                    editorAPI.addEditorPartListener(EditorManager.this);
                }
            });
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {

            assert sarosSession == oldSarosSession;

            Utils.runSafeSWTSync(log, new Runnable() {
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

                    editorPool.removeAllEditors(sarosSession);

                    dirtyStateListener.unregisterAll();

                    sarosSession.removeListener(sharedProjectListener);
                    sarosSession.removeActivityProvider(EditorManager.this);

                    if (buffListener != null)
                        buffListener.dispose();

                    sarosSession = null;
                    lastEditTimes.clear();
                    lastRemoteEditTimes.clear();
                    contributionAnnotationManager.dispose();
                    contributionAnnotationManager = null;
                    remoteEditorManager = null;
                    remoteWriteAccessManager.dispose();
                    remoteWriteAccessManager = null;
                    locallyActiveEditor = null;
                    locallyOpenEditors.clear();
                }
            });
        }

        @Override
        public void projectAdded(String projectID) {
            Utils.runSafeSWTSync(log, new Runnable() {

                /*
                 * When Alice invites Bob to a session with a project and Alice
                 * has some Files of the shared project already open, Bob will
                 * not receive any Actions (Selection, Contribution etc.) for
                 * the open editors. When Alice closes and reopens this Files
                 * again everything is going back to normal. To prevent that
                 * from happening this method is needed.
                 */
                public void run() {
                    // Calling this method might cause openPart events
                    Set<IEditorPart> allOpenEditorParts = EditorAPI
                        .getOpenEditors();

                    Set<IEditorPart> editorsOpenedByRestoring = editorPool
                        .getAllEditors();

                    // for every open file (editorPart) we act as if we just
                    // opened it
                    for (IEditorPart editorPart : allOpenEditorParts) {
                        // Make sure that we open those editors twice
                        // (print a warning)
                        log.debug(editorPart.getTitle());
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
    };

    protected ISharedEditorListener sharedEditorListener = new AbstractSharedEditorListener() {

        @Override
        public void activeEditorChanged(User user, SPath path) {

            // #2707089 We must clear annotations from shared editors that are
            // not commonly viewed

            // We only need to react to remote users changing editor
            if (user.isLocal())
                return;

            Predicate<SarosAnnotation> p = new Predicate<SarosAnnotation>() {
                public boolean evaluate(SarosAnnotation annotation) {
                    return annotation instanceof ViewportAnnotation;
                }
            };

            for (SPath localEditor : locallyOpenEditors) {
                if (!localEditor.equals(path)) {
                    for (IEditorPart match : editorPool.getEditors(localEditor)) {
                        removeAllAnnotations(match, p);
                    }
                }
            }
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

    public EditorManager(Saros saros, SarosSessionManager sessionManager,
        StopManager stopManager, EditorAPI editorApi) {

        log.trace("EditorManager initialized");

        this.saros = saros;

        editorAPI = editorApi;
        sessionManager.addSarosSessionListener(this.sessionListener);
        addSharedEditorListener(sharedEditorListener);

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

        if (!file.isAccessible()) {
            log.error(".connect File " + file.getName()
                + " could not be accessed");
            return;
        }

        log.trace(".connect(" + file.getProjectRelativePath().toOSString()
            + ") invoked");

        if (!isConnected(file)) {
            FileEditorInput input = new FileEditorInput(file);
            IDocumentProvider documentProvider = getDocumentProvider(input);
            try {
                documentProvider.connect(input);
            } catch (CoreException e) {
                log.error("Error connecting to a document provider on file '"
                    + file.toString() + "':", e);
            }
            connectedFiles.add(file);
        }
    }

    public void disconnect(IFile file) {

        log.trace(".disconnect(" + file.getProjectRelativePath().toOSString()
            + ") invoked");

        if (!isConnected(file)) {
            log.warn(".disconnect(): Trying to disconnect"
                + " DocProvider which is not connected: "
                + file.getFullPath().toOSString());
            return;
        }

        FileEditorInput input = new FileEditorInput(file);
        IDocumentProvider documentProvider = getDocumentProvider(input);
        documentProvider.disconnect(input);

        connectedFiles.remove(file);
    }

    public void addSharedEditorListener(ISharedEditorListener editorListener) {
        this.editorListenerDispatch.add(editorListener);
    }

    public void removeSharedEditorListener(ISharedEditorListener editorListener) {
        this.editorListenerDispatch.remove(editorListener);
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
    public Set<SPath> getLocallyOpenEditors() {
        return this.locallyOpenEditors;
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
    public void generateEditorActivated(@Nullable SPath path) {

        this.locallyActiveEditor = path;

        if (path != null)
            this.locallyOpenEditors.add(path);

        editorListenerDispatch.activeEditorChanged(sarosSession.getLocalUser(),
            path);

        fireActivity(new EditorActivity(sarosSession.getLocalUser(),
            Type.Activated, path));

    }

    /**
     * Fires an update of the given viewport for the given {@link IEditorPart}
     * so that all remote parties know that the user is now positioned at the
     * given viewport in the given part.
     * 
     * A viewport activityDataObject not necessarily indicates that the given
     * IEditorPart is currently active. If it is (the given IEditorPart matches
     * the locallyActiveEditor) then the {@link #localViewport} is updated to
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

        if (this.sarosSession == null) {
            log.warn("SharedEditorListener not correctly unregistered!");
            return;
        }

        SPath path = editorAPI.getEditorPath(part);
        if (path == null) {
            log.warn("Could not find path for editor " + part.getTitle());
            return;
        }

        if (path.equals(locallyActiveEditor))
            this.localViewport = viewport;

        fireActivity(new ViewportActivity(sarosSession.getLocalUser(),
            viewport, path));

        editorListenerDispatch.viewportGenerated(part, viewport, path);

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

        SPath path = editorAPI.getEditorPath(part);
        if (path == null) {
            log.warn("Could not find path for editor " + part.getTitle());
            return;
        }

        if (path.equals(locallyActiveEditor))
            localSelection = newSelection;

        int offset = newSelection.getOffset();
        int length = newSelection.getLength();

        fireActivity(new TextSelectionActivity(sarosSession.getLocalUser(),
            offset, length, path));
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

        if (sarosSession == null) {
            log.error("Shared Project has ended, but text edits"
                + " are received from local buddy.");
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

        SPath path = editorAPI.getEditorPath(changedEditor);
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

        TextEditActivity textEdit = new TextEditActivity(
            sarosSession.getLocalUser(), offset, text, replacedText, path);

        if (!this.hasWriteAccess) {
            /**
             * TODO If we don't have {@link User.Permission#WRITE_ACCESS}, then
             * receiving this event might indicate that the user somehow
             * achieved to change his document. We should run a consistency
             * check.
             * 
             * But watch out for changes because of a consistency check!
             */
            log.warn("Local buddy with read-only access caused text changes: "
                + textEdit);
            return;
        }

        EditorManager.this.lastEditTimes.put(path, System.currentTimeMillis());

        fireActivity(textEdit);

        // inform all registered ISharedEditorListeners about this text edit
        editorListenerDispatch.textEditRecieved(sarosSession.getLocalUser(),
            path, text, replacedText, offset);

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

        assert Utils.isSWT();

        User sender = activity.getSource();
        if (!sender.isInSarosSession()) {
            log.warn("Trying to execute activityDataObject for unknown buddy: "
                + activity);
            return;
        }

        // First let the remote managers update itself based on the
        // activityDataObject
        remoteEditorManager.exec(activity);
        remoteWriteAccessManager.exec(activity);

        activity.dispatch(activityReceiver);
    }

    protected void execEditorActivity(EditorActivity editorActivity) {
        User sender = editorActivity.getSource();
        SPath sPath = editorActivity.getPath();
        switch (editorActivity.getType()) {
        case Activated:
            execActivated(sender, sPath);
            break;
        case Closed:
            execClosed(sender, sPath);
            break;
        case Saved:
            saveText(sPath);
            break;
        default:
            log.warn("Unexpected type: " + editorActivity.getType());
        }
    }

    protected void execTextEdit(TextEditActivity textEdit) {

        log.trace("EditorManager.execTextEdit invoked");

        SPath path = textEdit.getPath();
        IFile file = path.getFile();

        if (!file.exists()) {
            log.error("TextEditActivity refers to file which"
                + " is not available locally: " + textEdit);
            // TODO A consistency check can be started here
            return;
        }

        User user = textEdit.getSource();

        /*
         * Disable documentListener temporarily to avoid being notified of the
         * change
         */
        documentListener.enabled = false;

        replaceText(path, textEdit.getOffset(), textEdit.getReplacedText(),
            textEdit.getText(), user);

        documentListener.enabled = true;

        /*
         * If the text edit ends in the visible region of a local editor, set
         * the cursor annotation.
         * 
         * TODO Performance optimization in case of batch operation might make
         * sense. Problem: How to recognize batch operations?
         */
        for (IEditorPart editorPart : editorPool.getEditors(path)) {
            ITextViewer viewer = EditorAPI.getViewer(editorPart);
            if (viewer == null) {
                // No text viewer for the editorPart found.
                continue;
            }
            int cursorOffset = textEdit.getOffset()
                + textEdit.getText().length();
            if (viewer.getTopIndexStartOffset() <= cursorOffset
                && cursorOffset <= viewer.getBottomIndexEndOffset()) {

                editorAPI.setSelection(editorPart, new TextSelection(
                    cursorOffset, 0), user, user.equals(getFollowedUser()));
            }
        }

        // inform all registered ISharedEditorListeners about this text edit
        editorListenerDispatch.textEditRecieved(user, path, textEdit.getText(),
            textEdit.getReplacedText(), textEdit.getOffset());
    }

    protected void execTextSelection(TextSelectionActivity selection) {

        log.trace("EditorManager.execTextSelection invoked");

        SPath path = selection.getPath();

        if (path == null) {
            EditorManager.log
                .error("Received text selection but have no writable editor");
            return;
        }

        TextSelection textSelection = new TextSelection(selection.getOffset(),
            selection.getLength());

        User user = selection.getSource();

        Set<IEditorPart> editors = EditorManager.this.editorPool
            .getEditors(path);
        for (IEditorPart editorPart : editors) {
            this.editorAPI.setSelection(editorPart, textSelection, user,
                user.equals(getFollowedUser()));
        }

        /*
         * inform all registered ISharedEditorListeners about a text selection
         * made
         */
        editorListenerDispatch.textSelectionMade(selection);
    }

    protected void execViewport(ViewportActivity viewport) {

        log.trace("EditorManager.execViewport invoked");

        User user = viewport.getSource();
        boolean following = user.equals(getFollowedUser());

        {
            /**
             * Check if source is an observed user with
             * {@link User.Permission#WRITE_ACCESS} and his cursor is outside
             * the viewport.
             */
            ITextSelection userWithWriteAccessSelection = remoteEditorManager
                .getSelection(user);
            /**
             * user with {@link User.Permission#WRITE_ACCESS} selection can be
             * null if viewport activityDataObject came before the first text
             * selection activityDataObject.
             */
            if (userWithWriteAccessSelection != null) {
                /**
                 * TODO MR Taking the last line of the last selection of the
                 * user with {@link User.Permission#WRITE_ACCESS} might be a bit
                 * inaccurate.
                 */
                int userWithWriteAccessCursor = userWithWriteAccessSelection
                    .getEndLine();
                int top = viewport.getTopIndex();
                int bottom = viewport.getBottomIndex();
                following = following
                    && (userWithWriteAccessCursor < top || userWithWriteAccessCursor > bottom);
            }
        }

        Set<IEditorPart> editors = this.editorPool.getEditors(viewport
            .getPath());
        ILineRange lineRange = viewport.getLineRange();
        for (IEditorPart editorPart : editors) {
            if (following || user.hasWriteAccess())
                this.editorAPI.setViewportAnnotation(editorPart, lineRange,
                    user);
            if (following)
                this.editorAPI.reveal(editorPart, lineRange);
        }
        /*
         * inform all registered ISharedEditorListeners about a change in
         * viewport
         */
        editorListenerDispatch.viewportChanged(viewport);

    }

    protected void execActivated(User user, SPath path) {

        log.trace("EditorManager.execActivated invoked");

        editorListenerDispatch.activeEditorChanged(user, path);

        /**
         * Path null means this user with {@link User.Permission#WRITE_ACCESS}
         * has no active editor any more
         */
        if (user.equals(getFollowedUser()) && path != null) {
            editorAPI.openEditor(path);
        }
    }

    protected void execClosed(User user, SPath path) {

        log.trace("EditorManager.execClosed invoked");

        editorListenerDispatch.editorRemoved(user, path);

        if (user.equals(getFollowedUser())) {
            for (IEditorPart part : editorPool.getEditors(path)) {
                editorAPI.closeEditor(part);
            }
        }
    }

    protected void execColorChanged() {
        editorListenerDispatch.colorChanged();
    }

    /**
     * Called when the local user opened an editor part.
     */
    public void partOpened(IEditorPart editorPart) {

        log.trace(".partOpened invoked");

        if (!isSharedEditor(editorPart)) {
            return;
        }

        /*
         * If the resource is not accessible it might have been deleted without
         * the editor having been closed (for instance outside of Eclipse).
         * Others might be confused about if they receive this editor from us.
         */
        IResource editorResource = editorAPI.getEditorResource(editorPart);
        if (!editorResource.isAccessible()) {
            log.warn(".partOpened resource: "
                + editorResource.getLocation().toOSString()
                + " is not accesible");
            return;
        }

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

        log.trace(".partActiveted invoked");

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

        SPath editorPath = this.editorAPI.getEditorPath(editorPart);
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
        // notice currently followed user before closing the editor
        User followedUser = getFollowedUser();

        if (editorPool.isManaged(editor)) {

            // Pretend as if the editor was closed locally (but use the old part
            // before the move happened) and then simulate it being opened again
            SPath path = editorPool.getCurrentPath(editor, sarosSession);
            if (path == null) {
                log.warn("Editor was managed but path could not be found: "
                    + editor);
            } else {
                partClosedOfPath(editor, path);
            }

            partOpened(editor);

            // restore the previously followed user
            // in case it was set and has changed
            if (followedUser != null && followedUser != getFollowedUser())
                setFollowing(followedUser);
        }
    }

    /**
     * Called if the local user closed a part
     */
    public void partClosed(IEditorPart editorPart) {

        log.trace("EditorManager.partClosed invoked");

        if (!isSharedEditor(editorPart)) {
            return;
        }

        SPath path = editorAPI.getEditorPath(editorPart);

        partClosedOfPath(editorPart, path);
    }

    protected void partClosedOfPath(IEditorPart editorPart, SPath path) {

        // if closing the followed editor, leave follow mode
        if (getFollowedUser() != null) {
            RemoteEditor activeEditor = remoteEditorManager.getEditorState(
                getFollowedUser()).getActiveEditor();

            if (activeEditor != null && activeEditor.getPath().equals(path)) {
                setFollowing(null);
            }
        }

        this.editorPool.remove(editorPart, sarosSession);

        // Check if the currently active editor is closed
        boolean newActiveEditor = path.equals(this.locallyActiveEditor);

        this.locallyOpenEditors.remove(path);

        editorListenerDispatch.editorRemoved(sarosSession.getLocalUser(), path);

        fireActivity(new EditorActivity(sarosSession.getLocalUser(),
            Type.Closed, path));

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
    public boolean isOpened(SPath path) {
        return this.editorPool.getEditors(path).size() > 0;
    }

    /**
     * Gives the editors of given path.
     * 
     * @param path
     *            the project-relative path to the resource.
     * @return the set of editors
     */
    protected Set<IEditorPart> getEditors(SPath path) {
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
        if (sarosSession == null)
            return false;

        if (EditorAPI.getViewer(editorPart) == null)
            return false;

        IResource resource = this.editorAPI.getEditorResource(editorPart);

        if (resource == null)
            return false;

        return this.sarosSession.isShared(resource.getProject());
    }

    /**
     * This method is called when a remote text edit has been received over the
     * network to apply the change to the local files.
     * 
     * @param path
     *            The path in which the change should be made.
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
    protected void replaceText(SPath path, int offset, String replacedText,
        String text, User source) {

        IFile file = path.getFile();
        FileEditorInput input = new FileEditorInput(file);
        IDocumentProvider provider = getDocumentProvider(input);

        try {
            provider.connect(input);
        } catch (CoreException e) {
            log.error(
                "Could not connect document provider for file: "
                    + file.toString(), e);
            // TODO Trigger a consistency recovery
            return;
        }

        try {
            IDocument doc = provider.getDocument(input);
            if (doc == null) {
                log.error("Could not connect document provider for file: "
                    + file.toString(), new StackTrace());
                // TODO Trigger a consistency recovery
                return;
            }

            // Check if the replaced text is really there.
            if (log.isDebugEnabled()) {

                String is;
                try {
                    is = doc.get(offset, replacedText.length());
                    if (!is.equals(replacedText)) {
                        log.error("replaceText should be '"
                            + StringEscapeUtils.escapeJava(replacedText)
                            + "' is '" + StringEscapeUtils.escapeJava(is) + "'");
                    }
                } catch (BadLocationException e) {
                    // Ignore, because this is going to fail again just below
                }
            }

            // Try to replace
            try {
                doc.replace(offset, replacedText.length(), text);
            } catch (BadLocationException e) {
                log.error(String.format(
                    "Could not apply TextEdit at %d-%d of document "
                        + "with length %d.\nWas supposed to replace"
                        + " '%s' with '%s'.", offset,
                    offset + replacedText.length(), doc.getLength(),
                    replacedText, text));
                return;
            }
            lastRemoteEditTimes.put(path, System.currentTimeMillis());

            for (IEditorPart editorPart : editorPool.getEditors(path)) {

                if (editorPart instanceof ITextEditor) {
                    ITextEditor textEditor = (ITextEditor) editorPart;
                    IAnnotationModel model = textEditor.getDocumentProvider()
                        .getAnnotationModel(textEditor.getEditorInput());
                    contributionAnnotationManager.insertAnnotation(model,
                        offset, text.length(), source);
                }
            }
            IAnnotationModel model = provider.getAnnotationModel(input);
            contributionAnnotationManager.insertAnnotation(model, offset,
                text.length(), source);
        } finally {
            provider.disconnect(input);
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
    public void saveLazy(final SPath path) throws FileNotFoundException {
        if (!isDirty(path)) {
            return;
        }

        try {
            Utils.runSWTSync(new Callable<Object>() {
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
    public boolean isDirty(SPath path) throws FileNotFoundException {

        IFile file = path.getFile();

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
    public void saveText(SPath path) {

        IFile file = path.getFile();

        log.trace("EditorManager.saveText (" + file.getName() + ") invoked");

        if (!file.exists()) {
            log.warn("File not found for saving: " + path.toString(),
                new StackTrace());
            return;
        }

        editorListenerDispatch.userWithWriteAccessEditorSaved(path, true);

        FileEditorInput input = new FileEditorInput(file);
        IDocumentProvider provider = getDocumentProvider(input);

        if (!provider.canSaveDocument(input)) {
            /*
             * This happens when a file which is already saved is saved again by
             * a buddy
             */
            log.debug(".saveText File " + file.getName()
                + " does not need to be saved");
            return;
        }

        log.trace(".saveText File " + file.getName() + " will be saved");

        try {
            provider.connect(input);
        } catch (CoreException e) {
            log.error("Could not connect to a document provider on file '"
                + file.toString() + "':", e);
            return;
        }

        try {
            IDocument doc = provider.getDocument(input);

            // TODO Why do we need to connect to the annotation model here?
            IAnnotationModel model = provider.getAnnotationModel(input);
            if (model != null)
                model.connect(doc);

            log.trace("EditorManager.saveText Annotations on the IDocument "
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
                log.warn("Saving was canceled by buddy: " + path);
            }

            dirtyStateListener.enabled = true;

            if (model != null)
                model.disconnect(doc);

            log.trace("EditorManager.saveText File " + file.getName()
                + " will be reseted");

        } finally {
            provider.disconnect(input);
        }
    }

    /**
     * Sends an activityDataObject for clients to save the editor of given path.
     * 
     * @param path
     *            the project relative path to the resource that the user with
     *            {@link User.Permission#WRITE_ACCESS} was editing.
     */
    public void sendEditorActivitySaved(SPath path) {

        log.trace("EditorManager.sendEditorActivitySaved started for "
            + path.toString());

        // TODO technically we should mark the file as saved in the
        // editorPool, or?
        // What is the reason of this?

        editorListenerDispatch.userWithWriteAccessEditorSaved(path, false);
        fireActivity(new EditorActivity(sarosSession.getLocalUser(),
            Type.Saved, path));
    }

    /**
     * Sends given activityDataObject to all registered activityDataObject
     * listeners (most importantly the ActivitySequencer).
     */
    protected void fireActivity(IActivity activityDataObject) {

        for (IActivityListener listener : this.activityListeners) {
            listener.activityCreated(activityDataObject);
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
            extension.replaceAnnotations(
                annotations.toArray(new Annotation[annotations.size()]),
                Collections.emptyMap());
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
        return followedUser;
    }

    /**
     * Sets the {@link User} to follow or <code>null</code> if no user should be
     * followed.
     */
    public void setFollowing(User newFollowedUser) {
        assert newFollowedUser == null
            || !newFollowedUser.equals(sarosSession.getLocalUser()) : "Local buddy cannot follow himself!";

        User oldFollowedUser = this.followedUser;
        this.followedUser = newFollowedUser;

        if (oldFollowedUser != null && !oldFollowedUser.equals(newFollowedUser)) {
            editorListenerDispatch.followModeChanged(oldFollowedUser, false);
        }

        if (newFollowedUser != null) {
            editorListenerDispatch.followModeChanged(newFollowedUser, true);
            this.jumpToUser(newFollowedUser);
        }
    }

    /**
     * Return the Viewport for one of the EditorParts (it is undefined which)
     * associated with the given path or null if no Viewport has been found.
     */
    public ILineRange getCurrentViewport(SPath path) {

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

        SPath path = editorAPI.getEditorPath(editorPart);
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

        for (User user : sarosSession.getParticipants()) {

            if (user.isLocal()) {
                continue;
            }

            RemoteEditorState remoteEditorState = remoteEditorManager
                .getEditorState(user);
            if (!(remoteEditorState.isRemoteOpenEditor(path) && remoteEditorState
                .isRemoteActiveEditor(path))) {
                continue;
            }

            RemoteEditor remoteEditor = remoteEditorState.getRemoteEditor(path);

            if (user.hasWriteAccess() || user.equals(followedUser)) {
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
            log.info(Utils.prefix(jumpTo.getJID()) + "has no editor open");
            return;
        }

        IEditorPart newEditor = this.editorAPI.openEditor(activeEditor
            .getPath());

        if (newEditor == null) {
            return;
        }

        ILineRange viewport = activeEditor.getViewport();

        if (viewport == null) {
            log.warn(Utils.prefix(jumpTo.getJID())
                + "has no viewport in editor: " + activeEditor.getPath());
            return;
        }

        this.editorAPI.reveal(newEditor, viewport);

        /*
         * inform all registered ISharedEditorListeners about this jump
         * performed
         */
        editorListenerDispatch.jumpedToUser(jumpTo);
    }

    /**
     * Returns a snap shot copy of the paths representing the editors that the
     * given user has currently opened (one of them being the active editor).
     * 
     * Returns an empty set if the user has no editors open.
     */
    public Set<SPath> getRemoteOpenEditors(User user) {
        return remoteEditorManager.getRemoteOpenEditors(user);
    }

    public List<User> getRemoteOpenEditorUsers(SPath path) {
        return remoteEditorManager.getRemoteOpenEditorUsers(path);
    }

    public List<User> getRemoteActiveEditorUsers(SPath path) {
        return remoteEditorManager.getRemoteActiveEditorUsers(path);
    }

    /**
     * Returns the set of paths representing the editors which are currently
     * opened by the buddies and the local user.
     * 
     * Returns an empty set if no editors are opened.
     */
    public Set<SPath> getOpenEditorsOfAllParticipants() {
        Set<SPath> result = remoteEditorManager.getRemoteOpenEditors();
        result.addAll(locallyOpenEditors);
        return result;
    }

    /**
     * Returns the {@link IDocumentProvider} of the given {@link IEditorInput}.
     * This method analyzes the file extension of the {@link IFile} associated
     * with the given {@link IEditorInput}. Depending on the file extension it
     * returns file-types responsible {@link IDocumentProvider}.
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
        editorPool
            .setWriteAccessEnabled(!lock && sarosSession.hasWriteAccess());
    }

    public void dispose() {
        stopManager.removeBlockable(stopManagerListener);
    }

    /**
     * Triggers a "Document Revert" by other session participants. This happens
     * when a user with {@link User.Permission#WRITE_ACCESS} rejected changes in
     * file buffer (e.g. "Close editor"+ "Do NOT save"). This method gets
     * invoked, only when the user is a user with
     * {@link User.Permission#WRITE_ACCESS}. It sends to other session
     * participants two activityDataObjects which revert their documents:
     * <ul>
     * <li>TextEdit - replaces content of the document with new (reverted)
     * content</li>
     * <li>EditorAcitvity(Type.Saved) - saves the reverted document</li>
     * </ul>
     * 
     * @param path
     *            path of the document which was reverted
     * @param newContent
     *            new content of the document (after it was reverted)
     * @param oldContent
     *            old content of the document (before reverting)
     */
    public void triggerRevert(SPath path, String newContent, String oldContent) {

        log.trace(".triggerRevert invoked");
        log.trace(".triggerRevert File: " + path.toString());

        // TODO Check if we are in the shared project!!

        User localUser = sarosSession.getLocalUser();
        int offset = 0;

        /**
         * TODO We should warn the user if he is reverting changes by other
         * users with {@link User.Permission#WRITE_ACCESS}
         * 
         * TODO If the UndoManager knows which changes were ours, we could
         * revert just those
         */
        IActivity textEditActivity = new TextEditActivity(localUser, offset,
            newContent, oldContent, path);

        IActivity saveActivity = new EditorActivity(localUser, Type.Saved, path);

        fireActivity(textEditActivity);
        fireActivity(saveActivity);

    }

    public Set<SPath> getRemoteOpenEditors() {
        return remoteEditorManager.getRemoteOpenEditors();
    }

    public void colorChanged() {
        this.execColorChanged();
    }

}
