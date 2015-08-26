/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.EditorActivity.Type;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.ViewportActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.RemoteEditorManager.RemoteEditor;
import de.fu_berlin.inf.dpp.editor.RemoteEditorManager.RemoteEditorState;
import de.fu_berlin.inf.dpp.editor.annotations.ContributionAnnotation;
import de.fu_berlin.inf.dpp.editor.annotations.RemoteCursorAnnotation;
import de.fu_berlin.inf.dpp.editor.annotations.RemoteCursorStrategy;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.editor.annotations.SelectionFillUpAnnotation;
import de.fu_berlin.inf.dpp.editor.annotations.SelectionFillUpStrategy;
import de.fu_berlin.inf.dpp.editor.internal.AnnotationModelHelper;
import de.fu_berlin.inf.dpp.editor.internal.ContributionAnnotationManager;
import de.fu_berlin.inf.dpp.editor.internal.CustomAnnotationManager;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.editor.internal.IEditorAPI;
import de.fu_berlin.inf.dpp.editor.internal.LocationAnnotationManager;
import de.fu_berlin.inf.dpp.editor.internal.SafePartListener2;
import de.fu_berlin.inf.dpp.filesystem.EclipseFileImpl;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.AbstractSessionListener;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.ISessionListener;
import de.fu_berlin.inf.dpp.session.NullSessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.User.Permission;
import de.fu_berlin.inf.dpp.synchronize.Blockable;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.util.Predicate;
import de.fu_berlin.inf.dpp.util.StackTrace;

/**
 * Eclipse implementation of the {@link IEditorManager} interface.
 * <p>
 * The EditorManager contains the testable logic. All untestable logic should
 * only appear in a class of the {@link IEditorAPI} type. (CO: This is the
 * theory at least)
 * 
 * @author rdjemili
 * 
 *         TODO CO Since it was forgotten to reset the Editors of users with
 *         {@link Permission#WRITE_ACCESS} after a session closed, it is highly
 *         likely that this whole class needs to be reviewed for restarting
 *         issues
 * 
 *         TODO CO This class contains too many different concerns: TextEdits,
 *         Editor opening and closing, Parsing of activities, executing of
 *         activities, dirty state management,...
 */
