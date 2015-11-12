package de.fu_berlin.inf.dpp.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;

import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.ViewportActivity;
import de.fu_berlin.inf.dpp.editor.text.LineRange;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

/**
 * This class contains the state of the editors, viewports and selections of all
 * remote users as we believe it to be by listening to the Activities we
 * receive.
 */
public class RemoteEditorManager implements IActivityConsumer {

    private static final Logger log = Logger
        .getLogger(RemoteEditorManager.class);

    protected Map<User, RemoteEditorState> editorStates = new HashMap<User, RemoteEditorState>();

    protected ISarosSession sarosSession;

    /**
     * One editor of one user
     */
    public static class RemoteEditor {

        protected SPath path;

        protected ITextSelection selection;

        protected LineRange viewport;

        public RemoteEditor(SPath path) {
            this.path = path;
        }

        public SPath getPath() {
            return path;
        }

        public LineRange getViewport() {
            return viewport;
        }

        public void setViewport(LineRange viewport) {
            this.viewport = viewport;
        }

        public void setSelection(ITextSelection selection) {
            this.selection = selection;
        }

        public ITextSelection getSelection() {
            return this.selection;
        }
    }

    /**
     * Instances of this class represent the state of the editors of one user,
     * including the viewports and selections in each of these.
     */
    public static class RemoteEditorState {

        private final User user;

        private final LinkedHashMap<SPath, RemoteEditor> openEditors = new LinkedHashMap<SPath, RemoteEditor>();

        private RemoteEditor activeEditor;

        /**
         * Only to be accessed be the surrounding class
         */
        private final IActivityConsumer consumer = new AbstractActivityConsumer() {
            @Override
            public void receive(EditorActivity editorActivity) {
                SPath sPath = editorActivity.getPath();

                switch (editorActivity.getType()) {
                case ACTIVATED:
                    activated(sPath);
                    break;
                case SAVED:
                    break;
                case CLOSED:
                    closed(sPath);
                    break;
                default:
                    log.warn("Unexpected type: " + editorActivity.getType());
                    assert false;
                }
            }

            @Override
            public void receive(ViewportActivity viewportActivity) {
                LineRange lineRange = new LineRange(
                    viewportActivity.getStartLine(),
                    viewportActivity.getNumberOfLines());
                SPath path = viewportActivity.getPath();

                if (!openEditors.containsKey(path)) {
                    log.warn("Viewport for editor which was never activated: "
                        + path);
                    return;
                }

                getRemoteEditor(path).setViewport(lineRange);
            }

            @Override
            public void receive(TextSelectionActivity textSelectionActivity) {
                ITextSelection selection = new TextSelection(
                    textSelectionActivity.getOffset(),
                    textSelectionActivity.getLength());

                SPath path = textSelectionActivity.getPath();

                if (!openEditors.containsKey(path)) {
                    log.warn("received selection from user [" + user
                        + "] for editor which was never activated: " + path);
                    return;
                }

                getRemoteEditor(path).setSelection(selection);
            }
        };

        public RemoteEditorState(User user) {
            this.user = user;
        }

        /* Helper */

        private void activated(SPath path) {
            if (path == null) {
                activeEditor = null;
            } else {
                activeEditor = openEditors.get(path);

                // Create new or reinsert at the end of the Map
                if (activeEditor == null) {
                    activeEditor = new RemoteEditor(path);
                } else {
                    openEditors.remove(path);
                }
                openEditors.put(path, activeEditor);
            }
        }

        private void closed(SPath path) {
            RemoteEditor remoteEditor = openEditors.remove(path);

            if (remoteEditor == null) {
                log.warn("Removing an editor which has never been added: "
                    + path);
                return;
            }

            if (remoteEditor == activeEditor) {
                activeEditor = null;
            }
        }

        /* Public methods */

        public User getUser() {
            return this.user;
        }

        /**
         * Returns the activeEditor of the user of this RemoteEditorState or
         * null if the user has no editor open currently.
         */
        public RemoteEditor getActiveEditor() {
            return this.activeEditor;
        }

        public boolean isRemoteActiveEditor(SPath path) {
            if (activeEditor != null && activeEditor.getPath().equals(path))
                return true;
            return false;
        }

        /**
         * Returns a snapshot copy of the editors open for the user represented
         * by this RemoteEditorState.
         */
        public Set<SPath> getRemoteOpenEditors() {
            return new HashSet<SPath>(openEditors.keySet());
        }

