package de.fu_berlin.inf.dpp.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.ILineRange;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IActivityReceiver;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.ViewportActivity;

/**
 * This class contains the state of the editors, viewports and selections of all
 * remote users as we believe it to be by listening to the activities we
 * receive.
 */
public class RemoteEditorManager {

    private static final Logger log = Logger
        .getLogger(RemoteEditorManager.class.getName());

    protected Map<User, RemoteEditorState> editorStates = new HashMap<User, RemoteEditorState>();

    protected IActivityReceiver activityReceiver = new AbstractActivityReceiver() {
        @Override
        public boolean receive(EditorActivity editorActivity) {
            switch (editorActivity.getType()) {
            case Activated:
                getEditorState(editorActivity.getUser()).activated(
                    editorActivity.getPath());
                break;
            case Saved:
                break;
            case Closed:
                getEditorState(editorActivity.getUser()).closed(
                    editorActivity.getPath());
                break;
            }

            return false;
        }

        @Override
        public boolean receive(ViewportActivity viewportActivity) {

            getEditorState(viewportActivity.getUser()).setViewport(
                viewportActivity.getEditor(), viewportActivity.getLineRange());

            return false;
        }

        @Override
        public boolean receive(TextSelectionActivity textSelectionActivity) {

            getEditorState(textSelectionActivity.getUser()).setSelection(
                textSelectionActivity.getEditor(),
                textSelectionActivity.getSelection());

            return false;
        }
    };

    /**
     * One editor of one user
     */
    public static class RemoteEditor {

        IPath path;

        ITextSelection selection;

        ILineRange viewport;

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
     * This class represents the state of the editors, viewport, selection of
     * one remote user.
     */
    public static class RemoteEditorState {

        public RemoteEditorState(User user) {
            this.user = user;
        }

        public void setSelection(IPath path, ITextSelection selection) {

            if (!openEditors.containsKey(path)) {
                log.warn("Selection for editor which was never activated: "
                    + path);
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

        private RemoteEditor getRemoteEditor(IPath path) {
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
                activeEditor = getLastEditor();
            }
        }

        public User user;

        LinkedHashMap<IPath, RemoteEditor> openEditors = new LinkedHashMap<IPath, RemoteEditor>();

        RemoteEditor activeEditor;

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

    }

    public RemoteEditorState getEditorState(User user) {

        RemoteEditorState result = editorStates.get(user);
        if (result == null) {
            result = new RemoteEditorState(user);
            editorStates.put(user, result);
        }
        return result;
    }

    public void exec(IActivity activity) {
        activity.dispatch(activityReceiver);
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

}