@Component(module = "core")
public class EditorManager extends AbstractActivityProducer implements
    IEditorManager {

    /**
     * @JTourBusStop 5, Some Basics:
     * 
     *               When you work on a project using Saros, you still use the
     *               standard Eclipse Editor, however Saros adds a little extra
     *               needed functionality to them.
     * 
     *               EditorManager is one of the most important classes in this
     *               respect. Remember that every change done in an Editor needs
     *               to be intercepted, translated into an Activity and sent to
     *               all other participants. Furthermore every Activity from
     *               other participants needs to be replayed in your local
     *               editor when it is received.
     */

    private static final Logger LOG = Logger.getLogger(EditorManager.class);

    private final IEditorAPI editorAPI;

    boolean hasWriteAccess;

    boolean isLocked;

    private ISarosSession session;

    private SharedEditorListenerDispatch editorListenerDispatch = new SharedEditorListenerDispatch();

    private final IPreferenceStore preferenceStore;

    private RemoteEditorManager remoteEditorManager;

    private RemoteWriteAccessManager remoteWriteAccessManager;

    @Inject
    private FileReplacementInProgressObservable fileReplacementInProgressObservable;

    /**
     * The user that is followed or <code>null</code> if no user is followed.
     */
    private User followedUser = null;

    private final EditorPool editorPool;

    private final IPartListener2 partListener;

    private SPath locallyActiveEditor;

    private Set<SPath> locallyOpenEditors = new HashSet<SPath>();

    private ITextSelection localSelection;

    private ILineRange localViewport;

    /** all files that have connected document providers */
    private final Set<IFile> connectedFiles = new HashSet<IFile>();

    private AnnotationModelHelper annotationModelHelper;
    private LocationAnnotationManager locationAnnotationManager;
    private ContributionAnnotationManager contributionAnnotationManager;

    private final CustomAnnotationManager customAnnotationManager = new CustomAnnotationManager();

    private final IPropertyChangeListener annotationPreferenceListener = new IPropertyChangeListener() {
        @Override
        public void propertyChange(final PropertyChangeEvent event) {
            locationAnnotationManager.propertyChange(event,
                editorPool.getAllEditors());
        }
    };

    private final IActivityConsumer consumer = new AbstractActivityConsumer() {
        /**
         * @JTourBusStop 12, Activity sending, More complex example of a second
         *               dispatch:
         * 
         *               The exec() method below is a more complex example of
         *               the second dispatch: Before letting the activity
         *               perform the third dispatch (done in super.exec()), this
         *               specific implementation dispatches the activity to two
         *               other consumers.
         */

        /***/
        @Override
        public void exec(IActivity activity) {
            assert SWTUtils.isSWT();

            User sender = activity.getSource();
            if (!sender.isInSession()) {
                LOG.warn("skipping execution of activity " + activity
                    + " for user " + sender
                    + " who is not in the current session");
                return;
            }

            // First let the remote managers update itself based on the
            // Activity
            remoteEditorManager.exec(activity);
            remoteWriteAccessManager.exec(activity);

            super.exec(activity);
        }

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

    private final Blockable stopManagerListener = new Blockable() {
        @Override
        public void unblock() {
            execute(false);
        }

        @Override
        public void block() {
            execute(true);
        }

        private void execute(final boolean lock) {
            SWTUtils.runSafeSWTSync(LOG, new Runnable() {
                @Override
                public void run() {
                    lockAllEditors(lock);
                }
            });
        }
    };

    private final ISessionListener sessionListener = new AbstractSessionListener() {

        @Override
        public void permissionChanged(final User user) {

            // Make sure we have the up-to-date facts about ourself
            hasWriteAccess = session.hasWriteAccess();

            // Lock / unlock editors
            if (user.isLocal()) {
                editorPool.setEditable(hasWriteAccess);
            }

            // TODO [PERF] 1 Make this lazy triggered on activating a part?
            refreshAnnotations();
        }

        @Override
        public void userFinishedProjectNegotiation(User user) {

            // TODO The user should be able to ask us for this state

            // Send awareness information
            User localUser = session.getLocalUser();
            for (SPath path : getLocallyOpenEditors()) {
                fireActivity(new EditorActivity(localUser, Type.ACTIVATED, path));
            }

            fireActivity(new EditorActivity(localUser, Type.ACTIVATED,
                locallyActiveEditor));

            if (locallyActiveEditor == null)
                return;

            if (localViewport != null) {
                fireActivity(new ViewportActivity(localUser,
                    localViewport.getStartLine(),
                    localViewport.getNumberOfLines(), locallyActiveEditor));
            } else {
                LOG.warn("No viewport for locallyActivateEditor: "
                    + locallyActiveEditor);
            }

            if (localSelection != null) {
                int offset = localSelection.getOffset();
                int length = localSelection.getLength();

                fireActivity(new TextSelectionActivity(localUser, offset,
                    length, locallyActiveEditor));
            } else {
                LOG.warn("No selection for locallyActivateEditor: "
                    + locallyActiveEditor);
            }
        }

        @Override
        public void userColorChanged(User user) {
            editorListenerDispatch.colorChanged();
            refreshAnnotations();
        }

        @Override
        public void userLeft(final User user) {

            // If the user left which I am following, then stop following...
            if (user.equals(followedUser))
                setFollowing(null);

            removeAnnotationsFromAllEditors(new Predicate<Annotation>() {
                @Override
                public boolean evaluate(Annotation annotation) {
                    return annotation instanceof SarosAnnotation
                        && ((SarosAnnotation) annotation).getSource().equals(
                            user);
                }
            });
            remoteEditorManager.removeUser(user);
        }
    };

    private final ISessionLifecycleListener sessionLifecycleListener = new NullSessionLifecycleListener() {

        @Override
        public void sessionStarted(final ISarosSession session) {
            SWTUtils.runSafeSWTSync(LOG, new Runnable() {
                @Override
                public void run() {
                    initialize(session);
                }
            });
        }

        @Override
        public void sessionEnded(final ISarosSession session) {
            SWTUtils.runSafeSWTSync(LOG, new Runnable() {
                @Override
                public void run() {
                    uninitialize();
                }
            });
        }

        @Override
        public void projectAdded(String projectID) {
            SWTUtils.runSafeSWTSync(LOG, new Runnable() {

                /*
                 * When Alice invites Bob to a session with a project and Alice
                 * has some Files of the shared project already open, Bob will
                 * not receive any Actions (Selection, Contribution etc.) for
                 * the open editors. When Alice closes and reopens this Files
                 * again everything is going back to normal. To prevent that
                 * from happening this method is needed.
                 */
                @Override
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
                        LOG.debug(editorPart.getTitle());
                        if (!editorsOpenedByRestoring.contains(editorPart))
                            partOpened(editorPart);
                    }

                    IEditorPart activeEditor = editorAPI.getActiveEditor();
                    if (activeEditor != null) {
                        locallyActiveEditor = editorAPI
                            .getEditorPath(activeEditor);
                        partActivated(activeEditor);
                    }

                }
            });
        }
    };

    private final ISharedEditorListener sharedEditorListener = new AbstractSharedEditorListener() {

        @Override
        public void editorActivated(User user, SPath filePath) {
            // #2707089 We must clear annotations from shared editors that are
            // not commonly viewed

            // We only need to react to remote users changing editor
            if (user.isLocal())
                return;

            // Clear all viewport annotations of this user. That's not a problem
            // because the order of the activities is:
            // (1) EditorActivity (triggered this method call),
            // (2) TextSelectionActivity,
            // (3) ViewportActivity.
            for (IEditorPart editor : editorPool.getAllEditors()) {
                locationAnnotationManager.clearViewportForUser(user, editor);
            }
        }
    };

    public EditorManager(ISarosSessionManager sessionManager,
        EditorAPI editorAPI, IPreferenceStore preferenceStore) {
        this.editorAPI = editorAPI;
        this.preferenceStore = preferenceStore;
        editorPool = new EditorPool(this, editorAPI);
        partListener = new SafePartListener2(LOG, new EditorPartListener(this));

        registerCustomAnnotations();
        sessionManager
            .addSessionLifecycleListener(this.sessionLifecycleListener);
        addSharedEditorListener(sharedEditorListener);
    }

    // FIXME thread access (used by ProjectDeltaVisitor which might NOT run from
    // the SWT Thread
    public boolean isManaged(IFile file) {
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
    void connect(final IFile file) {

        if (!file.isAccessible()) {
            LOG.error(".connect(): file " + file + " could not be accessed");
            return;
        }

        LOG.trace(".connect(" + file + ") invoked");

        if (isManaged(file)) {
            LOG.trace("file " + file + " is already connected");
            return;
        }

        FileEditorInput input = new FileEditorInput(file);
        IDocumentProvider documentProvider = editorAPI
            .getDocumentProvider(input);

        try {
            documentProvider.connect(input);
        } catch (CoreException e) {
            LOG.error("could not connect to document provider for file: "
                + file, e);
        }

        connectedFiles.add(file);
    }

    void disconnect(final IFile file) {

        LOG.trace(".disconnect(" + file + ") invoked");

        if (!isManaged(file)) {
            LOG.warn(".disconnect(): file " + file + " already disconnected");
            return;
        }

        FileEditorInput input = new FileEditorInput(file);

        IDocumentProvider documentProvider = editorAPI
            .getDocumentProvider(input);

        documentProvider.disconnect(input);

        connectedFiles.remove(file);
    }

    @Override
    public String getContent(final SPath path) {
        try {
            return SWTUtils.runSWTSync(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return doGetContent(path);
                }
            });
        } catch (Exception e) {
            LOG.warn("Failed to get editor content for " + path, e);
            return null;
        }
    }

    private String doGetContent(SPath path) {
        IFile file = ((EclipseFileImpl) path.getFile()).getDelegate();
        FileEditorInput input = new FileEditorInput(file);
        IDocumentProvider provider = null;
        String content;

        try {
            provider = editorAPI.getDocumentProvider(input);
            provider.connect(input);
            IDocument doc = provider.getDocument(input);
            content = (doc != null) ? doc.get() : null;
        } catch (CoreException e) {
            LOG.warn("Failed to retrieve the content of " + path, e);
            content = null;

            /*
             * A CoreException means that the provider.connect(input) call
             * failed. Because the connect() didn't receive, we avoid calling
             * disconnect() in the finally block by setting the provider
             * variable to null.
             */
            provider = null;

        } finally {
            if (provider != null)
                provider.disconnect(input);
        }

        return content;
    }

    @Override
    public Set<SPath> getLocallyOpenEditors() {
        return locallyOpenEditors;
    }

    @Override
    public Set<SPath> getRemotelyOpenEditors() {
        return remoteEditorManager == null ? Collections.<SPath> emptySet()
            : remoteEditorManager.getRemoteOpenEditors();
    }

    @Override
    public void addSharedEditorListener(ISharedEditorListener editorListener) {
        editorListenerDispatch.add(editorListener);
    }

    @Override
    public void removeSharedEditorListener(ISharedEditorListener editorListener) {
        editorListenerDispatch.remove(editorListener);
    }

    /**
     * Sets the local editor 'opened' and fires an {@link EditorActivity} of
     * type {@link Type#ACTIVATED}.
     * 
     * @param path
     *            the project-relative path to the resource that the editor is
     *            currently editing or <code>null</code> if the local user has
     *            no editor open.
     */
    void generateEditorActivated(SPath path) {

        this.locallyActiveEditor = path;

        if (path != null && session.isShared(path.getResource()))
            locallyOpenEditors.add(path);

        editorListenerDispatch.editorActivated(session.getLocalUser(), path);

        fireActivity(new EditorActivity(session.getLocalUser(), Type.ACTIVATED,
            path));

    }

    /**
     * Fires an update of the given viewport for the given {@link IEditorPart}
     * so that all remote parties know that the user is now positioned at the
     * given viewport in the given part.
     * 
     * A ViewportActivity not necessarily indicates that the given IEditorPart
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
    void generateViewport(IEditorPart part, ILineRange viewport) {

        if (this.session == null) {
            LOG.warn("SharedEditorListener not correctly unregistered!");
            return;
        }

        SPath path = editorAPI.getEditorPath(part);
        if (path == null) {
            LOG.warn("Could not find path for editor " + part.getTitle());
            return;
        }

        if (path.equals(locallyActiveEditor))
            this.localViewport = viewport;

        ViewportActivity activity = new ViewportActivity(
            session.getLocalUser(), viewport.getStartLine(),
            viewport.getNumberOfLines(), path);

        fireActivity(activity);
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
    void generateSelection(IEditorPart part, ITextSelection newSelection) {

        SPath path = editorAPI.getEditorPath(part);
        if (path == null) {
            LOG.warn("Could not find path for editor " + part.getTitle());
            return;
        }

        if (path.equals(locallyActiveEditor))
            localSelection = newSelection;

        int offset = newSelection.getOffset();
        int length = newSelection.getLength();

        fireActivity(new TextSelectionActivity(session.getLocalUser(), offset,
            length, path));
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
    void textAboutToBeChanged(int offset, String text, int replaceLength,
        IDocument document) {

        if (fileReplacementInProgressObservable.isReplacementInProgress())
            return;

        if (session == null) {
            LOG.error("session has ended but text edits"
                + " are received from local user");
            return;
        }

        IEditorPart changedEditor = null;

        // FIXME: This is potentially slow and definitely ugly
        // search editor which changed
        for (IEditorPart editor : editorPool.getAllEditors()) {
            if (ObjectUtils.equals(editorAPI.getDocument(editor), document)) {
                changedEditor = editor;
                break;
            }
        }

        if (changedEditor == null) {
            LOG.error("Could not find editor for changed document " + document);
            return;
        }

        SPath path = editorAPI.getEditorPath(changedEditor);
        if (path == null) {
            LOG.warn("Could not find path for editor "
                + changedEditor.getTitle());
            return;
        }

        String replacedText;
        try {
            replacedText = document.get(offset, replaceLength);
        } catch (BadLocationException e) {
            LOG.error("Offset and/or replace invalid", e);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < replaceLength; i++)
                sb.append("?");
            replacedText = sb.toString();
        }

        TextEditActivity textEdit = new TextEditActivity(
            session.getLocalUser(), offset, text, replacedText, path);

        if (!hasWriteAccess || isLocked) {
            /**
             * TODO If we don't have {@link User.Permission#WRITE_ACCESS}, then
             * receiving this event might indicate that the user somehow
             * achieved to change his document. We should run a consistency
             * check.
             * 
             * But watch out for changes because of a consistency check!
             */
            LOG.warn("local user caused text changes: " + textEdit
                + " | write access : " + hasWriteAccess + ", session locked : "
                + isLocked);
            return;
        }

        fireActivity(textEdit);

        // inform all registered ISharedEditorListeners about this text edit
        editorListenerDispatch.textEdited(session.getLocalUser(), path, offset,
            replacedText, text);

        /*
         * TODO Investigate if this is really needed here
         */
        IEditorInput input = changedEditor.getEditorInput();
        IDocumentProvider provider = editorAPI.getDocumentProvider(input);
        IAnnotationModel model = provider.getAnnotationModel(input);
        contributionAnnotationManager.splitAnnotation(model, offset);
    }

    private void execEditorActivity(EditorActivity editorActivity) {
        User sender = editorActivity.getSource();
        SPath sPath = editorActivity.getPath();
        switch (editorActivity.getType()) {
        case ACTIVATED:
            execActivated(sender, sPath);
            break;
        case CLOSED:
            execClosed(sender, sPath);
            break;
        case SAVED:
            saveEditor(sPath);
            break;
        default:
            LOG.warn("Unexpected type: " + editorActivity.getType());
        }
    }

    private void execTextEdit(TextEditActivity textEdit) {

        LOG.trace(".execTextEdit invoked");

        SPath path = textEdit.getPath();
        IFile file = ((EclipseFileImpl) path.getFile()).getDelegate();

        if (!file.exists()) {
            LOG.error("TextEditActivity refers to file which"
                + " is not available locally: " + textEdit);
            // TODO A consistency check can be started here
            return;
        }

        User user = textEdit.getSource();

        /*
         * Disable documentListener temporarily to avoid being notified of the
         * change, otherwise this would lead to an infinite activity sending,
         * crashing the application
         */
        editorPool.setDocumentListenerEnabled(false);

        replaceText(path, textEdit.getOffset(), textEdit.getReplacedText(),
            textEdit.getText(), user);

        editorPool.setDocumentListenerEnabled(true);

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

                TextSelection selection = new TextSelection(cursorOffset, 0);
                locationAnnotationManager.setSelection(editorPart, selection,
                    user);

                if (user.equals(getFollowedUser())) {
                    adjustViewport(user, editorPart, selection);
                }
            }
        }

        // inform all registered ISharedEditorListeners about this text edit
        editorListenerDispatch.textEdited(user, path, textEdit.getOffset(),
            textEdit.getReplacedText(), textEdit.getText());
    }

    private void execTextSelection(TextSelectionActivity selection) {

        LOG.trace(".execTextSelection invoked");

        SPath path = selection.getPath();

        if (path == null) {
            LOG.error("Received text selection but have no writable editor");
            return;
        }

        TextSelection textSelection = new TextSelection(selection.getOffset(),
            selection.getLength());

        User user = selection.getSource();

        Set<IEditorPart> editors = editorPool.getEditors(path);

        for (IEditorPart editorPart : editors) {
            locationAnnotationManager.setSelection(editorPart, textSelection,
                user);

            if (user.equals(getFollowedUser())) {
                adjustViewport(user, editorPart, textSelection);
            }
        }

        /*
         * inform all registered ISharedEditorListeners about a text selection
         * made
         */
        editorListenerDispatch.textSelectionChanged(selection);
    }

    private void execViewport(ViewportActivity viewport) {

        LOG.trace(".execViewport invoked");

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
             * null if ViewportActivity came before the first
             * TextSelectActivity.
             */
            if (userWithWriteAccessSelection != null) {
                /**
                 * TODO MR Taking the last line of the last selection of the
                 * user with {@link User.Permission#WRITE_ACCESS} might be a bit
                 * inaccurate.
                 */
                int userWithWriteAccessCursor = userWithWriteAccessSelection
                    .getEndLine();

                int top = viewport.getStartLine();
                int bottom = viewport.getStartLine()
                    + viewport.getNumberOfLines();

                following = following
                    && (userWithWriteAccessCursor < top || userWithWriteAccessCursor > bottom);
            }
        }

        Set<IEditorPart> editors = editorPool.getEditors(viewport.getPath());

        ILineRange lineRange = new LineRange(viewport.getStartLine(),
            viewport.getNumberOfLines());

        for (IEditorPart editorPart : editors) {
            if (following || user.hasWriteAccess())
                locationAnnotationManager.setViewportForUser(user, editorPart,
                    lineRange);

            if (following) {
                adjustViewport(user, editorPart, lineRange);
            }
        }
    }

    private void execActivated(User user, SPath path) {

        LOG.trace(".execActivated invoked");

        editorListenerDispatch.editorActivated(user, path);

        /**
         * Path null means this user with {@link User.Permission#WRITE_ACCESS}
         * has no active editor any more
         */
        if (user.equals(getFollowedUser()) && path != null) {

            // open editor but don't change focus
            editorAPI.openEditor(path, false);

        } else if (user.equals(getFollowedUser()) && path == null) {
            /*
             * Changed waldmann 22.01.2012: Since the currently opened file and
             * the information if no shared files are opened is permanently
             * shown in the saros view, this is no longer necessary and has
             * proven to be quite annoying to users too. This should only be
             * activated again if a preference is added to enable users to
             * disable this type of notification
             */

            // SarosView
            // .showNotification(
            // "Follow mode paused!",
            // user.getHumanReadableName()
            // +
            // " selected an editor that is not shared. \nFollow mode stays active and follows as soon as possible.");
        }
    }

    private void execClosed(User user, SPath path) {

        LOG.trace(".execClosed invoked");

        editorListenerDispatch.editorClosed(user, path);

        for (final IEditorPart editorPart : editorPool.getEditors(path))
            locationAnnotationManager.clearSelectionForUser(user, editorPart);

        if (user.equals(getFollowedUser())) {
            for (IEditorPart part : editorPool.getEditors(path)) {
                editorAPI.closeEditor(part);
            }
        }
    }

    /**
     * Called when the local user opened an editor part. (called by
     * EditorPartListener)
     */
    void partOpened(IEditorPart editorPart) {

        LOG.trace(".partOpened invoked");

        if (!isSharedEditor(editorPart))
            return;

        /*
         * If the resource is not accessible it might have been deleted without
         * the editor having been closed (for instance outside of Eclipse).
         * Others might be confused about if they receive this editor from us.
         */
        final IResource resource = editorAPI.getEditorResource(editorPart);

        if (!resource.isAccessible()) {
            LOG.warn(".partOpened resource: " + resource + " is not accessible");
            return;
        }

        /*
         * side effect: this method call also locks the editor part if the user
         * has no write access or if the session is currently locked
         */
        editorPool.add(editorPart);

        final ITextViewer viewer = EditorAPI.getViewer(editorPart);

        if (viewer instanceof ISourceViewer)
            customAnnotationManager.installPainter((ISourceViewer) viewer);

        refreshAnnotations(editorPart);

        // HACK 6 Why does this not work via partActivated? Causes duplicate
        // activate events
        partActivated(editorPart);
    }

    /**
     * Called when the local user activated a shared editor.
     * 
     * This can be called twice for a single IEditorPart, because it is called
     * from partActivated and from partBroughtToTop.
     * 
     * We do not filter duplicate events, because it would be bad to miss events
     * and is not too bad have duplicate one's. In particular we use IPath as an
     * identifier to the IEditorPart which might not work for multiple editors
     * based on the same file. (called by EditorPartListener)
     */
    void partActivated(IEditorPart editorPart) {

        LOG.trace(".partActivated invoked");

        // First check for last editor being closed (which is a null editorPart)
        if (editorPart == null) {
            generateEditorActivated(null);
            return;
        }

        // Is the new editor part supported by Saros (and inside the project)
        // and the Resource accessible (we don't want to report stale files)?
        IResource resource = editorAPI.getEditorResource(editorPart);

        if (!isSharedEditor(editorPart)
            || !session.isShared(ResourceAdapterFactory.create(resource))
            || !editorAPI.getEditorResource(editorPart).isAccessible()) {
            generateEditorActivated(null);
            // follower switched to another unshared editor or closed followed
            // editor (not shared editor gets activated)
            if (isFollowing()) {
                setFollowing(null);
                SarosView
                    .showNotification(
                        "Follow Mode stopped!",
                        "You switched to another editor that is not shared \nor closed the followed editor.");
            }
            return;
        }

        /*
         * If the opened editor is not the active editor of the user being
         * followed, then leave follow mode
         */
        if (isFollowing()) {
            RemoteEditor activeEditor = remoteEditorManager.getEditorState(
                getFollowedUser()).getActiveEditor();

            if (activeEditor != null
                && !activeEditor.getPath().equals(
                    editorAPI.getEditorPath(editorPart))) {
                setFollowing(null);
                // follower switched to another shared editor or closed followed
                // editor (shared editor gets activated)
                SarosView
                    .showNotification("Follow Mode stopped!",
                        "You switched to another editor \nor closed the followed editor.");
            }
        }

        SPath editorPath = editorAPI.getEditorPath(editorPart);
        ILineRange viewport = editorAPI.getViewport(editorPart);
        ITextSelection selection = editorAPI.getSelection(editorPart);

        // Set (and thus send) in this order:
        generateEditorActivated(editorPath);
        generateSelection(editorPart, selection);
        if (viewport == null) {
            LOG.warn("Shared Editor does not have a Viewport: " + editorPart);
        } else {
            generateViewport(editorPart, viewport);
        }

        ITextViewer viewer = EditorAPI.getViewer(editorPart);

        if (viewer instanceof ISourceViewer)
            customAnnotationManager.installPainter((ISourceViewer) viewer);

    }

    /**
     * Called if the IEditorInput of the IEditorPart is now something different
     * than before! Probably when renaming. (called by EditorPartListener)
     * 
     */
    void partInputChanged(IEditorPart editorPart) {

        LOG.trace(".partInputChanged invoked");

        // notice currently followed user before closing the editor
        User followedUser = getFollowedUser();

        if (editorPool.isManaged(editorPart)) {

            // Pretend as if the editor was closed locally (but use the old part
            // before the move happened) and then simulate it being opened again
            SPath path = editorPool.getPath(editorPart);
            if (path == null) {
                LOG.warn("Editor was managed but path could not be found: "
                    + editorPart);
            } else {
                partClosedOfPath(editorPart, path);
            }

            partOpened(editorPart);

            // restore the previously followed user
            // in case it was set and has changed
            if (followedUser != null && followedUser != getFollowedUser())
                setFollowing(followedUser);
        }
    }

    /**
     * Called if the local user closed a part (called by EditorPartListener)
     */
    void partClosed(IEditorPart editorPart) {

        LOG.trace(".partClosed invoked");

        if (!isSharedEditor(editorPart)) {
            return;
        }

        SPath path = editorAPI.getEditorPath(editorPart);

        partClosedOfPath(editorPart, path);
    }

    private void partClosedOfPath(IEditorPart editorPart, SPath path) {

        // if closing the followed editor, leave follow mode
        if (getFollowedUser() != null) {
            RemoteEditor activeEditor = remoteEditorManager.getEditorState(
                getFollowedUser()).getActiveEditor();

            if (activeEditor != null && activeEditor.getPath().equals(path)) {
                // follower closed the followed editor (no other editor gets
                // activated)
                setFollowing(null);
                SarosView.showNotification("Follow Mode stopped!",
                    "You closed the followed editor.");
            }
        }

        editorPool.remove(editorPart);

        ITextViewer viewer = EditorAPI.getViewer(editorPart);

        if (viewer instanceof ISourceViewer)
            customAnnotationManager.uninstallPainter((ISourceViewer) viewer,
                false);

        // Check if the currently active editor is closed
        boolean newActiveEditor = path.equals(this.locallyActiveEditor);

        locallyOpenEditors.remove(path);

        editorListenerDispatch.editorClosed(session.getLocalUser(), path);

        fireActivity(new EditorActivity(session.getLocalUser(), Type.CLOSED,
            path));

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
     * @return <code>true</code> if the given resource is opened according to
     *         the editor pool.
     */
    // FIXME thread access (used by ProjectDeltaVisitor which might NOT run from
    // the SWT Thread
    public boolean isOpened(SPath path) {
        return editorPool.getEditors(path).size() > 0;
    }

    /**
     * Checks if the local currently active editor is part of the running Saros
     * session.
     * 
     * @return <code>true</code>, if active editor is part of the Saros session,
     *         <code>false</code> otherwise.
     */
    public boolean isActiveEditorShared() {
        checkThreadAccess();
        IEditorPart editorPart = editorAPI.getActiveEditor();
        return editorPart == null ? false : isSharedEditor(editorPart);
    }

    /**
     * Verifies if the given <code>IEditorPart</code> is supported by Saros,
     * which is based basically on following facts:
     * <ol>
     * <li>Has an underlying <code>IResource</code> as storage.</li>
     * <li>Can be adapted to an <code>ITextViewer</code>.</li>
     * <li>The underlying <code>IResource</code> is part of the current
     * (partial) project sharing (see {@link ISarosSession#isShared}).</li>
     * </ol>
     */
    private boolean isSharedEditor(final IEditorPart editorPart) {
        if (session == null)
            return false;

        if (EditorAPI.getViewer(editorPart) == null)
            return false;

        final IResource resource = editorAPI.getEditorResource(editorPart);

        if (resource == null)
            return false;

        return session.isShared(ResourceAdapterFactory.create(resource
            .getProject()));
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
    private void replaceText(SPath path, int offset, String replacedText,
        String text, User source) {

        IFile file = ((EclipseFileImpl) path.getFile()).getDelegate();
        FileEditorInput input = new FileEditorInput(file);
        IDocumentProvider provider = editorAPI.getDocumentProvider(input);

        try {
            provider.connect(input);
        } catch (CoreException e) {
            LOG.error(
                "Could not connect document provider for file: "
                    + file.toString(), e);
            // TODO Trigger a consistency recovery
            return;
        }

        try {
            IDocument doc = provider.getDocument(input);
            if (doc == null) {
                LOG.error("Could not connect document provider for file: "
                    + file.toString(), new StackTrace());
                // TODO Trigger a consistency recovery
                return;
            }

            // Check if the replaced text is really there.
            if (LOG.isDebugEnabled()) {

                String is;
                try {
                    is = doc.get(offset, replacedText.length());
                    if (!is.equals(replacedText)) {
                        LOG.error("replaceText should be '"
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
                LOG.error(String.format(
                    "Could not apply TextEdit at %d-%d of document "
                        + "with length %d.\nWas supposed to replace"
                        + " '%s' with '%s'.", offset,
                    offset + replacedText.length(), doc.getLength(),
                    replacedText, text));
                return;
            }

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
     * SWT thread.
     * 
     * @blocking This method returns after the file has been saved in the SWT
     *           Thread.
     */
    public void saveLazy(final SPath path) {

        SWTUtils.runSafeSWTSync(LOG, new Runnable() {

            @Override
            public void run() {

                boolean isDirty = false;
                try {
                    isDirty = isDirty(path);
                } catch (FileNotFoundException e) {
                    LOG.warn("could not save editor: " + path, e);
                }

                if (isDirty)
                    saveEditor(path);
            }
        });
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
    private boolean isDirty(SPath path) throws FileNotFoundException {

        IFile file = ((EclipseFileImpl) path.getFile()).getDelegate();

        if (file == null || !file.exists()) {
            throw new FileNotFoundException("File not found: " + path);
        }

        FileEditorInput input = new FileEditorInput(file);

        return editorAPI.getDocumentProvider(input).canSaveDocument(input);
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
    public void saveEditor(SPath path) {
        checkThreadAccess();

        IFile file = ((EclipseFileImpl) path.getFile()).getDelegate();

        LOG.trace(".saveEditor (" + file.getName() + ") invoked");

        if (!file.exists()) {
            LOG.warn("File not found for saving: " + path.toString(),
                new StackTrace());
            return;
        }

        FileEditorInput input = new FileEditorInput(file);
        IDocumentProvider provider = editorAPI.getDocumentProvider(input);

        if (!provider.canSaveDocument(input)) {
            /*
             * This happens when a file which is already saved is saved again by
             * a user.
             */
            LOG.debug(".saveEditor File " + file.getName()
                + " does not need to be saved");
            return;
        }

        LOG.trace(".saveEditor File " + file.getName() + " will be saved");

        try {
            provider.connect(input);
        } catch (CoreException e) {
            LOG.error("Could not connect to a document provider on file '"
                + file.toString() + "':", e);
            return;
        }

        final boolean isConnected = isManaged(file);

        /*
         * connect to the file so the SharedResourceManager /
         * ProjectDeltaVisitor will ignore the file change because it is
         * possible that no editor is open for this file
         */
        if (!isConnected)
            connect(file);

        try {
            IDocument doc = provider.getDocument(input);

            // TODO Why do we need to connect to the annotation model here?
            IAnnotationModel model = provider.getAnnotationModel(input);
            if (model != null)
                model.connect(doc);

            LOG.trace(".saveEditor Annotations on the IDocument are set");

            editorPool.setElementStateListenerEnabled(false);

            try {
                provider.saveDocument(new NullProgressMonitor(), input, doc,
                    true);
                LOG.debug("Saved document: " + path);
            } catch (CoreException e) {
                LOG.error("Failed to save document: " + path, e);
            }

            editorPool.setElementStateListenerEnabled(true);

            if (model != null)
                model.disconnect(doc);
        } finally {
            provider.disconnect(input);
            if (!isConnected)
                disconnect(file);
        }
    }

    /**
     * Sends an Activity for clients to save the editor of given path.
     * 
     * @param path
     *            the project relative path to the resource that the user with
     *            {@link Permission#WRITE_ACCESS} was editing.
     */
    void sendEditorActivitySaved(SPath path) {
        fireActivity(new EditorActivity(session.getLocalUser(), Type.SAVED,
            path));
    }

    /**
     * Removes all Annotation that fulfill given {@link Predicate} from all
     * editors.
     * 
     * @param predicate
     *            The filter to use for cleaning.
     */
    private void removeAnnotationsFromAllEditors(Predicate<Annotation> predicate) {

        for (IEditorPart editor : this.editorPool.getAllEditors()) {
            annotationModelHelper
                .removeAnnotationsFromEditor(editor, predicate);
        }
    }

    /**
     * Returns <code>true</code> if there is currently a {@link User} followed,
     * otherwise <code>false</code>.
     */
    // FIXME misplaced logic
    public boolean isFollowing() {
        return getFollowedUser() != null;
    }

    /**
     * Returns the followed {@link User} or <code>null</code> if currently no
     * user is followed.
     */
    // FIXME misplaced logic
    public User getFollowedUser() {
        return followedUser;
    }

    /**
     * Sets the {@link User} to follow or <code>null</code> if no user should be
     * followed.
     */
    // FIXME misplaced logic
    public void setFollowing(User newFollowedUser) {
        assert newFollowedUser == null
            || !newFollowedUser.equals(session.getLocalUser()) : "local user cannot follow himself!";

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

    private void refreshAnnotations() {
        for (IEditorPart part : editorPool.getAllEditors()) {
            refreshAnnotations(part);
        }
    }

    /**
     * Removes and then re-adds all annotations (viewport, contribution,
     * selection, ...) for the given editor part.
     * 
     * @param editorPart
     *            the editor part to refresh
     */
    private void refreshAnnotations(IEditorPart editorPart) {

        SPath path = editorAPI.getEditorPath(editorPart);
        if (path == null) {
            LOG.warn("Could not find path for editor " + editorPart.getTitle());
            return;
        }

        /*
         * ContributionAnnotations must not be removed here otherwise the
         * history in the ContributionAnnotationManager will break.
         */
        annotationModelHelper.removeAnnotationsFromEditor(editorPart,
            new Predicate<Annotation>() {
                @Override
                public boolean evaluate(Annotation annotation) {
                    return annotation instanceof SarosAnnotation
                        && !(annotation instanceof ContributionAnnotation);
                }
            });

        ITextViewer viewer = EditorAPI.getViewer(editorPart);

        if ((viewer instanceof ISourceViewer)) {
            contributionAnnotationManager
                .refreshAnnotations(((ISourceViewer) viewer)
                    .getAnnotationModel());
        }

        for (User user : session.getUsers()) {
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
                ILineRange lineRange = remoteEditor.getViewport();
                if (lineRange != null) {
                    locationAnnotationManager.setViewportForUser(user,
                        editorPart, lineRange);
                }
            }

            ITextSelection selection = remoteEditor.getSelection();
            if (selection != null) {
                locationAnnotationManager.setSelection(editorPart, selection,
                    user);
            }
        }
    }

    /**
     * @deprecated
     * @return
     */
    @Deprecated
    public RemoteEditorManager getRemoteEditorManager() {
        return this.remoteEditorManager;
    }

    // FIXME misplaced logic
    public void jumpToUser(User jumpTo) {

        RemoteEditor activeEditor = remoteEditorManager.getEditorState(jumpTo)
            .getActiveEditor();

        // you can't follow yourself
        if (session.getLocalUser().equals(jumpTo))
            return;

        if (activeEditor == null) {
            LOG.debug("user " + jumpTo + " has no editor open");

            // changed waldmann, 22.01.2012: this balloon Notification became
            // annoying as the awareness information, which file is opened is
            // now shown in the session view all the time (unless the user has
            // collapsed the tree element)

            // no active editor on target subject
            // SarosView.showNotification("Following " +
            // jumpTo.getJID().getBase()
            // + "!", jumpTo.getJID().getName()
            // + " has no shared file opened yet.");
            return;
        }

        IEditorPart newEditor = this.editorAPI.openEditor(activeEditor
            .getPath());

        if (newEditor == null) {
            return;
        }

        ILineRange viewport = activeEditor.getViewport();

        if (viewport == null) {
            LOG.warn("user " + jumpTo + " has no viewport in editor: "
                + activeEditor.getPath());
            return;
        }

        adjustViewport(jumpTo, newEditor, viewport);

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
        return remoteEditorManager == null ? Collections.<SPath> emptySet()
            : remoteEditorManager.getRemoteOpenEditors(user);
    }

    /**
     * Locks/unlocks all Editors for writing operations. Locked means local
     * keyboard inputs are not applied.
     * 
     * @param lock
     *            if true then editors are locked, otherwise they are unlocked
     */
    private void lockAllEditors(boolean lock) {
        LOG.debug(lock ? "locking all editors" : "unlocking all editors");

        editorPool.setEditable(!lock && session.hasWriteAccess());

        isLocked = lock;
    }

    /**
     * Convenience method for determining whether a file is currently open in an
     * editor.
     * 
     * @param path
     *            path of the file to check
     * @return <code>true</code> if there is an open editor for this file,
     *         <code>false</code> otherwise
     */
    public boolean isOpenEditor(SPath path) {
        checkThreadAccess();

        if (path == null)
            throw new IllegalArgumentException("path must not be null");

        for (IEditorPart editorPart : EditorAPI.getOpenEditors()) {
            IResource resource = editorAPI.getEditorResource(editorPart);

            if (resource == null)
                continue;

            if (ResourceAdapterFactory.create(resource).equals(
                path.getResource()))
                return true;
        }

        return false;
    }

    /**
     * Open the editor window of given {@link SPath}.
     * 
     * @param path
     *            Path of the Editor to open.
     */
    public void openEditor(SPath path) {
        checkThreadAccess();

        if (path == null)
            throw new IllegalArgumentException();

        editorAPI.openEditor(path);
    }

    /**
     * Close the editor of given {@link SPath}.
     * 
     * @param path
     *            Path of the file of which the editor should be closed
     */
    public void closeEditor(SPath path) {
        checkThreadAccess();

        if (path == null)
            throw new IllegalArgumentException("path must not be null");

        for (IEditorPart iEditorPart : EditorAPI.getOpenEditors()) {
            IResource resource = editorAPI.getEditorResource(iEditorPart);

            if (resource == null)
                continue;

            if (ResourceAdapterFactory.create(resource).equals(
                path.getResource()))
                editorAPI.closeEditor(iEditorPart);
        }
    }

    /*
     * IEditorManager IMPL start
     */
    @Override
    public void saveEditors(final IProject project) {
        SWTUtils.runSafeSWTSync(LOG, new Runnable() {

            @Override
            public void run() {

                if (remoteEditorManager == null)
                    return;

                final Set<SPath> editorPaths = remoteEditorManager
                    .getRemoteOpenEditors();

                editorPaths.addAll(locallyOpenEditors);

                for (final SPath path : editorPaths) {
                    if (project == null || project.equals(path.getProject()))
                        saveLazy(path);
                }
            }
        });
    }

    /*
     * IEditorManager IMPL end
     */

    /**
     * Add annotation types and drawing strategies using the following two
     * method calls
     * {@link CustomAnnotationManager#registerAnnotation(String, int)
     * registerAnnotation()} and
     * {@link CustomAnnotationManager#registerDrawingStrategy(String, org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy)
     * registerDrawingStrategy()} .
     */
    private void registerCustomAnnotations() {
        /*
         * Explanation of the "layer magic": The six SelectionAnnotationTypes (1
         * default + 5 users) are located on layers 8 to 13 (see plugin.xml,
         * extension point "markerAnnotationSpecification").
         */
        int defaultSelectionLayer = 8;

        /*
         * For every SelectionFillUpAnnotation of a different color there is an
         * own layer. There is one layer for the default color and one for each
         * of the different user colors. All SelectionFillUpAnnotations are
         * drawn in lower levels than the RemoteCursorAnnotation.
         * 
         * TODO: The color determines on which layer a selection will be drawn.
         * So, in "competitive" situations, some selections will never be
         * visible because they are always drawn in a lower layer. (Can only
         * happen in sessions with more than two participants: Carl selected a
         * block, Bob selects a statement within, Alice might not be able to see
         * Bob's selection until Carl's changes his.)
         */
        SelectionFillUpStrategy strategy = new SelectionFillUpStrategy();
        for (int i = 0; i <= SarosAnnotation.SIZE; i++) {
            String type = SarosAnnotation.getNumberedType(
                SelectionFillUpAnnotation.TYPE, i);
            customAnnotationManager.registerAnnotation(type,
                defaultSelectionLayer + i);
            customAnnotationManager.registerDrawingStrategy(type, strategy);
        }

        /*
         * The RemoteCursorAnnotations are drawn in the layer above.
         */
        customAnnotationManager.registerAnnotation(RemoteCursorAnnotation.TYPE,
            defaultSelectionLayer + SarosAnnotation.SIZE + 1);
        customAnnotationManager.registerDrawingStrategy(
            RemoteCursorAnnotation.TYPE, new RemoteCursorStrategy());
    }

    /**
     * Adjusts the viewport in Follow Mode. This function should be called if
     * the followed user's selection changes.
     * 
     * @param followedUser
     *            User who is followed
     * @param editorPart
     *            EditorPart of the open Editor
     * @param selection
     *            text selection of the followed user
     */
    private void adjustViewport(User followedUser, IEditorPart editorPart,
        ITextSelection selection) {
        if (selection == null)
            return;

        // range can be null
        ILineRange range = remoteEditorManager.getViewport(followedUser);
        adjustViewport(editorPart, range, selection);
    }

    /**
     * Adjusts the viewport in Follow Mode. This function should be called if
     * the followed user's viewport changes.
     * 
     * @param followedUser
     *            User who is followed
     * @param editorPart
     *            EditorPart of the open Editor
     * @param range
     *            viewport of the followed user
     */
    private void adjustViewport(User followedUser, IEditorPart editorPart,
        ILineRange range) {
        if (range == null)
            return;

        // selection can be null
        ITextSelection selection = remoteEditorManager
            .getSelection(followedUser);
        adjustViewport(editorPart, range, selection);
    }

    /**
     * Adjusts viewport. Focus is set on the center of the range, but priority
     * is given to selected lines. Either range or selection can be null, but
     * not both.
     * 
     * @param editorPart
     *            EditorPart of the open Editor
     * @param range
     *            viewport of the followed user. Can be <code>null</code>.
     * @param selection
     *            text selection of the followed user. Can be <code>null</code>.
     * 
     */
    private void adjustViewport(IEditorPart editorPart, ILineRange range,
        ITextSelection selection) {
        ITextViewer viewer = EditorAPI.getViewer(editorPart);
        if (viewer == null)
            return;

        IDocument document = viewer.getDocument();
        ILineRange localViewport = EditorAPI.getViewport(viewer);

        if (localViewport == null || document == null)
            return;

        int lines = document.getNumberOfLines();
        int rangeTop = 0;
        int rangeBottom = 0;
        int selectionTop = 0;
        int selectionBottom = 0;

        if (selection != null) {
            try {
                selectionTop = document.getLineOfOffset(selection.getOffset());
                selectionBottom = document.getLineOfOffset(selection
                    .getOffset() + selection.getLength());
            } catch (BadLocationException e) {
                // should never be reached
                LOG.error("Invalid line selection: offset: "
                    + selection.getOffset() + ", length: "
                    + selection.getLength());

                selection = null;
            }
        }

        if (range != null) {
            if (range.getStartLine() == -1) {
                range = null;
            } else {
                rangeTop = Math.min(lines - 1, range.getStartLine());
                rangeBottom = Math.min(lines - 1,
                    rangeTop + range.getNumberOfLines());
            }
        }

        if (range == null && selection == null)
            return;

        // top line of the new viewport
        int topPosition;
        int localLines = localViewport.getNumberOfLines();
        int remoteLines = rangeBottom - rangeTop;
        int sizeDiff = remoteLines - localLines;

        // initializations finished

        if (range == null || selection == null) {
            topPosition = (rangeTop + rangeBottom + selectionTop + selectionBottom) / 2;
            viewer.setTopIndex(topPosition);
            return;
        }

        /*
         * usually the viewport of the follower and the viewport of the followed
         * user will have the same center (this calculation). Exceptions may be
         * made below.
         */
        int center = (rangeTop + rangeBottom) / 2;
        topPosition = center - localLines / 2;

        if (sizeDiff <= 0) {
            // no further examination necessary when the local viewport is the
            // larger one
            viewer.setTopIndex(Math.max(0, Math.min(topPosition, lines)));
            return;
        }

        boolean selectionTopInvisible = (selectionTop < rangeTop + sizeDiff / 2);
        boolean selectionBottomInvisible = (selectionBottom > rangeBottom
            - sizeDiff / 2 - 1);

        if (rangeTop == 0
            && !(selectionTop <= rangeBottom && selectionTop > rangeBottom
                - sizeDiff)) {
            // scrolled to the top and no selection at the bottom of range
            topPosition = 0;

        } else if (rangeBottom == lines - 1
            && !(selectionBottom >= rangeTop && selectionBottom < rangeTop
                + sizeDiff)) {
            // scrolled to the bottom and no selection at the top of range
            topPosition = lines - localLines;

        } else if (selectionTopInvisible && selectionBottom >= rangeTop) {
            // making selection at top of range visible
            topPosition = Math.max(rangeTop, selectionTop);

        } else if (selectionBottomInvisible && selectionTop <= rangeBottom) {
            // making selection at bottom of range visible
            topPosition = Math.min(rangeBottom, selectionBottom) - localLines
                + 1;
        }

        viewer.setTopIndex(Math.max(0, Math.min(topPosition, lines)));
    }

    /**
     * Performs necessary initialization, i.e setting up listeners etc. for the
     * given session.
     */
    private void initialize(final ISarosSession newSession) {
        checkThreadAccess();

        assert session == null;
        assert editorPool.getAllEditors().size() == 0 : "EditorPool was not correctly reset!";

        session = newSession;

        session.getStopManager().addBlockable(stopManagerListener);

        hasWriteAccess = session.hasWriteAccess();
        session.addListener(sessionListener);
        session.addActivityProducer(this);
        session.addActivityConsumer(consumer);

        annotationModelHelper = new AnnotationModelHelper();
        locationAnnotationManager = new LocationAnnotationManager(
            preferenceStore);

        contributionAnnotationManager = new ContributionAnnotationManager(
            session, preferenceStore);

        remoteEditorManager = new RemoteEditorManager(session);
        remoteWriteAccessManager = new RemoteWriteAccessManager(session,
            editorAPI);

        preferenceStore.addPropertyChangeListener(annotationPreferenceListener);

        /*
         * FIXME there can be multiple workbench windows (see Eclipse:
         * Window->New Window menu entry)
         */
        IWorkbenchWindow window = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow();

        if (window != null)
            window.getPartService().addPartListener(partListener);
    }

    /**
     * Performs necessary uninitialization, i.e deregistering listeners etc.
     */
    private void uninitialize() {
        checkThreadAccess();

        setFollowing(null);

        /*
         * FIXME there can be multiple workbench windows (see Eclipse:
         * Window->New Window menu entry)
         */
        IWorkbenchWindow window = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow();

        if (window != null)
            window.getPartService().removePartListener(partListener);

        preferenceStore
            .removePropertyChangeListener(annotationPreferenceListener);

        /*
         * First need to remove the annotations and then clear the editorPool
         */
        removeAnnotationsFromAllEditors(new Predicate<Annotation>() {
            @Override
            public boolean evaluate(Annotation annotation) {
                return annotation instanceof SarosAnnotation;
            }
        });

        editorPool.removeAllEditors();

        customAnnotationManager.uninstallAllPainters(true);

        assert session != null;
        session.getStopManager().removeBlockable(stopManagerListener);
        session.removeListener(sessionListener);
        session.removeActivityProducer(this);
        session.removeActivityConsumer(consumer);
        session = null;

        annotationModelHelper = null;
        locationAnnotationManager = null;
        contributionAnnotationManager.dispose();
        contributionAnnotationManager = null;
        remoteEditorManager = null;
        remoteWriteAccessManager.dispose();
        remoteWriteAccessManager = null;
        locallyActiveEditor = null;
        locallyOpenEditors.clear();

    }

    private void checkThreadAccess() {
        if (!SWTUtils.isSWT())
            throw new IllegalStateException("method must be invoked from EDT");
    }
}