        /**
         * Returns a RemoteEditor representing the given path for the given
         * user.
         * 
         * This method never returns null but creates a new RemoteEditor lazily.
         * 
         * To query whether the user of this RemoteEditorState has the editor of
         * the given path open use isRemoteOpenEditor().
         */
        public RemoteEditor getRemoteEditor(SPath path) {
            RemoteEditor result = openEditors.get(path);
            if (result == null) {
                result = new RemoteEditor(path);
                openEditors.put(path, result);
            }
            return result;
        }

        public boolean isRemoteOpenEditor(SPath path) {
            return openEditors.containsKey(path);
        }
    }

    public RemoteEditorManager(ISarosSession sarosSession) {
        this.sarosSession = sarosSession;
    }

    public RemoteEditorState getEditorState(User user) {
        RemoteEditorState result = editorStates.get(user);
        if (result == null) {
            result = new RemoteEditorState(user);
            editorStates.put(user, result);
        }
        return result;
    }

    @Override
    public void exec(IActivity activity) {
        RemoteEditorState editorState = getEditorState(activity.getSource());
        editorState.consumer.exec(activity);
    }

    /**
     * Returns the selection of the given user in the currently active editor or
     * null if the user has no active editor or no selection in the active
     * editor.
     */
    public ITextSelection getSelection(User user) {
        RemoteEditor activeEditor = getEditorState(user).getActiveEditor();

        if (activeEditor == null)
            return null;

        return activeEditor.getSelection();
    }

    /**
     * @return the viewport of the given user in the currently active editor or
     *         <code>null</code> if the user has no active editor.
     */
    public LineRange getViewport(User user) {
        if (sarosSession.getLocalUser().equals(user)) {
            throw new IllegalArgumentException(
                "Viewport of the local user was queried.");
        }

        RemoteEditor activeEditor = getEditorState(user).getActiveEditor();

        if (activeEditor == null)
            return null;

        return activeEditor.getViewport();
    }

    /**
     * Clears all state information associated with the given user.
     */
    public void removeUser(User participant) {
        editorStates.remove(participant);
    }

    public List<User> getRemoteOpenEditorUsers(SPath path) {
        ArrayList<User> result = new ArrayList<User>();
        for (RemoteEditorState state : editorStates.values()) {
            if (state.isRemoteOpenEditor(path))
                result.add(state.getUser());
        }
        return result;
    }

    public List<User> getRemoteActiveEditorUsers(SPath path) {
        ArrayList<User> result = new ArrayList<User>();

        for (RemoteEditorState state : editorStates.values()) {
            if (state.isRemoteActiveEditor(path)) {
                result.add(state.getUser());
            }
        }
        return result;
    }

    /**
     * Returns a set of all paths representing the editors which are currently
     * opened by the remote users of this shared session (i.e. not our own).
     * 
     * If no editors are opened an empty set is being returned.
     */
    public Set<SPath> getRemoteOpenEditors() {
        Set<SPath> result = new HashSet<SPath>();
        for (RemoteEditorState state : editorStates.values()) {
            result.addAll(state.openEditors.keySet());
        }
        return result;
    }

    /**
     * Returns a set of all paths representing the editors which are currently
     * opened and active by the remote users of this shared session (i.e. not
     * our own).
     * 
     * @return set of all active remote editors
     */
    public Set<SPath> getRemoteActiveEditors() {
        Set<SPath> result = new HashSet<SPath>();
        for (RemoteEditorState state : editorStates.values()) {
            if (state.getActiveEditor() != null)
                result.add(state.getActiveEditor().getPath());
        }
        return result;
    }

    /**
     * Returns a snapshot copy of all paths representing the editors which are
     * currently opened by the given user of this shared session (i.e. not our
     * own).
     * 
     * If no editors are opened by the given user an empty set is being
     * returned.
     */
    public Set<SPath> getRemoteOpenEditors(User user) {
        return getEditorState(user).getRemoteOpenEditors();
    }

    /**
     * Returns the active Editor which is currently open by the given user of
     * this shared session (i.e. not our own).
     * 
     * @return the active RemoteEditor or <code>null</code> if the given user
     *         has no editor open.
     */
    public RemoteEditor getRemoteActiveEditor(User user) {
        return getEditorState(user).getActiveEditor();
    }

    /**
     * Checks if the active editor of the given user is part of the Saros
     * session, or not. Convenience method for calling
     * getEditorState(user).getActiveEditor() != null.
     * 
     * @return <code>true</code>, if the currently active remote editor of the
     *         given user is shared via the Saros session, <code>false</code>
     *         otherwise.
     */
    public boolean isRemoteActiveEditorShared(User user) {
        return getRemoteActiveEditor(user) != null;
    }
}
