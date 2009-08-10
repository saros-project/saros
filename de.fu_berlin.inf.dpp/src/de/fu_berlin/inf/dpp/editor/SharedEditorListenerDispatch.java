package de.fu_berlin.inf.dpp.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.User;

/**
 * ISharedEditorListener which can dispatch to a changing set of
 * ISharedEditorListeners.
 */
public class SharedEditorListenerDispatch implements ISharedEditorListener {

    protected List<ISharedEditorListener> editorListeners = new ArrayList<ISharedEditorListener>();

    public void add(ISharedEditorListener editorListener) {
        if (!this.editorListeners.contains(editorListener)) {
            this.editorListeners.add(editorListener);
        }
    }

    public void remove(ISharedEditorListener editorListener) {
        this.editorListeners.remove(editorListener);
    }

    public void activeEditorChanged(User user, IPath path) {
        for (ISharedEditorListener listener : editorListeners) {
            listener.activeEditorChanged(user, path);
        }
    }

    public void editorRemoved(User user, IPath path) {
        for (ISharedEditorListener listener : editorListeners) {
            listener.editorRemoved(user, path);
        }
    }

    public void driverEditorSaved(IPath path, boolean replicated) {
        for (ISharedEditorListener listener : editorListeners) {
            listener.driverEditorSaved(path, replicated);
        }
    }

    public void followModeChanged(User user) {
        for (ISharedEditorListener listener : editorListeners) {
            listener.followModeChanged(user);
        }
    }

    public void textEditRecieved(User user, IPath editor, String text,
        String replacedText, int offset) {
        for (ISharedEditorListener listener : editorListeners) {
            listener.textEditRecieved(user, editor, text, replacedText, offset);
        }
    }
}
