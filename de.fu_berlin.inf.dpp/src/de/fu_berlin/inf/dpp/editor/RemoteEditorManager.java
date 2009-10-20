package de.fu_berlin.inf.dpp.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.ILineRange;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.AbstractActivityDataObjectReceiver;
import de.fu_berlin.inf.dpp.activities.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.IActivityDataObjectReceiver;
import de.fu_berlin.inf.dpp.activities.serializable.EditorActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextSelectionActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ViewportActivityDataObject;
import de.fu_berlin.inf.dpp.project.ISharedProject;

/**
 * This class contains the state of the editors, viewports and selections of all
 * remote users as we believe it to be by listening to the activityDataObjects
 * we receive.
 */
public class RemoteEditorManager {

    private static final Logger log = Logger
        .getLogger(RemoteEditorManager.class);

    protected Map<User, RemoteEditorState> editorStates = new HashMap<User, RemoteEditorState>();

    protected ISharedProject sharedProject;

    /**
     * One editor of one user
     */
    public static class RemoteEditor {

        protected IPath path;

        protected ITextSelection selection;

        protected ILineRange viewport;

        public RemoteEditor(IPath path) {
            this.path = path;
        }

        public IPath getPath() {
            return path;
        }

        public ILineRange getViewport() {
            return viewport;
        }

        public void setViewport(ILineRange viewport) {
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
     * This class represents the state of the editors, viewports, selection of
     * one remote user.
     */
    public static class RemoteEditorState {

        protected User user;

        protected LinkedHashMap<IPath, RemoteEditor> openEditors = new LinkedHashMap<IPath, RemoteEditor>();

        protected RemoteEditor activeEditor;

        public RemoteEditorState(User user) {
            this.user = user;
        }

        public void setSelection(IPath path, ITextSelection selection) {

            if (!openEditors.containsKey(path)) {
                log.warn("Received selection from user [" + this.user
                    + "] for editor which was never activated: " + path);
                return;
            }

            getRemoteEditor(path).setSelection(selection);

        }

        public void setViewport(IPath path, ILineRange viewport) {

            if (!openEditors.containsKey(path)) {
                log.warn("Viewport for editor which was never activated: "
                    + path);
                return;
            }

            getRemoteEditor(path).setViewport(viewport);

        }

        public void activated(IPath path) {
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

        /**
         * Returns a RemoteEditor representing the given path for the given
         * user.
         * 
         * This method never returns null but creates a new RemoteEditor lazily.
         * 
         * To query whether the user of this RemoteEditorState has the editor of
         * the given path open use isRemoteOpenEditor().
         */
        public RemoteEditor getRemoteEditor(IPath path) {
            RemoteEditor result = openEditors.get(path);
            if (result == null) {
                result = new RemoteEditor(path);
                openEditors.put(path, result);
            }
            return result;
        }

        public RemoteEditor getLastEditor() {
            RemoteEditor editor = null;
            Iterator<RemoteEditor> it = openEditors.values().iterator();
            while (it.hasNext()) {
                editor = it.next();
            }
            return editor;
        }

        public void closed(IPath path) {

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

        protected IActivityDataObjectReceiver activityDataObjectReceiver = new AbstractActivityDataObjectReceiver() {
            @Override
            public void receive(
                EditorActivityDataObject editorActivityDataObject) {

                switch (editorActivityDataObject.getType()) {
                case Activated:
                    activated(editorActivityDataObject.getPath());
                    break;
                case Saved:
                    break;
                case Closed:
                    closed(editorActivityDataObject.getPath());
                    break;
                }
            }

            @Override
            public void receive(
                ViewportActivityDataObject viewportActivityDataObject) {

                setViewport(viewportActivityDataObject.getEditor(),
                    viewportActivityDataObject.getLineRange());
            }

            @Override
            public void receive(
                TextSelectionActivityDataObject textSelectionActivityDataObject) {

                setSelection(textSelectionActivityDataObject.getEditor(),
                    textSelectionActivityDataObject.getSelection());
            }
        };

        /**
         * Returns the activeEditor of the user of this RemoteEditorState or
         * null if the user has no editor open currently.
         */
        public RemoteEditor getActiveEditor() {
            return this.activeEditor;
        }

        public boolean isRemoteActiveEditor(IPath path) {
            if (activeEditor != null && activeEditor.getPath().equals(path))
                return true;
            return false;
        }

        public boolean isRemoteOpenEditor(IPath path) {
            return openEditors.containsKey(path);
        }

        public User getUser() {
            return this.user;
        }

        /**
         * Returns a snapshot copy of the editors open for the remote user
         * represented by this RemoteEditorState.
         */
        public Set<IPath> getRemoteOpenEditors() {
            return new HashSet<IPath>(openEditors.keySet());
        }

        public void exec(IActivityDataObject activityDataObject) {
            activityDataObject.dispatch(activityDataObjectReceiver);
        }
    }

    public RemoteEditorManager(ISharedProject sharedProject) {
        this.sharedProject = sharedProject;
    }

    public RemoteEditorState getEditorState(User user) {

        RemoteEditorState result = editorStates.get(user);
        if (result == null) {
            result = new RemoteEditorState(user);
            editorStates.put(user, result);
        }
        return result;
    }

    public void exec(IActivityDataObject activityDataObject) {

        User sender = sharedProject.getUser(activityDataObject.getSource());

        getEditorState(sender).exec(activityDataObject);
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
     * Clears all state information associated with the given user.
     */
    public void removeUser(User participant) {
        editorStates.remove(participant);
    }

    public List<User> getRemoteOpenEditorUsers(IPath path) {
        ArrayList<User> result = new ArrayList<User>();
        for (RemoteEditorState state : editorStates.values()) {
            if (state.isRemoteOpenEditor(path))
                result.add(state.getUser());
        }
        return result;
    }

    public List<User> getRemoteActiveEditorUsers(IPath path) {

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
    public Set<IPath> getRemoteOpenEditors() {
        Set<IPath> result = new HashSet<IPath>();
        for (RemoteEditorState state : editorStates.values()) {
            result.addAll(state.openEditors.keySet());
        }
        return result;
    }

    /**
     * Returns a snapshot copy of all paths representing the editors which are
     * currently opened by the given remote user of this shared session (i.e.
     * not our own).
     * 
     * If no editors are opened by the given user an empty set is being
     * returned.
     */
    public Set<IPath> getRemoteOpenEditors(User user) {
        return getEditorState(user).getRemoteOpenEditors();
    }
}
